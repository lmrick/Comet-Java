package com.cometproject.server.network.messages.tasks;

import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;
import com.cometproject.server.tasks.ICometTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class MessageEventTask implements ICometTask {
	
	private static final Logger log = LogManager.getLogger(MessageEventTask.class.getName());
	private final Event messageEvent;
	private final Session session;
	private final MessageEvent messageEventData;
	private final long start;
	
	public MessageEventTask(Event messageEvent, Session session, MessageEvent messageEventData) {
		this.messageEvent = messageEvent;
		this.session = session;
		this.messageEventData = messageEventData;
		this.start = System.currentTimeMillis();
	}
	
	@Override
	public void run() {
		try {
			log.debug("Started packet process for packet: [{}][{}]", this.messageEvent.getClass().getSimpleName(), messageEventData.getId());
			this.messageEvent.handle(this.session, this.messageEventData);
			
			long timeTakenSinceCreation = ((System.currentTimeMillis() - this.start));
			
			if (timeTakenSinceCreation >= 100) {
				if (session.getPlayer() != null && session.getPlayer().getData() != null)
					log.trace("[{}][{}][{}][{}] Packet took {}MS to execute", this.messageEvent.getClass().getSimpleName(), messageEventData.getId(), session.getPlayer().getId(), session.getPlayer().getData().getUsername(), timeTakenSinceCreation);
				else
					log.trace("[{}][{}] Packet took {}MS to execute", this.messageEvent.getClass().getSimpleName(), messageEventData.getId(), timeTakenSinceCreation);
			}
			log.debug("Finished packet process for packet: [{}][{}] in {}MS", this.messageEvent.getClass().getSimpleName(), messageEventData.getId(), timeTakenSinceCreation);
			
		} catch (Exception e) {
			if (this.session.getLog() != null)
				session.getLog().error(MessageFormat.format("Error while handling event: {0}", this.messageEvent.getClass().getSimpleName()), e);
			else log.error("Error while handling event: {}", this.messageEvent.getClass().getSimpleName(), e);
		}
	}
	
}
