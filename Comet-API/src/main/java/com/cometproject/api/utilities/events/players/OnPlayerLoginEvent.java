package com.cometproject.api.utilities.events.players;

import java.util.function.Consumer;

import com.cometproject.api.utilities.events.Event;
import com.cometproject.api.utilities.events.players.args.OnPlayerLoginEventArgs;

public class OnPlayerLoginEvent extends Event<OnPlayerLoginEventArgs> {
	
	public OnPlayerLoginEvent(Consumer<OnPlayerLoginEventArgs> eventConsumer) {
		super(eventConsumer);
	}
	
}
