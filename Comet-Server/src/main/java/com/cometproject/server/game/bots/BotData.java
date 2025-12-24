package com.cometproject.server.game.bots;

import com.cometproject.api.game.bots.BotMode;
import com.cometproject.api.game.bots.BotType;
import com.cometproject.api.game.bots.IBotData;
import com.cometproject.api.utilities.JsonUtil;
import com.cometproject.server.storage.queries.bots.RoomBotDao;
import com.cometproject.api.game.utilities.RandomUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;

public abstract class BotData implements IBotData {
	
	private final int id;
	private int chatDelay;
	private int ownerId;
	private String username;
	private String motto;
	private String figure;
	private String gender;
	private String ownerName;
	private boolean isAutomaticChat;
	private String[] messages;
	private final BotType botType;
	private BotMode mode;
	private String data;
	
	public BotData(int id, String username, String motto, String figure, String gender, String ownerName, int ownerId, String messages, boolean automaticChat, int chatDelay, BotType botType, BotMode mode, String data) {
		this.id = id;
		this.username = username;
		this.motto = motto;
		this.figure = figure;
		this.gender = gender;
		this.ownerId = ownerId;
		this.ownerName = ownerName;
		this.botType = botType;
		this.mode = mode;
		this.data = data;
		this.messages = (messages == null || messages.isEmpty()) ? new String[0] : JsonUtil.getInstance().fromJson(messages, String[].class);
		this.chatDelay = chatDelay;
		this.isAutomaticChat = automaticChat;
	}
	
	public BotData(int id, String botName, String ownerName, String botFigure, String botGender, String botMotto, BotType type) {
		this.id = id;
		this.username = botName;
		this.figure = botFigure;
		this.gender = botGender;
		this.motto = botMotto;
		this.botType = type;
	}
	
	@Override
	public JsonObject toJsonObject() {
		final JsonObject jsonObject = new JsonObject();
		final JsonArray jsonArray = new JsonArray();
		
		jsonObject.addProperty("id", this.id);
		jsonObject.addProperty("username", this.username);
		jsonObject.addProperty("motto", this.motto);
		jsonObject.addProperty("figure", this.figure);
		jsonObject.addProperty("gender", this.gender);
		jsonObject.addProperty("ownerId", this.ownerId);
		jsonObject.addProperty("ownerName", this.ownerName);
		jsonObject.addProperty("botType", this.botType.toString());
		jsonObject.addProperty("mode", this.mode.toString());
		jsonObject.addProperty("data", this.data);
		jsonObject.addProperty("chatDelay", this.chatDelay);
		jsonObject.addProperty("isAutomaticChat", this.isAutomaticChat);
		
		Arrays.stream(this.messages).forEach(jsonArray::add);
		
		jsonObject.add("messages", jsonArray);
		
		return jsonObject;
	}
	
	@Override
	public String getRandomMessage() {
		if (this.getMessages().length > 0) {
			int index = RandomUtil.getRandomInt(0, (this.getMessages().length - 1));
			
			return this.stripNonAlphanumeric(this.getMessages()[index]);
		}
		
		return "";
	}
	
	private String stripNonAlphanumeric(String msg) {
		return msg.replace("<", "").replace(">", "");
	}
	
	@Override
	public void save() {
		RoomBotDao.saveData(this);
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public void setUsername(String username) {
		this.username = this.stripNonAlphanumeric(username);
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
	public String getFigure() {
		return figure;
	}
	
	@Override
	public void setFigure(String figure) {
		this.figure = figure;
	}
	
	@Override
	public String getGender() {
		return gender;
	}
	
	@Override
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	@Override
	public int getChatDelay() {
		return this.chatDelay;
	}
	
	@Override
	public void setChatDelay(int delay) {
		this.chatDelay = delay;
	}
	
	@Override
	public String[] getMessages() {
		return this.messages;
	}
	
	@Override
	public void setMessages(String[] messages) {
		this.messages = messages;
	}
	
	@Override
	public boolean isAutomaticChat() {
		return isAutomaticChat;
	}
	
	@Override
	public void setAutomaticChat(boolean isAutomaticChat) {
		this.isAutomaticChat = isAutomaticChat;
	}
	
	@Override
	public String getOwnerName() {
		return ownerName;
	}
	
	@Override
	public int getOwnerId() {
		return ownerId;
	}
	
	@Override
	public void dispose() {
		Arrays.fill(messages, null);
	}
	
	@Override
	public BotType getBotType() {
		return botType;
	}
	
	@Override
	public BotMode getMode() {
		return mode;
	}
	
	@Override
	public void setMode(BotMode mode) {
		this.mode = mode;
	}
	
	@Override
	public String getData() {
		return data;
	}
	
	@Override
	public void setData(String data) {
		this.data = data;
	}
	
}

