package com.cometproject.server.game.commands.vip;

import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.Square;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.types.EntityPathfinder;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.types.chat.emotions.ChatEmotion;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.room.avatar.TalkMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.WhisperMessageComposer;
import com.cometproject.server.network.sessions.Session;

import java.util.List;

public class PullCommand extends ChatCommand {
	
	@Override
	public void execute(Session client, String[] params) {
		if (params.length == 0) {
			sendNotification(Locale.getOrDefault("command.user.invalid", "Invalid username!"), client);
			return;
		}
		
		if (client.getPlayer().getEntity().isRoomMuted() || client.getPlayer().getEntity().getRoom().getRights().hasMute(client.getPlayer().getId())) {
			sendNotification(Locale.getOrDefault("command.user.muted", "You are muted."), client);
			return;
		}
		
		String username = params[0];
		Session pulledSession = NetworkManager.getInstance().getSessions().getByPlayerUsername(username);
		
		if (pulledSession == null) {
			sendNotification(Locale.getOrDefault("command.user.offline", "This user is offline!"), client);
			return;
		}
		
		if (pulledSession.getPlayer().getEntity() == null) {
			sendNotification(Locale.getOrDefault("command.user.notinroom", "This user is not in a room."), client);
			return;
		}
		
		if (username.equals(client.getPlayer().getData().getUsername())) {
			sendNotification(Locale.get("command.pull.playerhimself"), client);
			return;
		}
		
		Room room = client.getPlayer().getEntity().getRoom();
		PlayerEntity pulledEntity = pulledSession.getPlayer().getEntity();
		
		if (pulledEntity.isOverriden()) {
			return;
		}
		
		if (pulledEntity.getPosition().distanceTo(client.getPlayer().getEntity().getPosition()) != 2) {
			client.getPlayer().getSession().send(new WhisperMessageComposer(client.getPlayer().getEntity().getId(), Locale.getOrDefault("command.notaround", "Oops! %playername% is not near, walk to this player.").replace("%playername%", pulledEntity.getUsername()), 34));
			return;
		}
		
		Position squareInFront = client.getPlayer().getEntity().getPosition().squareInFront(client.getPlayer().getEntity().getBodyRotation());
		
		if (room.getModel().getDoorX() == squareInFront.getX() && room.getModel().getDoorY() == squareInFront.getY()) {
			return;
		}
		
		pulledEntity.setWalkingGoal(squareInFront.getX(), squareInFront.getY());
		
		List<Square> path = EntityPathfinder.getInstance().makePath(pulledEntity, pulledEntity.getWalkingGoal());
		pulledEntity.unIdle();
		
		if (pulledEntity.getWalkingPath() != null) pulledEntity.getWalkingPath().clear();
		
		pulledEntity.setWalkingPath(path);
		
		room.getEntities().broadcastMessage(new TalkMessageComposer(client.getPlayer().getEntity().getId(), Locale.get("command.pull.message").replace("%playername%", pulledEntity.getUsername()), ChatEmotion.NONE, 0));
	}
	
	@Override
	public String getPermission() {
		return "pull_command";
	}
	
	@Override
	public String getParameter() {
		return Locale.getOrDefault("command.parameter.username", "%username%");
	}
	
	@Override
	public String getDescription() {
		return Locale.get("command.pull.description");
	}
	
	@Override
	public boolean canDisable() {
		return true;
	}
	
	@Override
	public boolean isAsync() {
		return true;
	}
	
}
