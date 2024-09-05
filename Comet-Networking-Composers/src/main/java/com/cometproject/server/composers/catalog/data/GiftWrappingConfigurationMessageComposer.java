package com.cometproject.server.composers.catalog.data;

import com.cometproject.api.game.catalog.ICatalogService;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.Arrays;

public class GiftWrappingConfigurationMessageComposer extends MessageComposer {
	private static final int[] GIFT_COLOURS = { 0, 1, 2, 3, 4, 5, 6, 8 };
	private static final int[] GIFT_DECORATIONS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
	private final ICatalogService catalogService;
	
	public GiftWrappingConfigurationMessageComposer(final ICatalogService catalogService) {
		this.catalogService = catalogService;
	}
	
	@Override
	public short getId() {
		return Composers.GiftWrappingConfigurationMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeBoolean(true);//?
		msg.writeInt(1);//?
		msg.writeInt(this.catalogService.getGiftBoxesNew().size());
		
		this.catalogService.getGiftBoxesNew().forEach(msg::writeInt);
		
		msg.writeInt(GIFT_COLOURS.length);
		
		Arrays.stream(GIFT_COLOURS).forEachOrdered(msg::writeInt);
		
		msg.writeInt(GIFT_DECORATIONS.length);
		
		Arrays.stream(GIFT_DECORATIONS).forEachOrdered(msg::writeInt);
		
		msg.writeInt(this.catalogService.getGiftBoxesOld().size());
		
		this.catalogService.getGiftBoxesOld().forEach(msg::writeInt);
	}
	
}
