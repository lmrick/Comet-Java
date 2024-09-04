package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions.custom;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.effects.PlayerEffect;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import org.apache.commons.lang.StringUtils;

public class WiredCustomEnable extends WiredActionItem {
	
	public WiredCustomEnable(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public boolean requiresPlayer() {
		return true;
	}
	
	@Override
	public int getInterface() {
		return 7;
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		if (!(event.entity instanceof PlayerEntity playerEntity)) {
			return;
		}
		
		if (playerEntity.getPlayer() == null || playerEntity.getPlayer().getSession() == null) {
			return;
		}
		
		if (this.getWiredData() == null || this.getWiredData().getText() == null) {
			return;
		}
		
		if (!StringUtils.isNumeric(this.getWiredData().getText())) {
			return;
		}
		
		int enableId = Integer.parseInt(this.getWiredData().getText());
		playerEntity.applyEffect(playerEntity.getCurrentEffect() != null ? playerEntity.getCurrentEffect().getEffectId() == enableId ? new PlayerEffect(0, 0) : new PlayerEffect(enableId, 0) : new PlayerEffect(enableId, 0));
	}
	
}
