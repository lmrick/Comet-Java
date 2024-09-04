package com.cometproject.server.protocol.codec;

import com.cometproject.api.networking.messages.IMessageComposer;
import com.cometproject.server.protocol.messages.Composer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

public class MessageEncoder extends MessageToByteEncoder<IMessageComposer> {
	
	private static final Logger log = Logger.getLogger(MessageEncoder.class);
	
	@Override
	protected void encode(ChannelHandlerContext ctx, IMessageComposer msg, ByteBuf out) {
		try {
			final Composer composer = ((Composer) msg.writeMessage(out));
			
			if (!composer.isFinalized()) {
				composer.content().setInt(0, composer.content().writerIndex() - 4);
			}
		} catch (Exception e) {
			log.error(MessageFormat.format("Error encoding message {0} / {1}", msg.getId(), msg.getClass().getSimpleName()), e);
		}
	}
	
}
