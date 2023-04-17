/*
 * This file is part of the j4r library.
 *
 * Copyright (C) 2020-2022 Her Majesty the Queen in right of Canada
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
package j4r.lang.codetranslator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;

import j4r.net.server.ServerConfiguration;

public class TranslationTest {

	@SuppressWarnings({"rawtypes" })
	@Test
	public void testLocalServerMultipleRequests() throws Exception {
		String filename = System.getProperty("java.io.tmpdir") + File.separator +  "J4RTmpFile";
		File j4rTMP = new File(filename);
		String lockFilename = System.getProperty("java.io.tmpdir") + File.separator + "J4RTmpFile.lock";
		File j4rTMPLock = new File(lockFilename);
		
		System.out.println(filename);
		if (j4rTMP.exists()) {
			j4rTMP.delete();
		}
		FakeJavaGatewayServer server = new FakeJavaGatewayServer(new ServerConfiguration(new int[]{0}, new int[]{0, 0}, 101, null) , null);	
		server.startApplication();

		while(!j4rTMP.exists()) {}		
		
		while(j4rTMPLock.exists()) {}		
		
		BufferedReader br = new BufferedReader(new FileReader(j4rTMP));
		String line = br.readLine();
		
		String[] info = line.split(";");
		br.close();
		
		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), Integer.parseInt(info[2]));
		FakeRClient client = new FakeRClient(socketAddress, false, Integer.parseInt(info[0]));	// false: it does as if was not a Java application
//		client.sendTimeToServer();

		long start = System.currentTimeMillis();
		Object arrayListRepresentation = client.createAnArrayList(); // creates a FakeArrayList object which will be used to test the Double vs double method call
		double diff = (System.currentTimeMillis() - start) * .001;
		System.out.println("Time to process the creation of an ArrayList instance in TCP : " + diff);
		Assert.assertTrue(arrayListRepresentation != null);
		Assert.assertTrue(arrayListRepresentation.toString().startsWith(REnvironment.R_JAVA_OBJECT_TOKEN + REnvironment.MainSplitter + "j4r.lang.codetranslator.FakeArrayList"));
		String hashCode = arrayListRepresentation.toString().substring(arrayListRepresentation.toString().indexOf("@") + 1);
		
		REnvironment env = server.getEnvironmentMap().get(InetAddress.getLoopbackAddress());
		
		ArrayList trueArrayList = (ArrayList) env.findObjectInEnvironment(REnvironment.R_JAVA_OBJECT_HASHCODE_PREFIX + hashCode).get(0).value;
		
		Object callback = client.addThisToArrayList(arrayListRepresentation, "characterhello world!");		// here we add "hello world!" to the arraylist
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.toString().equals("lotrue"));
		Assert.assertTrue(trueArrayList.get(0).equals("hello world!"));

		callback = client.addThisToArrayList(arrayListRepresentation, "integer1");  	// here we add the integer 1 to the arraylist
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.toString().equals("lotrue"));
		Assert.assertTrue((int) trueArrayList.get(1) == 1);

		callback = client.testThisDoubleWrapper(arrayListRepresentation);		// here we test if a method with Double as argument will be match to a double 
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.toString().equals("nu0.0"));

		callback = client.createAVectorWithArguments();
		Assert.assertTrue(callback != null);
		hashCode = callback.toString().substring(callback.toString().indexOf("@") + 1);
		Vector listWithCapacityOf3 = (Vector) env.findObjectInEnvironment(REnvironment.R_JAVA_OBJECT_HASHCODE_PREFIX + hashCode).get(0).value;
		Assert.assertEquals("Testing capacity", listWithCapacityOf3.capacity(), 3);

		callback = client.createMultipleVectorWithArguments();			// creating several objects at once
		Assert.assertTrue(callback != null);
		String[] objectReps = callback.toString().split(REnvironment.SubSplitter);
		int expCapacity = 3;
		for (String objRep : objectReps) {
			hashCode = objRep.toString().substring(objRep.toString().indexOf("@") + 1);
			Vector vec = (Vector) env.findObjectInEnvironment(REnvironment.R_JAVA_OBJECT_HASHCODE_PREFIX + hashCode).get(0).value;
			Assert.assertEquals("Testing capacity", vec.capacity(), expCapacity);
			expCapacity++;
		}
		
		callback = client.createArrayListWithCollection();
		hashCode = callback.toString().substring(callback.toString().indexOf("@") + 1);
		List arrayListWithConstructor = (ArrayList) env.findObjectInEnvironment(REnvironment.R_JAVA_OBJECT_HASHCODE_PREFIX + hashCode).get(0).value;
		Assert.assertEquals("Testing if the string is the expected one", "helloworld2!", arrayListWithConstructor.get(0).toString()); 
		System.out.println("TCP Server implementation with multiple requests successfully tested!");
		client.close();
//		int u = 0;
//		server.requestShutdown();
	}
	
}
