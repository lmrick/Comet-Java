package com.cometproject.server.game.commands.development;

import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.rooms.objects.items.RoomItem;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.sessions.Session;

import java.text.MessageFormat;

public class ItemVirtualIdCommand extends ChatCommand {
	
	@Override
	public void execute(Session client, String[] params) {
		if (params.length == 0) {
			client.send(new AlertMessageComposer(MessageFormat.format("There are currently {0} item virtual IDs in memory.", ItemManager.getInstance().getItemIdToVirtualIds().size())));
			return;
		}
		
		try {
			final int virtualId = Integer.parseInt(params[0]);
			final RoomItem roomItem = client.getPlayer().getEntity().getRoom().getItems().getFloorItem(virtualId);
			
			client.send(new AlertMessageComposer(MessageFormat.format("Virtual ID: {0}\nReal ID: {1}{2}", virtualId, ItemManager.getInstance().getItemIdByVirtualId(virtualId), roomItem != null ? "\nBase ID: " + roomItem.getDefinition().getId() : "")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getPermission() {
		return "dev";
	}
	
	@Override
	public String getParameter() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return null;
	}
	
	@Override
	public boolean isHidden() {
		return true;
	}
	
}
