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
package j4r.multiprocess;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.logging.Level;

import j4r.app.AbstractGenericEngine;
import j4r.app.AbstractGenericTask;

public class JavaProcessWrapper extends AbstractGenericTask implements PropertyChangeListener {

	private static final long serialVersionUID = 20120218L;

	private JavaProcess internalProcess;
	private boolean atLeastOneMessageReceived;

	
	public JavaProcessWrapper(String taskName, List<String> commands, File workingDirectory) {
		setName(taskName);
		internalProcess = new JavaProcess(commands, workingDirectory);
		internalProcess.redirectOutputStream(false);
		internalProcess.addPropertyChangeListener(this);
	}
	
	
	public JavaProcess getInternalProcess() {return internalProcess;}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean cancellable = super.cancel(mayInterruptIfRunning);
		if (cancellable)
			internalProcess.cancel(mayInterruptIfRunning);
		return cancellable;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getSource().equals(internalProcess)) {
			String propertyName = arg0.getPropertyName();
			if (propertyName.equals("MessageReceived")) {
				if (!atLeastOneMessageReceived) {
					atLeastOneMessageReceived = true;
				}
				AbstractGenericEngine.J4RLogger.log(Level.FINER, "Message received is: " +(String) arg0.getNewValue());
			}
		}
	}


	@Override
	public void doThisJob() throws Exception {
		internalProcess.execute();
		int output = -1;
		try {
			output = internalProcess.get();
		} catch (Exception e) {
			if (isCancelled()) {
				output = 0;
			} else {
				throw e;
			}
		} 
		if (output == 0 && atLeastOneMessageReceived) {
			return;
		} else {
			throw new Exception("Process exited with value " + output);
		}
	}

	
	
}
