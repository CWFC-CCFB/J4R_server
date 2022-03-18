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

import java.net.InetAddress;
import java.util.Map;

import j4r.net.server.JavaGatewayServer;
import j4r.net.server.ServerConfiguration;

class FakeJavaGatewayServer extends JavaGatewayServer {

	
	FakeJavaGatewayServer(ServerConfiguration servConf, Object mainInstance) throws Exception {
		super(servConf, mainInstance);
	}

	Map<InetAddress, REnvironment> getEnvironmentMap() {
		return translators;
	}
	
}
