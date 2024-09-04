package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions.custom;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.network.messages.outgoing.room.avatar.WhisperMessageComposer;

public class WiredCustomFastWalk extends WiredActionItem {
	
	public WiredCustomFastWalk(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public boolean requiresPlayer() {
		return true;
	}
	
	@Override
	public int getInterface() {
		return 1;
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		if (!(event.entity instanceof PlayerEntity playerEntity)) {
			return;
		}
		
		if (playerEntity.getPlayer() == null || playerEntity.getPlayer().getSession() == null) {
			return;
		}
		
		playerEntity.toggleFastWalk();
		playerEntity.getPlayer().getSession().send(playerEntity.isFastWalkEnabled() ? new WhisperMessageComposer(playerEntity.getId(), Locale.getOrDefault("wired.custom.fastwalk.enabled", "Now you have fastwalk activated!"), 0) : new WhisperMessageComposer(playerEntity.getId(), Locale.getOrDefault("wired.custom.fastwalk.disabled", "Now you have fastwalk deactivated!"), 0));
	}
	
}
