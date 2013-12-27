package com.cometsrv.network.messages.incoming.handshake;

import com.cometsrv.game.GameEngine;
import com.cometsrv.game.players.data.PlayerLoader;
import com.cometsrv.game.players.types.Player;
import com.cometsrv.network.messages.incoming.IEvent;
import com.cometsrv.network.messages.outgoing.handshake.HomeRoomMessageComposer;
import com.cometsrv.network.messages.outgoing.handshake.LoginMessageComposer;
import com.cometsrv.network.messages.outgoing.misc.AdvancedAlertMessageComposer;
import com.cometsrv.network.messages.outgoing.misc.MotdNotificationComposer;
import com.cometsrv.network.messages.outgoing.moderation.ModToolMessageComposer;
import com.cometsrv.network.messages.outgoing.user.permissions.FuserightsMessageComposer;
import com.cometsrv.network.messages.types.Event;
import com.cometsrv.network.sessions.Session;

import java.util.concurrent.TimeUnit;

public class SSOTicketMessageEvent implements IEvent {
    public void handle(Session client, Event msg) {
        String ticket = msg.readString();

        if(ticket.length() < 10 || ticket.length() > 30) {
            client.disconnect();
            return;
        }

        Player player = PlayerLoader.loadPlayerBySSo(ticket);

        if(player == null) {
            client.disconnect();
            return;
        }

        if(GameEngine.getBans().hasBan(Integer.toString(player.getId()))) {
            client.send(AdvancedAlertMessageComposer.compose(
                    "You've been banned!",
                    "It seems you've been banned.<br><br><b>Reason:</b><br>" + GameEngine.getBans().get(Integer.toString(player.getId())).getReason() + "<br><br>If you feel you received this in error, please contact the system administrator."
            ));

            GameEngine.getLogger().warn("Banned player: " + client.getPlayer().getId() + " tried logging in");

            try {
                TimeUnit.SECONDS.sleep(30);
            } catch(Exception e) {
                GameEngine.getLogger().error("Error while sleeping banned client thread.", e);
            }
            return;
        }

        player.setSession(client);
        client.setPlayer(player);

        GameEngine.getRooms().loadRoomsForUser(player);

        client.getLogger().info(client.getPlayer().getData().getUsername() + " logged in");

        client.send(LoginMessageComposer.compose());
        client.send(FuserightsMessageComposer.compose(client.getPlayer().getSubscription().exists(), client.getPlayer().getData().getRank()));
        client.send(MotdNotificationComposer.compose());
        client.send(HomeRoomMessageComposer.compose(player.getSettings().getHomeRoom()));

        if(client.getPlayer().getPermissions().hasPermission("mod_tool")) {
            client.send(ModToolMessageComposer.compose());
        }
    }
}
