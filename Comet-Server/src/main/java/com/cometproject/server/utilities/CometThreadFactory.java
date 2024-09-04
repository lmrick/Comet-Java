package com.cometproject.server.utilities;

import javax.annotation.Nonnull;
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
	public Thread newThread(@Nonnull Runnable r) {
		int threadId = this.threadCounter.incrementAndGet();
		return new Thread(r, String.format("Comet-%s-%s", baseName, threadId));
	}
	
}
