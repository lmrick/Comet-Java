package com.cometproject.server.game.rooms.objects.items.types.floor.boutique;

import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.types.Room;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MannequinFloorItem extends RoomItemFloor {
	
	private String name = "New Mannequin";
	private String figure = "ch-210-62.lg-270-62";
	private String gender = "m";
	
	public MannequinFloorItem(RoomItemData roomItemData, Room room) {
		super(roomItemData, room);
		
		if (!this.getItemData().getData().isEmpty()) {
			String[] splitData = this.getItemData().getData().split(";#;");
			if (splitData.length != 3) return;
			
			this.name = splitData[0];
			this.figure = splitData[1];
			this.gender = splitData[2];
			
			String[] figureParts = this.figure.split("\\.");
			StringBuilder finalFigure = new StringBuilder();
			
			Arrays.stream(figureParts).filter(figurePart -> !figurePart.contains("hr") && !figurePart.contains("hd") && !figurePart.contains("he") && !figurePart.contains("ha")).forEachOrdered(figurePart -> finalFigure.append(figurePart).append("."));
			
			this.figure = finalFigure.substring(0, finalFigure.length() - 1);
		}
	}
	
	public void composeItemData(IComposerDataWrapper msg) {
		msg.writeInt(0);
		msg.writeInt(1);
		msg.writeInt(3);
		
		msg.writeString("GENDER");
		msg.writeString(this.getGender());
		msg.writeString("FIGURE");
		msg.writeString(this.getFigure());
		msg.writeString("OUTFIT_NAME");
		msg.writeString(this.getName());
	}
	
	@Override
	public boolean onInteract(RoomEntity entity, int requestData, boolean isWiredTrigger) {
		if (isWiredTrigger || !(entity instanceof PlayerEntity playerEntity)) return isWiredTrigger;
		
		if (this.name == null || this.gender == null || this.figure == null) return false;
		
		if (!this.gender.equals(playerEntity.getGender())) return false;
		
		String newFigure = Arrays.stream(playerEntity.getFigure().split("\\.")).filter(playerFigurePart -> !playerFigurePart.startsWith("ch") && !playerFigurePart.startsWith("lg")).map(playerFigurePart -> playerFigurePart + ".").collect(Collectors.joining());
		String newFigureParts = "";
		
		switch (playerEntity.getGender().toUpperCase()) {
			case "M", "F" -> {
				if (this.figure.isEmpty()) return false;
				newFigureParts = this.figure;
			}
		}
		
		for (String newFigurePart : newFigureParts.split("\\.")) {
			if (newFigurePart.startsWith("hd")) newFigureParts = newFigureParts.replace(newFigurePart, "");
		}
		
		if (newFigureParts.isEmpty()) return false;
		
		final String figure = newFigure + newFigureParts;
		
		if (figure.length() > 512) return false;
		
		playerEntity.getPlayer().getData().setFigure(figure);
		playerEntity.getPlayer().getData().setGender(this.gender);
		
		playerEntity.getPlayer().getData().save();
		playerEntity.getPlayer().poof();
		
		return true;
	}
	
	@Override
	public String getDataObject() {
		return MessageFormat.format("{0};#;{1};#;{2}", this.name, this.figure, this.gender);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFigure() {
		return figure;
	}
	
	public void setFigure(String figure) {
		this.figure = figure;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
}

