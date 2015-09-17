/**
 * 
 */
package com.alibaba.middleware.race.mom.file;

import java.io.Serializable;

import com.alibaba.middleware.race.mom.model.SendTask;

/**
 * @author showki
 *
 */
public class LogTask implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SendTask task;
	/* 
	 * 0 means this message have been received, 
	 * 1 means this message have sent to client successfully
	 */
	private int status;
	
	public LogTask(SendTask task, int status) {
		this.task = task;
		this.status = status;
	}
	/**
	 * @return the task
	 */
	public SendTask getTask() {
		return task;
	}
	/**
	 * @param task the task to set
	 */
	public void setTask(SendTask task) {
		this.task = task;
	}
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	
}
