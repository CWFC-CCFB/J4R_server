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
package j4r.app;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * This class is the basic class for all the GenericTask classes. It extends the SwingWorker class. The
 * result of the task as whether or not it was correctly terminated is set during the done() method.
 * NOTE: The super methods get(), and cancel(boolean) are not recommended. The user should use the methods
 * isCorrectlyTerminated() and cancel() instead.
 * @author Mathieu Fortin - November 2011
 */
public abstract class AbstractGenericTask implements Serializable, Runnable, Future<Boolean> {

	private static final long serialVersionUID = 20111219L;
	
	private boolean correctlyTerminated;
	private Exception failureReason;
	private String name;

	private boolean isStarted = false;
	private boolean isOver = false; 
	private final Object lock = new Object();
	
	/**
	 * Member isCancelled should be used in the doThisJob() method to ensure a proper cancellation.
	 */
	protected boolean isCancelled;

	/**
	 * Empty constructor.
	 */
	protected AbstractGenericTask() {}

	public final boolean isCorrectlyTerminated() {
		return get();
	}
	
	/**
	 * This methods sets the correctlyTerminated member.
	 * @param correctlyTerminated a boolean
	 */
	protected final void setCorrectlyTerminated(boolean correctlyTerminated) {this.correctlyTerminated = correctlyTerminated;}

	public Exception getFailureReason() {return failureReason;}
	
	/**
	 * This method sets the failureReason member. 
	 * @param failureReason an Exception instance
	 */
	protected void setFailureReason(Exception failureReason) {this.failureReason = failureReason;}

	/**
	 * This method sets the name of the task.
	 * @param name a String
	 */
	protected void setName(String name) {this.name = name;}
	
	public String getName() {return name;}
	
	@Override
	public String toString() {
		if (getName() != null && !getName().isEmpty()) {
			return getName();
		} else {
			return super.toString();
		}
	}
	
	
	@Override
	public final void run() {
		try {
			if (!isCancelled) {
				isStarted = true;
				doThisJob();
			}
			setCorrectlyTerminated(true);
		} catch (Exception e) {
			setFailureReason(e);
			setCorrectlyTerminated(false);
		}
		synchronized(lock) {
			isOver = true;
			lock.notifyAll();
		}
	}
	
	/**
	 * This method is the inner part of the workerdoInBackground method. 
	 * It should be defined in derived classes.
	 * @throws Exception 
	 */
	protected abstract void doThisJob() throws Exception;


	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (isOver) {
			return false;
		} else if (!isStarted) {
			isCancelled = true;
			return true;
		} else if (isStarted && mayInterruptIfRunning) {
			isCancelled = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	
	@Override
	public final boolean isDone() {
		return isOver || isCancelled;
	}
		
	@Override
	public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		long timeoutInMillisec = TimeUnit.MILLISECONDS.convert(timeout, unit);
		synchronized(lock) {
			while (!isOver) {
				lock.wait(timeoutInMillisec);
			}
		}
		return correctlyTerminated;
	}

	@Override
	public Boolean get() {
		try {
			return get(0, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			setFailureReason(e);
			setCorrectlyTerminated(false);
			return correctlyTerminated;
		}
	}
	

}
