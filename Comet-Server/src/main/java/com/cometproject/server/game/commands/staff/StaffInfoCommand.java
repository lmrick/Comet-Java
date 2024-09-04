package com.cometproject.server.game.commands.staff;

import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.moderation.ModerationManager;
import com.cometproject.server.network.messages.outgoing.notification.MotdNotificationMessageComposer;
import com.cometproject.server.network.sessions.Session;

import java.util.stream.Collectors;

public class StaffInfoCommand extends ChatCommand {
	
	@Override
	public void execute(Session client, String[] message) {
		final String staffInfo = ModerationManager.getInstance().getModerators().stream().map(player -> "* Username: " + player.getPlayer().getData().getUsername() + " / Room: " + (player.getPlayer().getEntity() != null && player.getPlayer().getEntity().getRoom() != null ? player.getPlayer().getEntity().getRoom().getData().getName() : "none") + " / Rank: " + player.getPlayer().getData().getRank() + "\r").collect(Collectors.joining("", "Current online staffs:\r\r", ""));
		
		final MotdNotificationMessageComposer msg = new MotdNotificationMessageComposer(staffInfo);
		
		client.send(msg);
	}
	
	@Override
	public boolean isAsync() {
		return true;
	}
	
	@Override
	public String getPermission() {
		return "staffinfo_command";
	}
	
	@Override
	public String getParameter() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return Locale.get("command.staffinfo.description");
	}
	
}