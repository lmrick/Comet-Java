package com.cometproject.server.composers.catalog.groups;

import com.cometproject.api.game.groups.IGroupItemService;
import com.cometproject.api.game.groups.items.IGroupBadgeItem;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class GroupElementsMessageComposer extends MessageComposer {
	
	private final IGroupItemService groupItemService;
	
	public GroupElementsMessageComposer(IGroupItemService groupItemService) {
		this.groupItemService = groupItemService;
	}
	
	@Override
	public short getId() {
		return Composers.BadgeEditorPartsMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.groupItemService.getBases().size());
		
		this.groupItemService.getBases().forEach(base -> {
			msg.writeInt(base.getId());
			msg.writeString(base.getFirstValue());
			msg.writeString(base.getSecondValue());
		});
		
		msg.writeInt(this.groupItemService.getSymbols().size());
		
		this.groupItemService.getSymbols().forEach(symbol -> {
			msg.writeInt(symbol.getId());
			msg.writeString(symbol.getFirstValue());
			msg.writeString(symbol.getSecondValue());
		});
		
		msg.writeInt(this.groupItemService.getBaseColours().size());
		
		this.groupItemService.getBaseColours().forEach(colour -> {
			msg.writeInt(colour.getId());
			msg.writeString(colour.getFirstValue());
		});
		
		msg.writeInt(this.groupItemService.getSymbolColours().size());
		
		this.groupItemService.getSymbolColours().values().forEach(colour -> {
			msg.writeInt(colour.getId());
			msg.writeString(colour.getFirstValue());
		});
		
		msg.writeInt(this.groupItemService.getBackgroundColours().size());
		
		this.groupItemService.getBackgroundColours().values().forEach(colour -> {
			msg.writeInt(colour.getId());
			msg.writeString(colour.getFirstValue());
		});
	}
	
}
