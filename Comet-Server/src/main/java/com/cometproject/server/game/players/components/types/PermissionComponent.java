package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerPermissions;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.permissions.PermissionsManager;
import com.cometproject.server.game.permissions.types.CommandPermission;
import com.cometproject.server.game.permissions.types.OverrideCommandPermission;
import com.cometproject.server.game.permissions.types.Rank;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.types.Room;

public class PermissionComponent extends PlayerComponent implements IPlayerPermissions {
	
	public PermissionComponent(PlayerComponentContext componentContext) {
		super(componentContext);
	}
	
	@Override
	public Rank getRank() {
		return PermissionsManager.getInstance().getRank(this.getComponentContext().getPlayer().getData().getRank());
	}
	
	@Override
	public boolean hasCommand(String key) {
		if (this.getComponentContext().getPlayer().getData().getRank() == 255) {
			return true;
		}
		
		if (PermissionsManager.getInstance().getOverrideCommands().containsKey(key)) {
			OverrideCommandPermission permission = PermissionsManager.getInstance().getOverrideCommands().get(key);
			
			if (permission.playerId() == this.getComponentContext().getPlayer().getData().getId() && permission.enabled()) {
				return true;
			}
		}
		
		if (PermissionsManager.getInstance().getCommands().containsKey(key)) {
			CommandPermission permission = PermissionsManager.getInstance().getCommands().get(key);
			
			if (permission.minimumRank() <= this.getComponentContext().getPlayer().getData().getRank()) {
				boolean hasCommand = !permission.vipOnly() || this.getComponentContext().getPlayer().getData().isVip();
				
				if (!permission.rightsOnly()) {
					return hasCommand;
				}
				
				if (hasCommand) {
					if (this.getComponentContext().getPlayer().getEntity().getRoom().getRights().hasRights(this.getComponentContext().getPlayer().getId())) {
						return true;
					}
				}
			}
		}
		
		return key.equals("debug") && Comet.isDebugging || key.equals("dev");
	}
	
	@Override
	public void dispose() {
	
	}
	
}