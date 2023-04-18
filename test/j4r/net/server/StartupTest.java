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
package j4r.net.server;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

import j4r.app.Startup;
import j4r.net.PortBindingException;

public class StartupTest {
	
	private static double LagTime = 5d;

	private static double getNbSecondsSinceLastModification(String filename) throws IOException {
		File f = new File(filename);
		double nbSecs = (System.currentTimeMillis() - f.lastModified()) * .001;
		return nbSecs;
	}
	
	/*
	 * Test the creation of J4RTmpFile and its content.
	 */
	@Test
	public void startupJ4RTmpFileCreationTest() throws Exception {
		List<String> arguments = new ArrayList<String>();
		arguments.add("-ports");
		arguments.add("0:0");
		arguments.add("-backdoorports");
		arguments.add("0:0");
		arguments.add("-loglevel");
		arguments.add("INFO");
		arguments.add("-wd");
		arguments.add(System.getProperty("java.io.tmpdir"));
		Startup.main(arguments.toArray(new String[] {}));
		
		Thread.sleep(2000);
		
		String J4RTmpFilename = System.getProperty("java.io.tmpdir") + File.separator + "J4RTmpFile";
		double nbSecs = getNbSecondsSinceLastModification(J4RTmpFilename); 
		Assert.assertTrue("Time since creation of J4RTmpFile smaller than " + LagTime + " sec.", nbSecs < LagTime);

		Scanner scanner = new Scanner(new File(J4RTmpFilename));
		String fileContent = scanner.nextLine();
		String[] content = fileContent.split(";");
		Assert.assertEquals("Testing file content", 3, content.length);
		scanner.close();
		JavaGatewayServer.cleanupAfterTesting();

	}

	
	/*
	 * Test a public server with no key.
	 */
	@Test
	public void startupPublicServerWithNoKey() throws Exception {
		List<String> arguments = new ArrayList<String>();
		arguments.add("-ports");
		arguments.add("0:0");
		arguments.add("-backdoorports");
		arguments.add("0:0");
		arguments.add("-public");
		arguments.add("on");
		arguments.add("-loglevel");
		arguments.add("INFO");
		arguments.add("-wd");
		arguments.add(System.getProperty("java.io.tmpdir"));
		Startup.main(arguments.toArray(new String[] {}));
		Thread.sleep(2000);
		JavaGatewayServer.cleanupAfterTesting();
	}

	/*
	 * Test a public server whose key cannot be parsed.
	 */
	@Test
	public void startupPublicServerWithWrongKey() throws Exception {
		List<String> arguments = new ArrayList<String>();
		arguments.add("-ports");
		arguments.add("0:0");
		arguments.add("-backdoorports");
		arguments.add("0:0");
		arguments.add("-public");
		arguments.add("on");
		arguments.add("-key");
		arguments.add("Sunlight");
		arguments.add("-loglevel");
		arguments.add("INFO");
		arguments.add("-wd");
		arguments.add(System.getProperty("java.io.tmpdir"));
		Startup.main(arguments.toArray(new String[] {}));
		Thread.sleep(2000);
		JavaGatewayServer.cleanupAfterTesting();
	}

	
	@Test
	public void testFailureDueToPortBindingException1() throws Exception {
		Startup.TestPurpose = true;
		ServerSocket s = new ServerSocket(0);
		List<String> arguments = new ArrayList<String>();
		arguments.add("-ports");
		arguments.add(s.getLocalPort() + ":0");
		arguments.add("0:0");
		arguments.add("-backdoorports");
		arguments.add("0:0");
		arguments.add("-wd");
		arguments.add("null");
		try {
			Startup.main(arguments.toArray(new String[] {}));
		} catch (Exception e) {
			assertTrue(e instanceof PortBindingException);
		}
		s.close();
		JavaGatewayServer.cleanupAfterTesting();
		Startup.TestPurpose = false;
	}

	@Test
	public void testFailureDueToPortBindingException2() throws Exception {
		Startup.TestPurpose = true;
		ServerSocket s = new ServerSocket(0);
		List<String> arguments = new ArrayList<String>();
		arguments.add("-ports");
		arguments.add("0:0");
		arguments.add("0:0");
		arguments.add("-backdoorports");
		arguments.add(s.getLocalPort() + ":0");
		arguments.add("-wd");
		arguments.add("null");
		try {
			Startup.main(arguments.toArray(new String[] {}));
		} catch (Exception e) {
			assertTrue(e instanceof PortBindingException);
		}
		s.close();
		JavaGatewayServer.cleanupAfterTesting();
		Startup.TestPurpose = false;
	}

	@Test
	public void testFailureDueToPortBindingException3() throws Exception {
		Startup.TestPurpose = true;
		ServerSocket s = new ServerSocket(0);
		List<String> arguments = new ArrayList<String>();
		arguments.add("-ports");
		arguments.add("0:0");
		arguments.add("0:0");
		arguments.add("-backdoorports");
		arguments.add("0:" + s.getLocalPort());
		arguments.add("-wd");
		arguments.add("null");
		try {
			Startup.main(arguments.toArray(new String[] {}));
		} catch (Exception e) {
			assertTrue(e instanceof PortBindingException);
		}
		s.close();
		JavaGatewayServer.cleanupAfterTesting();
		Startup.TestPurpose = false;
	}

	/**
	 * Entry point for testing j4r_server implementation from an existing Java application.
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Object myMainInstance = new ArrayList();  // here change the constructor of ArrayList for that of the application
		ServerConfiguration servConf = new ServerConfiguration(1, 10, new int[] {18000}, new int[] {50000,50001}, 212);
		JavaGatewayServer server = new JavaGatewayServer(servConf, myMainInstance);
		server.startApplication();
	}
	
}
