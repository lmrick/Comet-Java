package com.cometproject.server.network.messages.outgoing.room.items.wired.dialog;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredActionItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.base.WiredTriggerItem;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;
import java.util.List;

public class WiredActionMessageComposer extends MessageComposer {
	private final List<WiredTriggerItem> incompatibleTriggers;
	private final WiredActionItem wiredAction;
	
	public WiredActionMessageComposer(final WiredActionItem wiredAction) {
		this.wiredAction = wiredAction;
		this.incompatibleTriggers = wiredAction.getIncompatibleTriggers();
	}
	
	@Override
	public short getId() {
		return Composers.WiredEffectConfigMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeBoolean(false); // advanced
		msg.writeInt(wiredAction.getFurniSelection());
		
		msg.writeInt(wiredAction.getWiredData().getSelectedIds().size());
		
		wiredAction.getWiredData().getSelectedIds().stream()
		.mapToInt(itemId -> ItemManager.getInstance().getItemVirtualId(itemId))
		.forEach(msg::writeInt);
		
		msg.writeInt(wiredAction.getDefinition().getSpriteId());
		msg.writeInt(wiredAction.getVirtualId());
		
		msg.writeString(wiredAction.getWiredData().getText());
		
		msg.writeInt(wiredAction.getWiredData().getParams().size());
		wiredAction.getWiredData().getParams().values().forEach(msg::writeInt);
		
		msg.writeInt(wiredAction.getWiredData().getSelectionType());
		msg.writeInt(wiredAction.getInterface());
		msg.writeInt(wiredAction.getWiredData().getDelay());
		
		msg.writeInt(incompatibleTriggers.size());
		
		incompatibleTriggers.stream()
		.mapToInt(incompatibleTrigger -> incompatibleTrigger.getDefinition().getSpriteId())
		.forEach(msg::writeInt);
		
		//        msg.writeString(""); //no idea
	}
	
	@Override
	public void dispose() {
		this.incompatibleTriggers.clear();
	}
	
}
