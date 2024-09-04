package com.cometproject.server.network.messages.outgoing.handshake;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

public class InitCryptoMessageComposer extends MessageComposer {
	
	private final String prime;
	private final String generator;
	
	public InitCryptoMessageComposer(final String prime, final String generator) {
		this.prime = prime;
		this.generator = generator;
	}
	
	@Override
	public short getId() {
		return Composers.InitCryptoMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeString(this.prime);
		msg.writeString(this.generator);
	}
	
}
