package com.cometproject.server.modules.events;

import com.cometproject.api.modules.commands.CommandInfo;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.api.utilities.events.Event;
import com.cometproject.api.utilities.events.EventArgs;
import com.cometproject.api.utilities.events.IEventHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class EventHandlerService implements IEventHandler {
	private final ExecutorService asyncEventExecutor;
	private final Logger log = LogManager.getLogger(EventHandlerService.class);
	private final Map<Class<?>, List<Event<?>>> listeners;
	private final Map<String, BiConsumer<ISession, String[]>> chatCommands;
	private final Map<String, CommandInfo> commandInfo;
	
	public EventHandlerService() {
		this.asyncEventExecutor = Executors.newCachedThreadPool();
		this.listeners = Maps.newConcurrentMap();
		this.chatCommands = Maps.newConcurrentMap();
		this.commandInfo = Maps.newConcurrentMap();
	}
	
	@Override
	public void initialize() {
		if (this.listeners != null) {
			this.listeners.clear();
		}
		
		if (this.chatCommands != null) {
			this.chatCommands.clear();
		}
		
		if (this.commandInfo != null) {
			this.commandInfo.clear();
		}
	}
	
	@Override
	public void registerCommandInfo(String commandName, CommandInfo info) {
		this.commandInfo.put(commandName, info);
	}
	
	@Override
	public void registerChatCommand(String commandExecutor, BiConsumer<ISession, String[]> consumer) {
		this.chatCommands.put(commandExecutor, consumer);
	}
	
	@Override
	public void registerEvent(Event<?> consumer) {
		if (this.listeners.containsKey(consumer.getClass())) {
			this.listeners.get(consumer.getClass()).add(consumer);
		} else {
			this.listeners.put(consumer.getClass(), Lists.newArrayList(consumer));
		}
		
		log.info(String.format("Registered event listener for %s", consumer.getClass().getSimpleName()));
	}
	
	@Override
	public <T extends EventArgs> boolean handleEvent(Class<? extends Event<T>> eventClass, T args) {
		if (this.listeners.containsKey(eventClass)) {
			this.invoke(eventClass, args);
			log.info(String.format("Event handled: %s\n", eventClass.getSimpleName()));
		} else {
			log.info(String.format("Unhandled event: %s\n", eventClass.getSimpleName()));
		}
		
		return args.isCancelled();
	}
	
	@Override
	public Map<String, CommandInfo> getCommands() {
		return this.commandInfo;
	}
	
	@Override
	public boolean handleCommand(ISession session, String commandExecutor, String[] arguments) {
		if (!this.chatCommands.containsKey(commandExecutor) || !this.commandInfo.containsKey(commandExecutor)) {
			return false;
		}
		
		CommandInfo commandInfo = this.commandInfo.get(commandExecutor);
		if (!session.getPlayer().getPermissions().hasCommand(commandInfo.permission()) && !commandInfo.permission().isEmpty()) {
			return false;
		}
		
		BiConsumer<ISession, String[]> chatCommand = this.chatCommands.get(commandExecutor);
		try {
			chatCommand.accept(session, arguments);
		} catch (Exception e) {
			log.warn(MessageFormat.format("Failed to execute module command: {0}", commandExecutor));
		}
		
		return true;
	}
	
	private <T extends EventArgs> void invoke(Class<? extends Event<T>> eventArgsClass, T args) {
		var eventList = listeners.get(eventArgsClass);
		if (eventList != null) {
			eventList.forEach(event -> {
				try {
					@SuppressWarnings("unchecked") Event<T> typedEvent = (Event<T>) event;
					if (typedEvent.isAsync()) this.asyncEventExecutor.submit(() -> typedEvent.consume(args));
					else typedEvent.consume(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}
	
	
}