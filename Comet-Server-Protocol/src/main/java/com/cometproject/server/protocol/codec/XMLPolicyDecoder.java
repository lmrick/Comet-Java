package com.cometproject.server.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class XMLPolicyDecoder extends ByteToMessageDecoder {
	
	private static final String POLICY = """
																			<?xml version="1.0"?>
																			<!DOCTYPE cross-domain-policy SYSTEM "/xml/dtds/cross-domain-policy.dtd">
																			<cross-domain-policy>
																			<allow-access-from domain="*" to-ports="1-31111" />
																			</cross-domain-policy>
																			""" + (char) 0;
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		
		in.markReaderIndex();
		if (in.readableBytes() < 1) return;
		
		byte delimiter = in.readByte();
		
		in.resetReaderIndex();
		
		if (delimiter == 0x3C) {
			ctx.channel().writeAndFlush(POLICY).addListener(ChannelFutureListener.CLOSE);
		} else {
			ctx.channel().pipeline().remove(this);
			
			MessageDecoder decoder = ctx.pipeline().get(MessageDecoder.class);
			decoder.decode(ctx, in, out);
		}
	}
	
}
