/*
 * This file is part of the j4r library.
 *
 * Copyright (C) 2020-2021 Her Majesty the Queen in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package j4r.net.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import j4r.app.Startup;
import j4r.lang.codetranslator.REnvironment;
import j4r.net.server.BasicClient.ClientRequest;

/**
 * The JavaLocalGatewayServer class is a server that makes it possible 
 * to create objects and to execute methods in Java from a non-Java application.
 * @author Mathieu Fortin 
 */
public class JavaGatewayServer extends AbstractServer {
	
	private static final List<Charset> PotentialCharsets = new ArrayList<Charset>();
	static {
		PotentialCharsets.add(Charset.forName("UTF-8"));
		PotentialCharsets.add(Charset.forName("ISO-8859-1"));
	}
	
	
	public static final String EXTENSION = "-ext";
	public static final String PORT = "-ports"; 
	public static final String BACKDOORPORT = "-backdoorports";
	public static final String NB_PORTS = "-nbports";
	public static final String WD = "-wd";
	public static final String MEMORY = "-mem";
	public static final String PortSplitter = ":";
	public static final String PUBLIC = "-public";	
	public static final String KEY = "-key";
	public static final String HEADLESS = "-headless";

	
	/**
	 * A wrapper for Exception.
	 * @author Mathieu Fortin
	 */
	@SuppressWarnings("serial")
	final class JavaGatewayException extends Exception {
		
		final Throwable nestedException;
		
		JavaGatewayException(Throwable e) {
			super(e);
			this.nestedException = e;
		}

		
		public String toString() {
			StringBuilder stackTraceSB = new StringBuilder();
			StackTraceElement[] st = nestedException.getStackTrace();
			int length = st.length > 10 ? 10 : st.length;
			for (int i = 0; i < length; i++) {
				if (i == 0) {
					stackTraceSB.append(st[i].toString());
				} else {
					stackTraceSB.append(System.lineSeparator() + st[i].toString());
				}
			}
			String stackTraceStr = stackTraceSB.toString();
			
			return this.getClass().getName() + "_" + nestedException.toString() + System.lineSeparator() + stackTraceStr;
		}
	}
	

	private class JavaGatewayClientThread extends ClientThread {

		REnvironment translator;
		
		protected JavaGatewayClientThread(AbstractServer.CallReceiverThread receiver, int workerID) {
			super(receiver, workerID);
		}

		@Override
		public void run() {
			while(!shutdownCall) {
				try {
					firePropertyChange("status", null, "Waiting");
					socketWrapper = receiver.clientQueue.take();
					InetAddress clientAddress = socketWrapper.getInetAddress();
					this.translator = registerClient(clientAddress);
					JavaGatewayServer.this.whoIsWorkingForWho.put(this, clientAddress);
					firePropertyChange("status", null, "Connected to client: " + clientAddress.getHostAddress());		
					while (!socketWrapper.isClosed()) {
						try {
							Object somethingInParticular = processRequest();
							if (somethingInParticular != null) {
								if (Thread.interrupted()) {
									throw new InterruptedException();
								}
								if (somethingInParticular.equals(BasicClient.ClientRequest.closeConnection) 
										|| somethingInParticular.equals(BasicClient.ClientRequest.closeConnection.name())) {
									socketWrapper.writeObject(ServerReply.ClosingConnection);
									closeSocket();
									JavaGatewayServer.this.whoIsWorkingForWho.remove(this);
									if (!JavaGatewayServer.this.whoIsWorkingForWho.contains(clientAddress)) {
										JavaGatewayServer.this.translators.remove(clientAddress);
									}
									if (JavaGatewayServer.this.isPrivate()) {
										JavaGatewayServer.this.requestShutdown();
									}
									break;
								} else {
									socketWrapper.writeObject(somethingInParticular);
								}
							} else {
								socketWrapper.writeObject(ServerReply.Done);
							}
						} catch (Exception e) {		// something wrong happened during the processing of the request
							try {
								if (e instanceof IOException) {	// seems that the connection was lost
									closeSocket();
								} else if (!socketWrapper.isClosed()) {
//									AbstractGenericEngine.J4RLogger.log(Level.SEVERE, e.getMessage(), e);
									if (e instanceof InvocationTargetException) {
										socketWrapper.writeObject(new JavaGatewayException(((InvocationTargetException) e).getTargetException()));
									} else {
										socketWrapper.writeObject(new JavaGatewayException(e));
									}
								}
							} catch (IOException e1) {}
						}
					}
					if (JavaGatewayServer.this.shutdownOnClosedConnection) {
						JavaGatewayServer.this.requestShutdown();
						break;
					}
				} catch (InterruptedException e) {}
			}
		}


		@Override
		protected Object processRequest() throws Exception {
			Object crudeRequest = getSocket().readObject();
			if (crudeRequest instanceof String) {
				String request = (String) crudeRequest;
				if (ClientRequest.closeConnection.name().equals(request.trim())) {
					return ClientRequest.closeConnection;
				} else {
					if (request.startsWith("time")) {
						long startMillisec = Long.parseLong(request.substring(4));
						long finalTime = System.currentTimeMillis();
						double elapsedTime =  (finalTime - startMillisec);
						Startup.getMainLogger().log(Level.FINE, "Elapsed time single received packet:" + elapsedTime);
					}
					return this.translator.processCode(request);
				}
			} else {
				return null;
			}
		}

	}

	private static JavaGatewayServer Instance;
	
	protected final ConcurrentHashMap<InetAddress, REnvironment> translators;	
	protected final boolean shutdownOnClosedConnection;
	private final Object mainInstance;
	
	/**
	 * Constructor.
	 * @param servConf a ServerConfiguration instance
	 * @param mainInstance an Object that can later be accessed by the client using the static getMainInstance() method
	 * @throws Exception
	 */
	public JavaGatewayServer(ServerConfiguration servConf, Object mainInstance) throws Exception {
		super(servConf, false);
		this.translators = new ConcurrentHashMap<InetAddress, REnvironment>();
		this.shutdownOnClosedConnection = servConf.isPrivateServer();	// enable shutdown on close connection for private servers only
		this.mainInstance = mainInstance;
		Instance = this;
	}

	static void cleanupAfterTesting() {
		Instance = null;
	}
	
	/**
	 * Provide the main instance. <br>
	 * <br> 
	 * This instance is set through the constructor and can be null if there is no need for such an instance.
	 * @return an Object instance
	 */
	public static Object getMainInstance() {
		return Instance.mainInstance;
	}

	
	synchronized REnvironment registerClient(InetAddress clientAddress) {
		if (!translators.containsKey(clientAddress)) {
			translators.put(clientAddress, new REnvironment());
		}
		return(translators.get(clientAddress));
	}

	/**
	 * Return true if the server is public, false if it is private or if no
	 * server is running. 
	 * @return a boolean instance
	 */
	public static Boolean isPublicServerRunning() {
		if (Instance != null) {
			return !Instance.isPrivate();
		} else {
			return false;
		}
	}
	


	@Override
	protected ClientThread createClientThread(AbstractServer.CallReceiverThread receiver, int id) {
		return new JavaGatewayClientThread(receiver, id);
	}

	

	@Override
	protected void createFileInfoForLocalServer() throws IOException {
		String filename = getConfiguration().wd.trim() + File.separator + "J4RTmpFile";
		
		// create the lock file
		String lockFilename = filename + ".lock";
		File lockFile = new File(lockFilename);
		FileWriter lockWriter = new FileWriter(lockFile);
		lockWriter.write("");
		lockWriter.close();
		
		File file = new File(filename);
		String realizedListeningPorts = "";
		for (CallReceiverThread t : callReceiverThreads) {
			if (!realizedListeningPorts.isEmpty()) {
				realizedListeningPorts += PortSplitter;
			}
			realizedListeningPorts += t.serverSocket.getLocalPort();
		}
		String outputStr = "" + getConfiguration().key + ";" + 
				backdoorThread.emergencySocket.getLocalPort() + PortSplitter + gcReceiverThread.serverSocket.getLocalPort() + ";" + 
				realizedListeningPorts; 
		FileWriter writer = new FileWriter(file);
		writer.write(outputStr);
		writer.close();
		
		lockFile.delete();	// delete the lock file
	}

	@Override
	protected List<Charset> getPotentialCharsets() {
		return PotentialCharsets;
	}

	
}
