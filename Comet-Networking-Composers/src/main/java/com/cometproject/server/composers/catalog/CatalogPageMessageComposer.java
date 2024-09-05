package com.cometproject.server.composers.catalog;

import com.cometproject.api.game.catalog.ICatalogService;
import com.cometproject.api.game.catalog.types.CatalogPageType;
import com.cometproject.api.game.catalog.types.ICatalogFrontPageEntry;
import com.cometproject.api.game.catalog.types.ICatalogItem;
import com.cometproject.api.game.catalog.types.ICatalogPage;
import com.cometproject.api.game.players.IPlayer;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;
import com.google.common.collect.Sets;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CatalogPageMessageComposer extends MessageComposer {
	
	private final String catalogType;
	private final ICatalogPage catalogPage;
	private final IPlayer player;
	private final ICatalogService catalogService;
	
	public CatalogPageMessageComposer(final String catalogType, final ICatalogPage catalogPage, final IPlayer player, ICatalogService catalogService) {
		this.catalogType = catalogType;
		this.catalogPage = catalogPage;
		this.player = player;
		this.catalogService = catalogService;
	}
	
	@Override
	public short getId() {
		return Composers.CatalogPageMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.catalogPage.getId());
		msg.writeString(this.catalogType); // builders club or not
		msg.writeString(this.catalogPage.getTemplate());
		
		msg.writeInt(this.catalogPage.getImages().size());
		
		this.catalogPage.getImages().forEach(msg::writeString);
		
		msg.writeInt(this.catalogPage.getTexts().size());
		
		this.catalogPage.getTexts().forEach(msg::writeString);
		
		if (this.catalogPage.getType() == CatalogPageType.RECENT_PURCHASES) {
			final Set<ICatalogItem> recentPurchases = player.getRecentPurchases().stream().map(this.catalogService::getCatalogItem).filter(Objects::nonNull).collect(Collectors.toSet());
			
			msg.writeInt(recentPurchases.size());
			
			recentPurchases.forEach(item -> item.compose(msg));
			
			recentPurchases.clear();
		} else if (!this.catalogPage.getTemplate().equals("frontpage") && !this.catalogPage.getTemplate().equals("club_buy")) {
			msg.writeInt(this.catalogPage.getItems().size());
			
			this.catalogPage.getItems().values().forEach(item -> item.compose(msg));
		} else {
			msg.writeInt(0);
		}
		
		msg.writeInt(0);
		msg.writeBoolean(false); // allow seasonal currency as credits
		
		if (this.catalogPage.getTemplate().equals("frontpage4")) {
			msg.writeInt(this.catalogService.getFrontPageEntries().size());
			
			this.catalogService.getFrontPageEntries().forEach(entry -> {
				msg.writeInt(entry.id());
				msg.writeString(entry.caption());
				msg.writeString(entry.image());
				msg.writeInt(0);
				msg.writeString(entry.pageLink());
				msg.writeString(entry.pageId());
			});
		}
	}
	
}
