/*
 * This file is part of the j4r library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
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
package j4r.app;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import j4r.lang.J4RSystem;
import j4r.net.server.JavaGatewayServer;
import j4r.net.server.ServerConfiguration;

/**
 * The main entry point for the J4R server.
 * @author Mathieu Fortin - April 2023
 */
public class Startup {
	
	static boolean TestPurpose = false;

	private static final String LOGLEVEL = "-loglevel";
	
	private static final Random RANDOM = new Random();
	
	static File LogFile = null;

	private static int generateSecurityKey() {
		return RANDOM.nextInt();
	}

	private static int[] parsePorts(String str) {
		String[] p = str.split(JavaGatewayServer.PortSplitter);
		int[] ports = new int[p.length];
		for (int i = 0; i < p.length; i++) {
			ports[i] = Integer.parseInt(p[i]);
		}
		return ports;
	}
	
	private static void createLogFile(String wd) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd");  
		LocalDateTime now = LocalDateTime.now();  
		if (wd != null && new File(wd).isDirectory()) {
			LogFile = new File(wd + File.separator + "J4RServer" + dtf.format(now) + ".log");
		} else {
			LogFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "J4RServer" + dtf.format(now) + ".log");
		}
		if (LogFile.exists())
			LogFile.delete();
		try {
			FileHandler fh = new FileHandler(LogFile.getAbsolutePath());  
		    AbstractGenericEngine.J4RLogger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter);  
		} catch (Exception e) {
			System.out.println("I did not manage to create a log file (Exception thrown!)");
			AbstractGenericEngine.J4RLogger.log(Level.SEVERE, "Unable to create log file!");
		}
	}

//	public static String findClassPath() {
//		String jarFilename = JarUtility.getJarFileIAmInIfAny(REnvironment.class);
//
//		String classPath = jarFilename != null ? 
//				jarFilename.substring(jarFilename.lastIndexOf(ObjectUtility.PathSeparator) + 1) : // adding quotes to deal with spaces in path MF2022-09-13
//				ObjectUtility.getTrueRootPath(REnvironment.class);  // adding quotes to deal with spaces in path MF2022-09-13
//		
//		AbstractGenericEngine.J4RLogger.log(Level.INFO, "ClassPath = " + classPath);
//		return classPath;
//	}
	

//	FORMER documentation	
//	 * To define the classpath, use the "-ext" plus the different paths separated by the REnvironment.ClassPathSeparator 
//	 * variable (e.g. <code> -ext C:\myExtention\*::C:\myClasses\</code>).

	/**
	 * Main entry point for creating a REnvironment hosted by a Java local gateway server. <br>
	 * <br>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			System.out.println("Usage: [-firstcall <value>] [-ports <value>] [-backdoorport <value>]");
			System.out.println("       [-key <value>] [-public <value>] [-loglevel <value>]");
			System.out.println("");
			System.out.println("   firstcall     either true or false");
			System.out.println("   ports         one positive integer or a series of integer separated by :");
			System.out.println("                 e.g. 18000:18001");
			System.out.println("   backdoorport  two integers seperated by :   e.g. 50000:50001");
			System.out.println("   key           an integer (only needed if public is set to on)");
			System.out.println("   public        either on or off");
			System.out.println("   loglevel      INFO, FINE, FINER, FINEST");
			return;
		} 
		JavaGatewayServer server = null;
		try {
			List<String> arguments = J4RSystem.setClassicalOptions(args);
			String wd = J4RSystem.retrieveArgument(JavaGatewayServer.WD, arguments);
			createLogFile(wd);

			String logLevel = J4RSystem.retrieveArgument(LOGLEVEL, arguments);
			if (logLevel != null) {
				Level l = null;
				try {
					l = Level.parse(logLevel.toUpperCase());
					AbstractGenericEngine.J4RLogger.setLevel(l);
				} catch(Exception e) {
					AbstractGenericEngine.J4RLogger.log(Level.WARNING, "Unable to set this log level: " + logLevel);
				}
			}
			
			String portStr = J4RSystem.retrieveArgument(JavaGatewayServer.PORT, arguments);
			int[] listeningPorts;
			if (portStr != null) {
				listeningPorts = parsePorts(portStr);
			} else {
				String nbPortsStr = J4RSystem.retrieveArgument(JavaGatewayServer.NB_PORTS, arguments);
				int nbPorts;
				if (nbPortsStr == null) {
					nbPorts = 1;
				} else {
					nbPorts = Integer.parseInt(nbPortsStr);
				}
				listeningPorts = new int[nbPorts];
			}
			String backdoorportStr = J4RSystem.retrieveArgument(JavaGatewayServer.BACKDOORPORT, arguments);
			int[] backdoorports;
			if (backdoorportStr != null) {
				backdoorports = parsePorts(backdoorportStr);
			} else {
				backdoorports = new int[2];
			}
			String publicMode = J4RSystem.retrieveArgument(JavaGatewayServer.PUBLIC, arguments);
			int key;
			boolean isPublic = false;
			if (publicMode != null && publicMode.trim().toLowerCase().equals("on")) {
				isPublic = true;
				String keyStr = J4RSystem.retrieveArgument(JavaGatewayServer.KEY, arguments);
				key = Integer.parseInt(keyStr);
			} else {
				key = generateSecurityKey();
			}
			ServerConfiguration conf;
			if (isPublic) {
 				conf = new ServerConfiguration(1, 10, listeningPorts, backdoorports, key);
			} else {
 				conf = new ServerConfiguration(listeningPorts, backdoorports, key, wd);
			}
			server = new JavaGatewayServer(conf, null);	// null: no need for a main instance 
			server.startApplication();
		} catch (Exception e) {
			if (TestPurpose) {
				throw e;
			} else {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		}
	}
	

}
