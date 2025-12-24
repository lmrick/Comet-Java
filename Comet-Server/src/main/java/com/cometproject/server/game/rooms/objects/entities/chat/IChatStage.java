package com.cometproject.server.game.rooms.objects.entities.chat;

import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;

public interface IChatStage {
	
	ChatDecision handle(PlayerEntity entity, ChatContext ctx);
	
}
