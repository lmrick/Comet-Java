package com.cometproject.server.network.messages;

import com.cometproject.server.boot.Comet;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.tasks.MessageEventTask;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.headers.Events;
import com.cometproject.server.protocol.messages.MessageEvent;
import java.text.MessageFormat;

public class MessagingHandler {
	
	private final MessageHandler messageHandler;
	
	public MessagingHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}
	
	public void handle(MessageEvent message, Session client) {
		
		final Short header = message.getId();
		
		if (Comet.isDebugging) {
			MessageHandler.log.debug(message.toString());
		}
		
		if (!Comet.isRunning) return;
		
		if (this.messageHandler.getMessages().containsKey(header)) {
			try {
				final Event event = this.messageHandler.getMessages().get(header);
				
				if (event != null) {
					if (this.messageHandler.isAsyncEventExecution()) {
						this.messageHandler.getEventExecutor().submit(new MessageEventTask(event, client, message));
					} else {
						final long start = System.currentTimeMillis();
						MessageHandler.log.info(MessageFormat.format("Started packet process for packet: [{0}][{1}]", event.getClass().getSimpleName(), header));
						
						event.handle(client, message);
						
						long timeTakenSinceCreation = ((System.currentTimeMillis() - start));
						if (timeTakenSinceCreation >= 100) {
							MessageHandler.log.info(client.getPlayer() != null && client.getPlayer().getData() != null ? MessageFormat.format("[{0}][{1}][{2}][{3}] Packet took {4}ms to execute", event.getClass().getSimpleName(), message.getId(), client.getPlayer().getId(), client.getPlayer().getData().getUsername(), timeTakenSinceCreation) : "[" + event.getClass().getSimpleName() + "][" + message.getId() + "] Packet took " + timeTakenSinceCreation + "ms to execute");
						}
						
						MessageHandler.log.info(MessageFormat.format("Finished packet process for packet: [{0}][{1}] in {2}ms", event.getClass().getSimpleName(), header, System.currentTimeMillis() - start));
					}
				}
			} catch (Exception e) {
				if (client.getLogger() != null) {
					client.getLogger().error(MessageFormat.format("Error while handling event: {0}", this.messageHandler.getMessages().get(header).getClass().getSimpleName()), e);
				} else {
					MessageHandler.log.error(MessageFormat.format("Error while handling event: {0}", this.messageHandler.getMessages().get(header).getClass().getSimpleName()), e);
				}
			}
		} else if (Comet.isDebugging) {
			MessageHandler.log.info(MessageFormat.format("Unhandled message: {0} / {1}", Events.valueOfId(header), header));
		}
	}
	
}
