package com.cometproject.server.tasks;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CometThreadFactory implements ThreadFactory {
	private final String baseName;
	private final AtomicInteger threadCounter;
	
	public CometThreadFactory(String baseNameFormat) {
		this.baseName = baseNameFormat;
		this.threadCounter = new AtomicInteger(0);
	}
	
	@Override
	public Thread newThread(Runnable runnable) {
		int threadId = this.threadCounter.incrementAndGet();
		return new Thread(runnable, String.format("Comet-%s-%s", baseName, threadId));
	}
	
}
