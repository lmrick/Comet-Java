package com.cometproject.api.utilities.events.catalog;

import com.cometproject.api.utilities.events.Event;
import com.cometproject.api.utilities.events.catalog.args.ItemRemovedArgs;
import java.util.function.Consumer;

public class OnItemRemovedEvent extends Event<ItemRemovedArgs> implements IEconomyEvent {
	
	public OnItemRemovedEvent(Consumer<ItemRemovedArgs> consumer) {
		super(consumer);
	}
	
	@Override
	public boolean isAsync() {
		return true;
	}
	
}
