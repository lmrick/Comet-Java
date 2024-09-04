package com.cometproject.server.game.commands.development;

import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.network.sessions.Session;

import java.text.MessageFormat;

public class PositionCommand extends ChatCommand {
	
	private final boolean debug;
	
	public PositionCommand() {
		this.debug = false;
	}
	
	public PositionCommand(boolean debug) {
		this.debug = debug;
	}
	
	@Override
	public void execute(Session client, String[] params) {
		sendNotification(MessageFormat.format("{0}Y: {1}\r\nZ: {2}\r\nRotation: {3}\r\n", MessageFormat.format("X: {0}\r\n", client.getPlayer().getEntity().getPosition().getX()), client.getPlayer().getEntity().getPosition().getY(), client.getPlayer().getEntity().getPosition().getZ(), client.getPlayer().getEntity().getBodyRotation()), client);
	}
	
	@Override
	public String getPermission() {
		return this.debug ? "dev" : "position_command";
	}
	
	@Override
	public String getParameter() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return Locale.get("command.position.description");
	}
	
}
