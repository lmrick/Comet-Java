package com.cometproject.server.composers.catalog;

import com.cometproject.api.game.catalog.ICatalogService;
import com.cometproject.api.game.catalog.types.ICatalogItem;
import com.cometproject.api.game.catalog.types.ICatalogPage;
import com.cometproject.api.game.furniture.IFurnitureService;
import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.List;
import java.util.Objects;

public class CatalogIndexMessageComposer extends MessageComposer {
	private final IFurnitureService furnitureService;
	private final ICatalogService catalogService;
	private final int playerRank;
	
	public CatalogIndexMessageComposer(final ICatalogService catalogService, final IFurnitureService furnitureService, final int playerRank) {
		this.catalogService = catalogService;
		this.furnitureService = furnitureService;
		this.playerRank = playerRank;
	}
	
	@Override
	public short getId() {
		return Composers.CatalogIndexMessageComposer;
	}
	
	@Override
	public void compose(final IComposerDataWrapper msg) {
		msg.writeBoolean(true);
		msg.writeInt(0);
		msg.writeInt(-1);
		msg.writeString("root");
		msg.writeString("");
		msg.writeInt(0);
		msg.writeInt(this.countAccessiblePages(this.catalogService.getParentPages()));
		
		this.catalogService.getParentPages().stream()
						.filter(page -> page.getMinRank() <= this.playerRank)
						.forEachOrdered(page -> composePage(page, msg));
		
		msg.writeBoolean(false);
		msg.writeString("NORMAL");
	}
	
	private void composePage(ICatalogPage page, IComposerDataWrapper msg) {
		msg.writeBoolean(true);
		msg.writeInt(page.getIcon());
		msg.writeInt(page.isEnabled() ? page.getId() : -1);
		msg.writeString(page.getLinkName().equals("undefined") ? page.getCaption().toLowerCase().replaceAll("[^A-Za-z0-9]", "").replace(" ", "_") : page.getLinkName());
		msg.writeString(page.getCaption());
		msg.writeInt(0);
		msg.writeInt(this.countAccessiblePages(page.getChildren()));
		
		page.getChildren().stream()
						.filter(child -> child.getMinRank() <= this.playerRank)
						.forEachOrdered(child -> {
			msg.writeBoolean(true);
			msg.writeInt(child.getIcon());
			msg.writeInt(child.isEnabled() ? child.getId() : -1);
			msg.writeString(child.getLinkName().equals("undefined") ? child.getCaption().toLowerCase().replaceAll("[^A-Za-z0-9]", "").replace(" ", "_") : child.getLinkName());
			msg.writeString(child.getCaption());
			msg.writeInt(child.getOfferSize());
			child.getItems().values().stream().filter(item -> !item.getItemId().equals("-1")).map(item -> this.furnitureService.getDefinition(item.getItems().getFirst().itemId())).filter(Objects::nonNull).mapToInt(IFurnitureDefinition::getOfferId).filter(offerId -> offerId != -1).forEachOrdered(msg::writeInt);
			msg.writeInt(this.countAccessiblePages(child.getChildren()));
			child.getChildren().stream().filter(childTwo -> child.getMinRank() <= this.playerRank).forEachOrdered(childTwo -> composePage(childTwo, msg));
		});
	}
	
	private int countAccessiblePages(final List<ICatalogPage> pages) {
		return (int) pages.stream().filter(catalogPage -> catalogPage.getMinRank() <= this.playerRank).count();
	}
	
}
