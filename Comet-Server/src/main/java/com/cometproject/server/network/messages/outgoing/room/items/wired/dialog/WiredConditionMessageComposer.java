package com.cometproject.server.network.messages.outgoing.room.items.wired.dialog;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.WiredUtil;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredConditionItem;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class WiredConditionMessageComposer extends MessageComposer {
	
	private final WiredConditionItem wiredConditionItem;
	
	public WiredConditionMessageComposer(final WiredConditionItem wiredConditionItem) {
		this.wiredConditionItem = wiredConditionItem;
	}
	
	@Override
	public short getId() {
		return Composers.WiredConditionConfigMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeBoolean(false); // advanced
		msg.writeInt(WiredUtil.MAX_FURNI_SELECTION);
		
		msg.writeInt(wiredConditionItem.getWiredData().getSelectedIds().size());
		
		wiredConditionItem.getWiredData().getSelectedIds().stream().mapToInt(itemId -> ItemManager.getInstance().getItemVirtualId(itemId)).forEach(msg::writeInt);
		
		msg.writeInt(wiredConditionItem.getDefinition().getSpriteId());
		msg.writeInt(wiredConditionItem.getVirtualId());
		
		msg.writeString(wiredConditionItem.getWiredData().getText());
		
		msg.writeInt(wiredConditionItem.getWiredData().getParams().size());
		
		wiredConditionItem.getWiredData().getParams().values().forEach(msg::writeInt);
		
		msg.writeInt(wiredConditionItem.getWiredData().getSelectionType());
		msg.writeInt(wiredConditionItem.getInterface());
	}
	
}
