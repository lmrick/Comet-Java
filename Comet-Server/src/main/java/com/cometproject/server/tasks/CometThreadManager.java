package com.cometproject.server.tasks;

import com.cometproject.api.config.Configuration;
import com.cometproject.api.utilities.Initializable;
import com.cometproject.server.game.rooms.types.components.ItemProcessComponent;
import com.cometproject.server.game.rooms.types.components.ProcessComponent;
import org.apache.log4j.Logger;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CometThreadManager implements Initializable {
	
	public static int POOL_SIZE = 0;
	private static CometThreadManager cometThreadManagerInstance;
	private ScheduledExecutorService coreExecutor;
	private ScheduledExecutorService roomProcessingExecutor;
	
	public CometThreadManager() {
	
	}
	
	public static CometThreadManager getInstance() {
		if (cometThreadManagerInstance == null) cometThreadManagerInstance = new CometThreadManager();
		return cometThreadManagerInstance;
	}
	
	@Override
	public void initialize() {
		var poolSize = Integer.parseInt((String) Configuration.currentConfig().getOrDefault("comet.system.threads", "8"));
		
		this.coreExecutor = Executors.newScheduledThreadPool(poolSize, r -> {
			POOL_SIZE++;
			
			var scheduledThread = new CometThread(r);
			scheduledThread.setName("Comet-Scheduler-Thread-" + POOL_SIZE);
			
			final var log = Logger.getLogger("Comet-Scheduler-Thread-" + POOL_SIZE);
			scheduledThread.setUncaughtExceptionHandler((t, e) -> log.error("Exception in worker thread", e));
			
			return scheduledThread;
		});
		
		final var roomProcessingPool = 8;
		final var counter = new AtomicInteger();
		
		this.roomProcessingExecutor = Executors.newScheduledThreadPool(roomProcessingPool, r -> {
			var scheduledThread = new CometThread(r);
			scheduledThread.setName("Room-Processor-" + counter.incrementAndGet());
			
			final Logger log = Logger.getLogger(scheduledThread.getName());
			scheduledThread.setUncaughtExceptionHandler((t, e) -> log.error("Exception in room worker thread", e));
			
			return scheduledThread;
		});
	}
	
	public void executeOnce(ICometTask task) {
		this.coreExecutor.submit(task);
	}
	
	public ScheduledFuture<?> executePeriodic(ICometTask task, long initialDelay, long period, TimeUnit unit) {
		if (task instanceof ProcessComponent || task instanceof ItemProcessComponent) {
			return this.roomProcessingExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
		}
		
		return this.coreExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
	}
	
	public void executeSchedule(ICometTask task, long delay, TimeUnit unit) {
		if (task instanceof ProcessComponent) {
			this.roomProcessingExecutor.schedule(task, delay, unit);
			return;
		}
		
		this.coreExecutor.schedule(task, delay, unit);
	}
	
	public ScheduledExecutorService getCoreExecutor() {
		return coreExecutor;
	}
	
}
