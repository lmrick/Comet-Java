package com.cometproject.server.protocol.messages;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.api.networking.messages.IMessageComposer;
import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;

public abstract class MessageComposer implements IMessageComposer {
	
	private static final Logger log = Logger.getLogger(MessageComposer.class);
	
	public MessageComposer() {
	}
	
	public final IComposerDataWrapper writeMessage(ByteBuf buf) {
		return this.writeMessageImpl(buf);
	}
	
	public final Composer writeMessageImpl(ByteBuf buffer) {
		final Composer composer = new Composer(this.getId(), buffer);
		
		try {
			this.compose(composer);
		} catch (Exception e) {
			throw e;
		} finally {
			this.dispose();
		}
		
		return composer;
	}
	
	public abstract short getId();
	public abstract void compose(IComposerDataWrapper msg);
	public void dispose() { }
	
}
