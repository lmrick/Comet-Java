package com.cometproject.server.game.commands.development;

import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.sessions.Session;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProcessTimesCommand extends ChatCommand {
	
	@Override
	public void execute(Session client, String[] params) {
		final String processTimesBuilder;
		
		Room room = client.getPlayer().getEntity().getRoom();
		
		if (room.getProcess().getProcessTimes() == null) {
			room.getProcess().setProcessTimes(new ArrayList<>());
			
			client.send(new AlertMessageComposer("Process times for this room are now being recorded. (Max: 30)"));
			return;
		}
		
		processTimesBuilder = room.getProcess().getProcessTimes().stream().map(processTime -> processTime + "\n").collect(Collectors.joining());
		
		client.send(new AlertMessageComposer("<b>Process Times</b><br><br>" + processTimesBuilder));
		
		room.getProcess().getProcessTimes().clear();
		room.getProcess().setProcessTimes(null);
	}
	
	@Override
	public String getPermission() {
		return "debug";
	}
	
	@Override
	public String getParameter() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public boolean isHidden() {
		return true;
	}
	
}
