package com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.types.BotEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.events.WiredItemEvent;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.types.chat.emotions.ChatEmotion;
import com.cometproject.server.network.messages.outgoing.room.avatar.ShoutMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.TalkMessageComposer;

public class WiredActionBotTalk extends WiredActionItem {
	
	public static final int PARAM_MESSAGE_TYPE = 0;
	
	public WiredActionBotTalk(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public boolean requiresPlayer() {
		return false;
	}
	
	@Override
	public int getInterface() {
		return 23;
	}
	
	@Override
	public void onEventComplete(WiredItemEvent event) {
		if (!this.getWiredData().getText().contains("\t")) {
			return;
		}
		
		if (!(event.entity instanceof PlayerEntity)) {
			return;
		}
		
		final String[] talkData = this.getWiredData().getText().split("\t");
		
		if (talkData.length != 2) {
			return;
		}
		
		final String botName = talkData[0];
		String message = talkData[1];
		
		if (botName.isEmpty() || message.isEmpty()) {
			return;
		}
		
		message = message.replace("%username%", event.entity.getUsername());
		
		message = message.replace("<", "").replace(">", "");
		
		final BotEntity botEntity = this.getRoom().getBots().getBotByName(botName);
		
		if (botEntity != null) {
			boolean isShout = (this.getWiredData().getParams().size() == 1 && (this.getWiredData().getParams().get(PARAM_MESSAGE_TYPE) == 1));
			
			this.getRoom().getEntities().broadcastMessage(isShout ? new ShoutMessageComposer(botEntity.getId(), message, ChatEmotion.NONE, 2) : new TalkMessageComposer(botEntity.getId(), message, ChatEmotion.NONE, 2));
		}
	}
	
}