package com.cometproject.api.utilities.events.catalog;

import com.cometproject.api.utilities.events.Event;
import com.cometproject.api.utilities.events.catalog.args.PurchaseArgs;
import java.util.function.Consumer;

public class OnPurchaseEvent extends Event<PurchaseArgs> implements IEconomyEvent {
	
	public OnPurchaseEvent(Consumer<PurchaseArgs> consumer) {
		super(consumer);
	}
	
	@Override
	public boolean isAsync() {
		return true;
	}
	
}
