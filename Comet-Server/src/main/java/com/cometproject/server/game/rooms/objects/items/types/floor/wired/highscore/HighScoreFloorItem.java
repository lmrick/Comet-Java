package com.cometproject.server.game.rooms.objects.items.types.floor.wired.highscore;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.api.utilities.JsonUtil;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.score.ScoreboardClearType;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.data.score.ScoreboardItemData;
import com.cometproject.server.game.rooms.types.Room;
import com.google.common.collect.Lists;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class HighScoreFloorItem extends RoomItemFloor {
	
	private final ScoreboardClearType clearType;
	private final ScoreboardItemData itemData;
	private boolean state;
	
	HighScoreFloorItem(RoomItemData roomItemData, Room room) {
		super(roomItemData, room);
		
		final String data = roomItemData.getData();
		this.clearType = ScoreboardClearType.getByFurniType(Integer.parseInt(this.getDefinition().getItemName().split("\\*")[1]));
		
		ScoreboardItemData scoreboardItemData = null;
		try {
			if (roomItemData.getData().startsWith("1{") || roomItemData.getData().startsWith("0{")) {
				this.state = data.startsWith("1");
				scoreboardItemData = JsonUtil.getInstance().fromJson(data.substring(1), ScoreboardItemData.class);
			} else {
				this.state = false;
				scoreboardItemData = new ScoreboardItemData(Comet.getTime(), Lists.newCopyOnWriteArrayList());
			}
		} catch (Exception e) {
			this.state = false;
			scoreboardItemData = new ScoreboardItemData(Comet.getTime(), Lists.newCopyOnWriteArrayList());
		}
		
		this.itemData = scoreboardItemData;
		this.clear(false);
	}

	public abstract void onTeamWins(List<String> users, int score);
	public abstract int getScoreType();
	
	public void clear(boolean sendUpdate) {
		boolean cleared = false;
		
		final Date currentDate = new Date();
		final Calendar currentCalendar = Calendar.getInstance();
		currentCalendar.setTime(currentDate);
		
		final Date date = new Date(this.itemData.getLastClearTimestamp());
		final Calendar clearedCalendar = Calendar.getInstance();
		clearedCalendar.setTime(date);
		
		switch (this.clearType) {
			case ALL_TIME -> {
			
			}
			
			case DAILY -> {
				if (currentCalendar.get(Calendar.DAY_OF_MONTH) != clearedCalendar.get(Calendar.DAY_OF_MONTH)) {
					this.getScoreData().clear();
					cleared = true;
				}
			}
			
			case WEEKLY -> {
				if (currentCalendar.get(Calendar.WEEK_OF_YEAR) != currentCalendar.get(Calendar.WEEK_OF_YEAR)) {
					this.getScoreData().clear();
					cleared = true;
				}
			}
			
			case MONTHLY -> {
				if (currentCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)) {
					this.getScoreData().clear();
					cleared = true;
				}
			}
		}
		
		if (sendUpdate && cleared) {
			this.sendUpdate();
		}
		
		if (cleared) {
			this.saveData();
		}
	}
	
	@Override
	public void composeItemData(IComposerDataWrapper msg) {
		msg.writeInt(0);
		this.composeHighScoreData(msg);
	}
	
	@Override
	public boolean onInteract(RoomEntity entity, int requestData, boolean isWiredTrigger) {
		if (!isWiredTrigger) {
			if (!(entity instanceof PlayerEntity playerEntity)) {
				return false;
			}
			
			if (!playerEntity.getRoom().getRights().hasRights(playerEntity.getPlayerId()) && !playerEntity.getPlayer().getPermissions().getRank().roomFullControl()) {
				return false;
			}
		}
		
		this.state = !this.state;
		
		this.sendUpdate();
		this.saveData();
		return true;
	}
	
	@Override
	public String getDataObject() {
		return (this.state ? "1" : "0") + JsonUtil.getInstance().toJson(this.itemData);
	}
	
	void addEntry(List<String> users, int score) {
		addEntry(users, score, false, false);
	}
	
	void addEntry(List<String> users, int score, boolean updateExisting, boolean increaseExisting) {
		this.itemData.addEntry(new ScoreboardItemData.HighScoreEntry(users, score), updateExisting, increaseExisting);
		this.update();
	}
	
	public void update() {
		this.sendUpdate();
		this.saveData();
	}
	
	ScoreboardItemData getScoreData() {
		return itemData;
	}
	
	private ScoreboardClearType getClearType() {
		return this.clearType;
	}
	
	private void composeHighScoreData(IComposerDataWrapper msg) {
		msg.writeInt(6);
		
		msg.writeString(this.state ? "1" : "0");
		msg.writeInt(this.getScoreType());
		msg.writeInt(this.getClearType().getClearTypeId());
		
		final List<ScoreboardItemData.HighScoreEntry> highScores = this.getScoreData().getTopScores();
		msg.writeInt(highScores.size());
		
		highScores.forEach(entry -> {
			msg.writeInt(entry.getScore());
			msg.writeInt(entry.getUsers().size());
			entry.getUsers().forEach(msg::writeString);
		});
	}
	
}
