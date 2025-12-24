package com.cometproject.api.utilities.events.catalog;

import com.cometproject.api.utilities.events.Event;
import com.cometproject.api.utilities.events.catalog.args.ItemCreatedArgs;
import java.util.function.Consumer;

public class OnItemCreatedEvent extends Event<ItemCreatedArgs> implements IEconomyEvent {
	
	public OnItemCreatedEvent(Consumer<ItemCreatedArgs> consumer) {
		super(consumer);
	}
	
	@Override
	public boolean isAsync() {
		return true;
	}
	
}