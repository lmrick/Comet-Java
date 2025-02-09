package com.cometproject.server.network.messages.outgoing.room.items.wired.dialog;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.WiredUtil;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredTriggerItem;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.List;

public class WiredTriggerMessageComposer extends MessageComposer {
	
	private final List<WiredActionItem> incompatibleActions;
	private final WiredTriggerItem wiredTrigger;
	
	public WiredTriggerMessageComposer(final WiredTriggerItem wiredTriggerItem) {
		this.wiredTrigger = wiredTriggerItem;
		this.incompatibleActions = wiredTriggerItem.getIncompatibleActions();
	}
	
	@Override
	public short getId() {
		return Composers.WiredTriggerConfigMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeBoolean(false); // advanced
		msg.writeInt(WiredUtil.MAX_FURNI_SELECTION);
		
		msg.writeInt(wiredTrigger.getWiredData().getSelectedIds().size());
		
		wiredTrigger.getWiredData().getSelectedIds().stream().mapToInt(itemId -> ItemManager.getInstance().getItemVirtualId(itemId)).forEach(msg::writeInt);
		
		msg.writeInt(wiredTrigger.getDefinition().getSpriteId());
		msg.writeInt(wiredTrigger.getVirtualId());
		
		msg.writeString(wiredTrigger.getWiredData().getText());
		
		msg.writeInt(wiredTrigger.getWiredData().getParams().size());
		
		wiredTrigger.getWiredData().getParams().values().forEach(msg::writeInt);
		
		msg.writeInt(wiredTrigger.getWiredData().getSelectionType());
		msg.writeInt(wiredTrigger.getInterface());
		
		msg.writeInt(incompatibleActions.size());
		
		incompatibleActions.stream().mapToInt(incompatibleAction -> incompatibleAction.getDefinition().getSpriteId()).forEach(msg::writeInt);
	}
	
	@Override
	public void dispose() {
		this.incompatibleActions.clear();
	}
	
}
