package com.cometproject.server.game.players.data;

import com.cometproject.api.game.players.data.IPlayerAvatar;
import com.cometproject.server.game.utilities.validator.PlayerFigureValidator;

public class PlayerAvatarData implements IPlayerAvatar {
	
	private final int id;
	private String username;
	private String figure;
	private String gender;
	private String motto;
	private int regTimestamp;
	private Object tempData = null;
	
	public PlayerAvatarData(int id, String username, String figure, String gender, String motto, int regTimestamp) {
		this.id = id;
		this.username = username;
		this.figure = figure;
		this.gender = gender;
		this.motto = motto;
		this.regTimestamp = regTimestamp;
		
		if (figure == null) {
			return;
		}
		
		if (PlayerFigureValidator.isValidFigureCode(this.figure, gender)) {
			this.figure = PlayerData.DEFAULT_FIGURE;
		}
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String getFigure() {
		return figure;
	}
	
	@Override
	public void setFigure(String figure) {
		this.figure = figure;
	}
	
	@Override
	public String getMotto() {
		return motto;
	}
	
	@Override
	public void setMotto(String motto) {
		this.motto = motto;
	}
	
	@Override
	public String getGender() {
		return this.gender;
	}
	
	@Override
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	@Override
	public void tempData(final Object data) {
		this.tempData = data;
	}
	
	@Override
	public Object tempData() {
		return this.tempData;
	}
	
	@Override
	public int getRegTimestamp() {
		return regTimestamp;
	}
	
	@Override
	public void setRegTimestamp(int regTimestamp) {
		this.regTimestamp = regTimestamp;
	}
	
}
