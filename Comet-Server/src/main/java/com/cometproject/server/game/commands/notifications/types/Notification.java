package com.cometproject.server.game.commands.notifications.types;

import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Notification {
	private final String trigger;
	private final String text;
	private final NotificationType type;
	private final int minRank;
	private final int coolDown;
	
	public Notification(ResultSet data) throws SQLException {
		this.trigger = data.getString("name");
		this.text = data.getString("text");
		this.type = NotificationType.valueOf(data.getString("type").toUpperCase());
		this.minRank = data.getInt("min_rank");
		this.coolDown = data.getInt("cooldown");
	}
	
	public void execute(Player player) {
		if ((player.getNotificationDelay() + coolDown) >= Comet.getTime()) {
			return;
		}
		
		switch (this.type) {
			case GLOBAL -> NetworkManager.getInstance().getSessions().broadcast(new AdvancedAlertMessageComposer(this.text + "\n\n-" + player.getData().getUsername()));
			case LOCAL -> player.getSession().send(new AdvancedAlertMessageComposer(this.text));
		}
		
		player.setNotificationDelay((int) Comet.getTime());
	}
	
	public String getTrigger() {
		return trigger;
	}
	
	public String getText() {
		return text;
	}
	
	public NotificationType getType() {
		return type;
	}
	
	public int getMinRank() {
		return minRank;
	}
	
	public int getCoolDown() {
		return coolDown;
	}
	
}
