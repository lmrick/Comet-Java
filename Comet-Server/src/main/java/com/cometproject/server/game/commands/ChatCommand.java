package com.cometproject.server.game.commands;

import com.cometproject.server.locale.Locale;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.NotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.WhisperMessageComposer;
import com.cometproject.server.network.sessions.Session;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class ChatCommand {
	
	public static void sendNotification(String msg, Session session) {
		session.send(new NotificationMessageComposer("generic", msg));
	}
	
	public static void sendAlert(String msg, Session session) {
		session.send(new AlertMessageComposer(msg));
	}
	
	public static void sendWhisper(String msg, Session session) {
		session.send(new WhisperMessageComposer(session.getPlayer().getEntity().getId(), msg));
	}
	
	public static void isExecuted(Session session) {
		session.send(new NotificationMessageComposer("up", Locale.getOrDefault("command.executed", "Command was executed successfully!")));
	}
	
	public abstract void execute(Session client, String[] params);
	public abstract String getPermission();
	public abstract String getParameter();
	public abstract String getDescription();
	
	public final String merge(String[] params) {
		final StringBuilder stringBuilder = new StringBuilder();
		
		Arrays.stream(params).forEachOrdered(s -> {
			if (!params[params.length - 1].equals(s)) stringBuilder.append(s).append(" ");
			else stringBuilder.append(s);
		});
		
		return stringBuilder.toString();
	}
	
	public String merge(String[] params, int begin) {
		return IntStream.range(0, params.length).filter(i -> i >= begin).mapToObj(i -> params[i] + " ").collect(Collectors.joining());
	}
	
	public String getLoggableDescription() {
		return getDescription();
	}
	
	public boolean isLoggable() {
		return false;
	}
	
	public boolean isHidden() {
		return false;
	}
	
	public boolean canDisable() {
		return false;
	}
	
	public boolean isAsync() {
		return false;
	}
	
	public boolean bypassFilter() {
		return false;
	}
	
	public static class Execution implements Runnable {
		private final ChatCommand command;
		private final String[] params;
		private final Session session;
		
		public Execution(ChatCommand command, String[] params, Session session) {
			this.command = command;
			this.params = params;
			this.session = session;
		}
		
		@Override
		public void run() {
			command.execute(session, params);
		}
		
	}
	
}
