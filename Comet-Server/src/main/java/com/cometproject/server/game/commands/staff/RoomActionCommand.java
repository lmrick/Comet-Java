package com.cometproject.server.game.commands.staff;

import com.cometproject.api.game.bots.BotMode;
import com.cometproject.api.game.bots.BotType;
import com.cometproject.api.game.rooms.entities.RoomEntityStatus;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.bots.BotData;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.effects.PlayerEffect;
import com.cometproject.server.game.rooms.objects.entities.types.BotEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.entities.types.data.PlayerBotData;
import com.cometproject.server.network.messages.outgoing.room.avatar.AvatarsMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.DanceMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.TalkMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.stream.IntStream;

public class RoomActionCommand extends ChatCommand {
	
	private String logDesc = "";
	
	@Override
	public void execute(Session client, String[] params) {
		if (params.length < 1) {
			return;
		}
		
		final String action = params[0];
		
		switch (action) {
			case "list" -> sendAlert("- effect %effectid% \n- say %msg% \n- dance %danceid%\n- sign %signid%\n- bots %count% <i>(To remove, say \"minions leave\")</i>\n- handitem %itemid%", client);
			case "effect" -> {
				if (!StringUtils.isNumeric(params[1])) return;
				int effectId = Integer.parseInt(params[1]);
				client.getPlayer().getEntity().getRoom().getEntities().getPlayerEntities().forEach(playerEntity -> playerEntity.applyEffect(new PlayerEffect(effectId, 0)));
			}
			case "say" -> {
				String msg = this.merge(params, 1);
				client.getPlayer().getEntity().getRoom().getEntities().getPlayerEntities().forEach(playerEntity -> playerEntity.getRoom().getEntities().broadcastMessage(new TalkMessageComposer(playerEntity.getId(), msg, RoomManager.getInstance().getEmotions().getEmotion(msg), 0)));
			}
			case "dance" -> {
				if (!StringUtils.isNumeric(params[1])) return;
				int danceId = Integer.parseInt(params[1]);
				client.getPlayer().getEntity().getRoom().getEntities().getPlayerEntities().forEach(playerEntity -> {
					playerEntity.setDanceId(danceId);
					playerEntity.getRoom().getEntities().broadcastMessage(new DanceMessageComposer(playerEntity.getId(), danceId));
				});
			}
			case "sign" -> {
				if (!StringUtils.isNumeric(params[1])) return;
				client.getPlayer().getEntity().getRoom().getEntities().getPlayerEntities().forEach(playerEntity -> {
					playerEntity.addStatus(RoomEntityStatus.SIGN, String.valueOf(params[1]));
					playerEntity.markDisplayingSign();
					playerEntity.markNeedsUpdate();
				});
			}
			case "bots" -> {
				if (!StringUtils.isNumeric(params[1])) {
					return;
				}
				
				int count = Integer.parseInt(params[1]);
				final Position entityPosition = client.getPlayer().getEntity().getPosition();
				
				if (count > 1000) {
					count = 1000;
				} else if (count < 0) {
					count = 1;
				}
				
				List<RoomEntity> addedEntities = Lists.newArrayList();
				
				IntStream.range(0, count).forEachOrdered(i -> {
					final int id = -(i + 1);
					final String username = client.getPlayer().getData().getUsername() + "Minion" + i;
					final String motto = "";
					BotData botData = new PlayerBotData(id, username, motto, client.getPlayer().getData().getFigure(), client.getPlayer().getData().getGender(), client.getPlayer().getData().getUsername(), client.getPlayer().getId(), "[]", true, 7, BotType.MIMIC, BotMode.DEFAULT, "");
					final BotEntity botEntity = client.getPlayer().getEntity().getRoom().getBots().addBot(botData, entityPosition.getX(), entityPosition.getY(), entityPosition.getZ());
					if (botEntity != null) {
						addedEntities.add(botEntity);
					}
				});
				
				client.getPlayer().getEntity().getRoom().getEntities().broadcastMessage(new AvatarsMessageComposer(addedEntities));
			}
			case "handitem" -> {
				int handItem = Integer.parseInt(params[1]);
				client.getPlayer().getEntity().getRoom().getEntities().getPlayerEntities().forEach(playerEntity -> playerEntity.carryItem(handItem, false));
			}
		}
		
		this.logDesc = "El staff %s ha hecho roomaction en la sala '%b' con el parámetro %p".replace("%s", client.getPlayer().getData().getUsername()).replace("%b", client.getPlayer().getEntity().getRoom().getData().getName()).replace("%p", action);
	}
	
	@Override
	public String getPermission() {
		return "roomaction_command";
	}
	
	@Override
	public String getParameter() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return Locale.get("command.roomaction.description");
	}
	
	@Override
	public String getLoggableDescription() {
		return this.logDesc;
	}
	
	@Override
	public boolean isLoggable() {
		return true;
	}
	
}
