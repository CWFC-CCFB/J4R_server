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
package j4r.net;

import java.net.BindException;

/**
 * A simple extension of the BindException class. <br>
 * <br>
 * It automatically reports the port number that is already binded.
 * @author Mathieu Fortin - April 2023
 */
@SuppressWarnings("serial")
public class PortBindingException extends BindException {

	final int port;
	
	public PortBindingException(int port) {
		this.port = port;
	}
	
	@Override
	public String getMessage() {
		return "Port " + port + " is already binded.";
	}
	
}
