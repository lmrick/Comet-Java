package com.cometproject.server.game.rooms.objects.items.types.floor.games;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.games.GameType;
import com.cometproject.server.game.rooms.types.components.games.RoomGame;
import org.apache.commons.lang.StringUtils;

public abstract class GameTimerFloorItem extends RoomItemFloor {
	
	private String lastTime;
	
	public GameTimerFloorItem(RoomItemData itemData, Room room) {
		super(itemData, room);
	}
	
	@Override
	public boolean onInteract(RoomEntity entity, int requestData, boolean isWiredTriggered) {
		if (this.interactionBlocked(entity, isWiredTriggered)) return false;
		
		
		final RoomGame currentGame = this.getRoom().getGame().getInstance();
		if (this.getItemData().getData().equals("0") || (requestData == 2 && (currentGame == null || !currentGame.isActive()))) {
			int time = 0;
			
			if (currentGame != null && !currentGame.isActive()) {
				this.resetGame();
			}
			
			if (!this.getItemData().getData().isEmpty() && StringUtils.isNumeric(this.getItemData().getData())) {
				time = Integer.parseInt(this.getItemData().getData());
			}
			
			time = switch (time) {
				case 0 -> 30;
				case 30 -> 60;
				case 60 -> 120;
				case 120 -> 180;
				case 180 -> 300;
				case 300 -> 600;
				case 600 -> 1200;
				case 1200 -> 1800;
				default -> 0;
			};
			
			this.getItemData().setData(time + "");
			this.sendUpdate();
			this.saveData();
		} else {
			int gameLength = Integer.parseInt(this.getItemData().getData());
			
			this.lastTime = this.getItemData().getData();
			
			if (gameLength == 0) return true;
			
			if (currentGame == null) {
				this.getRoom().getGame().createNew();
				this.getRoom().getGame().getInstance().startTimer(gameLength);
			} else {
				if (requestData == 1) {
					currentGame.setActive(!currentGame.isActive());
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void onPickup() {
		this.resetGame();
	}
	
	private void resetGame() {
		if (this.getRoom().getGame().getInstance() != null) {
			this.getRoom().getGame().getInstance().onGameEnds();
			this.getRoom().getGame().stop();
		}
	}
	
}
