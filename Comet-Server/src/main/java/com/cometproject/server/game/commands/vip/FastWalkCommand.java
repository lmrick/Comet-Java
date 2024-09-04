package com.cometproject.server.game.commands.vip;

import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.network.sessions.Session;

public class FastWalkCommand extends ChatCommand {
	
	@Override
	public void execute(Session client, String[] params) {
		client.getPlayer().getEntity().toggleFastWalk();
		
		sendWhisper(client.getPlayer().getEntity().isFastWalkEnabled() ? Locale.get("command.fastwalk.enabled") : Locale.get("command.fastwalk.disabled"), client);
	}
	
	@Override
	public String getPermission() {
		return "fastwalk_command";
	}
	
	@Override
	public String getParameter() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return Locale.get("command.fastwalk.description");
	}
	
	@Override
	public boolean canDisable() {
		return true;
	}
	
}
