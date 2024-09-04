package com.cometproject.server.protocol.messages;

import com.cometproject.api.networking.messages.wrappers.IEventDataWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.IntStream;

public final class MessageEvent implements IEventDataWrapper {
	
	private final short id;
	private final int length;
	private final ByteBuf buffer;
	
	public MessageEvent(int length, ByteBuf buf) {
		this.length = length;
		this.buffer = (buf != null) && (buf.readableBytes() > 0) ? buf : Unpooled.EMPTY_BUFFER;
		
		this.id = this.content().readableBytes() >= 2 ? this.readShort() : 0;
	}
	
	@Override
	public short readShort() {
		return this.content().readShort();
	}
	
	@Override
	public int readInt() {
		try {
			return this.content().readInt();
		} catch (Exception e) {
			return 0;
		}
	}
	
	@Override
	public boolean readBoolean() {
		return (this.content().readByte() == 1);
	}
	
	@Override
	public String readString() {
		var length = this.readShort();
		var data = new byte[length];
		this.content().readBytes(data);
		return new String(data);
	}
	
	@Override
	public byte[] readBytes(int length) {
		final var bytes = new byte[length];
		IntStream.range(0, length).forEach(i -> bytes[i] = this.buffer.readByte());
		return bytes;
	}
	
	@Override
	public byte[] toRawBytes() {
		int length = this.buffer.readableBytes() - 6;
		byte[] rawBytes = new byte[length];
		this.buffer.getBytes(6, rawBytes);
		return rawBytes;
	}
	
	@Override
	public String toString() {
		var body = content().toString(CharsetUtil.UTF_8);
		for (var i = 0; i < 13; i++) {
			body = body.replace(Character.toString((char) i), "[" + i + "]");
		}
		return body;
	}
	
	@Override
	public short getId() {
		return this.id;
	}
	
	private ByteBuf content() {
		return this.buffer;
	}
	
	@Override
	public int getLength() {
		return this.buffer.readableBytes();
	}
	
}