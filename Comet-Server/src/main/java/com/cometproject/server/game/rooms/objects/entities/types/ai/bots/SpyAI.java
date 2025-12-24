package com.cometproject.server.game.rooms.objects.entities.types.ai.bots;

import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.RoomEntityType;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.entities.types.ai.AbstractBotAI;
import com.cometproject.server.game.rooms.objects.entities.types.data.types.SpyBotData;

public class SpyAI extends AbstractBotAI {
	
	private boolean hasSaidYes = false;
	
	public SpyAI(RoomEntity entity) {
		super(entity);
	}
	
	@Override
	public boolean onPlayerEnter(PlayerEntity playerEntity) {
		if (playerEntity.getPlayerId() != this.getBotEntity().getData().getOwnerId()) {
			if (!playerEntity.getPlayer().isInvisible()) {
				if (!((SpyBotData) this.getBotEntity().getDataObject()).visitors().contains(playerEntity.getUsername())) {
					((SpyBotData) this.getBotEntity().getDataObject()).visitors().add(playerEntity.getUsername());
				}
			}
		} else {
			if (((SpyBotData) this.getBotEntity().getDataObject()).visitors().isEmpty()) {
				this.getBotEntity().say(Locale.getOrDefault("comet.game.bot.spy.noVisitors", "There have been no visitors while you've been away!!!"));
				this.hasSaidYes = true;
			} else {
				this.getBotEntity().say(Locale.getOrDefault("comet.game.bot.spy.sayYes", "Nice to see you Sir! Please say yes if you'd like me to tell who have visited room while you've been gone."));
				this.hasSaidYes = false;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean onTalk(PlayerEntity entity, String message) {
		if (this.hasSaidYes) {
			return false;
		}
		
		if (entity.getPlayerId() == this.getBotEntity().getData().getOwnerId()) {
			if (message.equals("yes") || message.equals("oui") || message.equals("sim") || message.equals("ya") || message.equals(Locale.getOrDefault("comet.game.bot.yes", "yes"))) {
				StringBuilder stillIn = new StringBuilder();
				StringBuilder left = new StringBuilder();
				
				((SpyBotData) this.getBotEntity().getDataObject()).visitors().forEach(username -> {
					boolean isLast = ((SpyBotData) this.getBotEntity().getDataObject()).visitors().indexOf(username) == (((SpyBotData) this.getBotEntity().getDataObject()).visitors().size() - 1);
					if (this.getBotEntity().getRoom().getEntities().getEntityByName(username, RoomEntityType.PLAYER) != null) {
						stillIn.append(username).append(isLast ? (stillIn.isEmpty()) ? Locale.getOrDefault("comet.game.bot.spy.stillInRoom.single", " is still in the room") : Locale.getOrDefault("comet.game.bot.spy.stillInRoom.multiple", " are still in the room") : ", ");
					} else {
						left.append(username).append(isLast ? left.toString().isEmpty() ? Locale.getOrDefault("comet.game.bot.spy.leftRoom.single", " has left") : Locale.getOrDefault("comet.game.bot.spy.leftRoom.multiple", " have left") : ", ");
					}
				});
				
				if (!left.toString().isEmpty()) {
					this.getBotEntity().say(left.toString());
				}
				
				if (!stillIn.toString().isEmpty()) {
					this.getBotEntity().say(stillIn.toString());
				}
				
				((SpyBotData) this.getBotEntity().getDataObject()).visitors().clear();
				this.getBotEntity().saveDataObject();
				this.hasSaidYes = true;
			}
		}
		return false;
	}
	
	@Override
	public boolean onPlayerLeave(PlayerEntity entity) {
		if (entity.getPlayerId() == this.getBotEntity().getData().getOwnerId()) {
			this.hasSaidYes = false;
		}
		
		return false;
	}
	
	@Override
	public boolean onAddedToRoom() {
		this.getBotEntity().say(Locale.getOrDefault("comet.game.bot.spy.addedToRoom", "Hi! Next time you enter the room, I'll let you know who visited while you were away.."));
		return false;
	}
	
}
