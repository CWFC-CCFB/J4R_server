/*
 * This file is part of the j4r library.
 *
 * Copyright (C) 2020 Mathieu Fortin for Canadian Forest Service.
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
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class ServerConfiguration implements Serializable {

	private static final long serialVersionUID = 20111222L;
	
	private static final int MIN_PORT = 1024;
	private static final int MAX_PORT = 65535;
	
	protected final int numberOfClientThreads;
	protected final int maxSizeOfWaitingList;
	protected final int[] listiningPorts;
	protected final int internalPort;
	protected final boolean isLocal;
	protected final int key;
	protected final String wd;

	
	/**
	 * Constructor. 
	 * @param numberOfClientThreads number of threads that can answer calls.
	 * @param maxSizeOfWaitingList number of pending calls
	 * @param listeningPort port on which the server exchange the information with the clients
	 * @param internalPort port on which the server can be accessed (backdoor port)
	 */
	public ServerConfiguration(int numberOfClientThreads, int maxSizeOfWaitingList, int[] listeningPorts, int internalPort) {
		this(listeningPorts, internalPort, numberOfClientThreads, maxSizeOfWaitingList, false, -1, null);
	}

	
	private ServerConfiguration(int[] listeningPorts, int internalPort, int numberOfClientThreads, int maxSizeOfWaitingList, boolean isLocal, int key, String wd) {
		this.isLocal = isLocal;
		if (numberOfClientThreads < 0 || numberOfClientThreads > 10) {
			throw new InvalidParameterException("Number of client threads should be between 1 and 10!"); 
		} else {
			this.numberOfClientThreads = numberOfClientThreads;
		}
		if (listeningPorts == null) {
			throw new InvalidParameterException("The listeningPorts argument cannot be set to null!");
		}
		for (int port : listeningPorts) {
			checkPort(port, "listening");
		}
		this.listiningPorts = listeningPorts;
		checkPort(internalPort, "internal");
		this.internalPort = internalPort;
		if (maxSizeOfWaitingList < 0) {
			this.maxSizeOfWaitingList = 0;
		} else {
			this.maxSizeOfWaitingList = maxSizeOfWaitingList;
		}
		this.key = key;
		if (isLocalServer()) {
			if (wd == null || !new File(wd).exists()) {
				this.wd = System.getProperty("java.io.tmpdir");
			} else {
				this.wd = wd;
			}
		} else {
			this.wd = null;
		}
	}
	
	/**
	 * Configuration for local server
	 * @param listiningPorts the ports to which the ServerSocket will listen (0 for random port selection)
	 * @param internalPort the backdoor port (0 for random port selection)
	 * @param key a security key to ensure the client is really the local user
	 * @param wd a string representing the working directory. If it is invalid, then Java uses the temporary directory as
	 * specified in the property 
	 */
	public ServerConfiguration(int[] listiningPorts, int internalPort, int key, String wd) {
		this(listiningPorts, internalPort, 1, 0, true, key, wd);
	}
	
	boolean isLocalServer() {return isLocal;}

	protected List<ServerSocket> createServerSockets() throws IOException {
		List<ServerSocket> sockets = new ArrayList<ServerSocket>();
		for (int port : listiningPorts) {
			sockets.add(new ServerSocket(port));
		}
		return sockets;
	}
	
	private void checkPort(int port, String portType) {
		if (port != 0 && (port < MIN_PORT || port > MAX_PORT)) {
			throw new InvalidParameterException("The " + portType + " port must be between " + MIN_PORT + " and " + MAX_PORT + " (or can be 0 for a random port)!");
		}			
	}
	
	
}	