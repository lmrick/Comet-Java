package com.cometproject.server.tasks;

import javax.annotation.Nonnull;
import java.text.MessageFormat;

public class CometThread extends Thread {
	
	public CometThread(Runnable runnable) {
		super(runnable, "Comet Thread");
	}
	
	public CometThread(Runnable runnable, String identifier) {
		super(runnable, MessageFormat.format("Comet Thread [{0}]", identifier));
	}
	
	public CometThread(ICometTask task) {
		super(task, "Comet Task");
	}
	
	public CometThread(ICometTask task, String identifier) {
		super(task, MessageFormat.format("Comet Task [{0}]", identifier));
	}
	
	@Override
	public void start() {
		if (this.isRunning()) {
			return;
		}
		
		super.start();
	}
	
	@Override
	@Nonnull
	public State getState() {
		return super.getState();
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
	
	public synchronized boolean isRunning() {
		return super.isAlive();
	}
	
}
