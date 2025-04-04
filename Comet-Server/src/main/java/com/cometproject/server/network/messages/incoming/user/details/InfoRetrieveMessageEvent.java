package com.cometproject.server.network.messages.incoming.user.details;


import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.achievements.IAchievementsService;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.messenger.MessengerConfigMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.achievements.AchievementPointsMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.achievements.AchievementRequirementsMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.buildersclub.BuildersClubMembershipMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.details.UserObjectMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.BadgeInventoryMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.permissions.AllowancesMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.wardrobe.FigureSetIdsMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;

public class InfoRetrieveMessageEvent implements Event {
	
	@Override
	public void handle(Session client, MessageEvent msg) {
		final var player = client.getPlayer();
		final var badges = client.getPlayer().getInventory().getBadges();
		final var rank = client.getPlayer().getData().getRank();
		final var wardrobeClothing = client.getPlayer().getWardrobe().getClothing();
		final var achievementsPoints = client.getPlayer().getData().getAchievementPoints();
		final var achievementsGroups = GameContext.getCurrent().getService(IAchievementsService.class).getAchievementGroups().values();

		client.getPlayer().sendBalance();
		
		client.sendQueue(new UserObjectMessageComposer(player));
		client.sendQueue(new BuildersClubMembershipMessageComposer());
		client.sendQueue(new AllowancesMessageComposer(rank));
		//client.send(new CitizenshipStatusMessageComposer());
		client.sendQueue(new AchievementPointsMessageComposer(achievementsPoints));
		client.sendQueue(new MessengerConfigMessageComposer());
		client.sendQueue(new BadgeInventoryMessageComposer(badges));
		client.sendQueue(new AchievementRequirementsMessageComposer(achievementsGroups));
		client.sendQueue(new FigureSetIdsMessageComposer(wardrobeClothing));
		client.getPlayer().getMessenger().sendStatus(true, client.getPlayer().getEntity() != null);
		
		client.flush();
	}
	
}
