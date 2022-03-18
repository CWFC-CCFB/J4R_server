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

import java.net.InetSocketAddress;

import j4r.net.server.AbstractServer.ServerReply;
import j4r.net.server.BasicClient;


public class FakeRClient extends BasicClient {

	protected FakeRClient(InetSocketAddress socketAddress, boolean isAJavaApplication, int key) throws Exception {
		super(socketAddress, 30, isAJavaApplication);
		Object reply = processRequest(key);
		if (!reply.toString().equals(ServerReply.SecurityChecked.name())) 
			throw new Exception("Security failed!");

		reply = processRequest("\u00E9\u00E8\u00E0\u00EF\u00FB");
		if (!reply.toString().equals(ServerReply.EncodingIdentified.name()))
			throw new Exception("Encoding detection failed!");

	}
	
	protected Object sendFakeRequest() throws BasicClientException {
		Double latitude = 46d;
		Double longitude = -71d;
		Double altitude = 300d;

		String request = latitude.toString().concat(longitude.toString()).concat(altitude.toString());
		Object result = processRequest(request);
		return result;
	}	
	
	protected Object createAnArrayList() throws BasicClientException {
		String request = REnvironment.ConstructCode + REnvironment.MainSplitter + "j4r.lang.codetranslator.FakeArrayList";
		Object result = processRequest(request);
		return result;
	}
	
	protected Object sendTimeToServer() throws BasicClientException {
		long currentTime = System.currentTimeMillis();
		String request = "time".concat(((Long) currentTime).toString());
		Object result = processRequest(request);
		return result;
	}

	protected Object createAVectorWithArguments() throws BasicClientException {
		String request = REnvironment.ConstructCode + REnvironment.MainSplitter + 
				"java.util.Vector" + REnvironment.MainSplitter + "integer3";
		Object result = processRequest(request);
		return result;
	}

	protected Object addThisToArrayList(Object arrayList, String toBeAdded) throws BasicClientException {
		String arrayListRep = arrayList.toString();
		String request = REnvironment.MethodCode + REnvironment.MainSplitter + REnvironment.R_JAVA_OBJECT_HASHCODE_PREFIX + 
				arrayListRep.substring(arrayListRep.indexOf("@") + 1) + REnvironment.MainSplitter +
				"add" + REnvironment.MainSplitter + toBeAdded;
		Object result = processRequest(request);
		return result;
	}

	protected Object testThisDoubleWrapper(Object arrayList) throws BasicClientException {
		String arrayListRep = arrayList.toString();
		String request = REnvironment.MethodCode + REnvironment.MainSplitter + REnvironment.R_JAVA_OBJECT_HASHCODE_PREFIX + 
				arrayListRep.substring(arrayListRep.indexOf("@") + 1) + REnvironment.MainSplitter +
				"processThisDouble" + REnvironment.MainSplitter + "numeric0";
		Object result = processRequest(request);
		return result;
	}

	protected Object createMultipleVectorWithArguments() throws BasicClientException {
		String request = REnvironment.ConstructCode + REnvironment.MainSplitter +
				"java.util.Vector" + REnvironment.MainSplitter + 
				"integer3" + REnvironment.SubSplitter + 
				"4" + REnvironment.SubSplitter + "5";
		Object result = processRequest(request);
		return result;
	}
}
