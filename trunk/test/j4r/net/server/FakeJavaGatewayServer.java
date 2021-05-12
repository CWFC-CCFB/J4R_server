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

import java.nio.charset.Charset;
import java.util.ArrayList;

public class FakeJavaGatewayServer {

	public static void main(String[] args) throws Exception {
		String myString = "Cha�ne avec des caract�res accentu�s";
		System.out.println(myString);
//		myString = new String(myString.getBytes(), Charset.forName("UTF-8"));
		System.out.println(myString);
		Charset defaultCharset = Charset.defaultCharset();
		System.out.println("Default charset is " + defaultCharset.toString());
		ArrayList myArrayList = new ArrayList();
		myArrayList.add(myString);
		ServerConfiguration servConf = new ServerConfiguration(1, 10, new int[] {18000,18001}, new int[] {50000,50001}, 212);
		JavaGatewayServer server = new JavaGatewayServer(servConf, myArrayList);
		server.startApplication();
//		server.requestShutdown();
//		int u = 0;
	}
	
}
