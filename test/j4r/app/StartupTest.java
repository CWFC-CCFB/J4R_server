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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Assert;
import org.junit.Test;

import j4r.net.PortBindingException;

public class StartupTest {
	
	private static double LagTime = 5d;

	private static double getNbSecondsSinceLastModification(String filename) throws IOException {
		System.out.println(filename);
		File f = new File(filename);
		double nbSecs = (System.currentTimeMillis() - f.lastModified()) * .001;
		System.out.println(nbSecs);
		return nbSecs;
	}
	
	/*
	 * Test the creation of the J4RTmpFile and log files as well as their contents.
	 */
	@Test
	public void startupJ4RTmpFileCreationTest() throws Exception {
		List<String> arguments = new ArrayList<String>();
		arguments.add("-ports");
		arguments.add("0:0");
		arguments.add("-backdoorport");
		arguments.add("0:0");
		arguments.add("-public");
		arguments.add("off");
		arguments.add("-loglevel");
		arguments.add("INFO");
		arguments.add("-wd");
		arguments.add(System.getProperty("java.io.tmpdir"));
		Startup.main(arguments.toArray(new String[] {}));
		
		Thread.sleep(2000);
		
		String J4RTmpFilename = System.getProperty("java.io.tmpdir") + File.separator + "J4RTmpFile";
		double nbSecs = getNbSecondsSinceLastModification(J4RTmpFilename); 
		Assert.assertTrue("Time since creation of J4RTmpFile smaller than " + LagTime + " sec.", nbSecs < LagTime);

		String logFilename = Startup.LogFile.getAbsolutePath();
		nbSecs = getNbSecondsSinceLastModification(logFilename); 
		Assert.assertTrue("Time since creation of log file smaller than " + LagTime + " sec.", nbSecs < LagTime);

		Scanner scanner = new Scanner(new File(J4RTmpFilename));
		String fileContent = scanner.nextLine();
		String[] content = fileContent.split(";");
		Assert.assertEquals("Testing file content", 3, content.length);
		scanner.close();

		scanner = new Scanner(new File(logFilename));
		String lastLine = null;
		while(scanner.hasNextLine()) {
			lastLine = scanner.nextLine();
		}
		Assert.assertEquals("Testing last line of log", "INFOS: Server started", lastLine);
		scanner.close();

	}

	@Test
	public void testFailureDueToPortBindingException1() throws Exception {
		Startup.TestPurpose = true;
		ServerSocket s = new ServerSocket(0);
		List<String> arguments = new ArrayList<String>();
		arguments.add("-ports");
		arguments.add(s.getLocalPort() + ":0");
		arguments.add("0:0");
		arguments.add("-backdoorport");
		arguments.add("0:0");
		arguments.add("-wd");
		arguments.add("null");
		try {
			Startup.main(arguments.toArray(new String[] {}));
		} catch (Exception e) {
			assertTrue(e instanceof PortBindingException);
		}
		s.close();
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
		arguments.add("-backdoorport");
		arguments.add(s.getLocalPort() + ":0");
		arguments.add("-wd");
		arguments.add("null");
		try {
			Startup.main(arguments.toArray(new String[] {}));
		} catch (Exception e) {
			assertTrue(e instanceof PortBindingException);
		}
		s.close();
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
		arguments.add("-backdoorport");
		arguments.add("0:" + s.getLocalPort());
		arguments.add("-wd");
		arguments.add("null");
		try {
			Startup.main(arguments.toArray(new String[] {}));
		} catch (Exception e) {
			assertTrue(e instanceof PortBindingException);
		}
		s.close();
		Startup.TestPurpose = false;
	}

}
