package com.cometproject.server.game.commands.staff;

import com.cometproject.api.game.players.data.IPlayerAvatar;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.network.ws.messages.youtube.YouTubeVideoMessage;

public class HotelVideoCommand extends ChatCommand {
	
	@Override
	public void execute(Session client, String[] params) {
		if (params.length < 1) {
			return;
		}
		
		final String videoId = params[0];
		final String message = this.merge(params, 1);
		final IPlayerAvatar avatar = client.getPlayer().getData();
		
		NetworkManager.getInstance().getSessions().broadcastWs(new YouTubeVideoMessage(videoId, message, avatar.getUsername(), avatar.getFigure()));
	}
	
	@Override
	public String getPermission() {
		return "hotelvideo_command";
	}
	
	@Override
	public String getParameter() {
		return "%video% %msg%";
	}
	
	@Override
	public String getDescription() {
		return Locale.get("command.hotelvideo.description");
	}
	
}
