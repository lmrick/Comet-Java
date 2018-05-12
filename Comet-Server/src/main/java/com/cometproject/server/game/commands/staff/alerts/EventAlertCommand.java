package com.cometproject.server.game.commands.staff.alerts;

import com.cometproject.api.networking.messages.IMessageComposer;
import com.cometproject.api.networking.sessions.ISession;
import com.cometproject.server.config.Locale;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.sessions.Session;


public class EventAlertCommand extends ChatCommand {
    @Override
    public void execute(Session client, String[] params) {
        if (params.length == 0) {
            return;
        }

        final IMessageComposer msg = new AdvancedAlertMessageComposer(
                Locale.get("command.eventalert.alerttitle"),
                Locale.get("command.eventalert.message").replace("%message%", this.merge(params)).replace("%username%", client.getPlayer().getData().getUsername()
                ) + "<br><br><i> " + client.getPlayer().getData().getUsername() + "</i>",
                Locale.get("command.eventalert.buttontitle"), "event:navigator/goto/" + client.getPlayer().getEntity().getRoom().getId(), "game_promo_small");

        for (ISession session : NetworkManager.getInstance().getSessions().getSessions().values()) {
            if (session.getPlayer() != null && !session.getPlayer().getSettings().ignoreEvents())
                session.send(msg);
        }
    }

    @Override
    public String getPermission() {
        return "eventalert_command";
    }

    @Override
    public String getParameter() {
        return "";
    }

    @Override
    public String getDescription() {
        return Locale.get("command.eventalert.description");
    }
}
