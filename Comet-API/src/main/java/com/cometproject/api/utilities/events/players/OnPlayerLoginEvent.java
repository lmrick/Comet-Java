package com.cometproject.api.utilities.events.players;

import java.util.function.Consumer;

import com.cometproject.api.utilities.events.Event;
import com.cometproject.api.utilities.events.players.args.PlayerLoginArgs;

public class OnPlayerLoginEvent extends Event<PlayerLoginArgs> {
	
	public OnPlayerLoginEvent(Consumer<PlayerLoginArgs> eventConsumer) {
		super(eventConsumer);
	}
	
}
