package com.cometproject.server.protocol.messages;

import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.util.CharsetUtil;

public class Composer implements ByteBufHolder, IComposerDataWrapper {
	protected final int id;
	protected final ByteBuf body;
	
	public Composer(short id, ByteBuf body) {
		this.id = id;
		this.body = body;
		
		try {
			this.body.writeInt(-1);
			this.body.writeShort(id);
		} catch (Exception e) {
			exceptionCaught(e);
		}
	}
	
	private void exceptionCaught(Exception e) {
		e.printStackTrace();
	}
	
	public Composer(int id, ByteBuf body) {
		this.id = id;
		this.body = body;
	}
	
	@Override
	public ByteBuf content() {
		return this.body;
	}
	
	@Override
	public Composer copy() {
		return new Composer(this.id, this.body.copy());
	}
	
	@Override
	public Composer duplicate() {
		return new Composer(this.id, this.body.duplicate());
	}
	
	@Override
	public Composer retainedDuplicate() {
		return new Composer(this.id, this.body.retainedDuplicate());
	}
	
	@Override
	public Composer replace(ByteBuf byteBuf) {
		return new Composer(this.id, byteBuf);
	}
	
	@Override
	public int refCnt() {
		return this.body.refCnt();
	}
	
	@Override
	public Composer retain() {
		return new Composer(this.id, this.body.retain());
	}
	
	@Override
	public Composer retain(int increment) {
		return new Composer(this.id, this.body.retain(increment));
	}
	
	@Override
	public Composer touch() {
		this.body.touch();
		return this;
	}
	
	@Override
	public Composer touch(Object o) {
		this.body.touch(o);
		return this;
	}
	
	@Override
	public boolean release() {
		return this.body.release();
	}
	
	@Override
	public boolean release(int decrement) {
		return this.body.release(decrement);
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	@Override
	public void clear() {
		this.body.clear();
	}
	
	@Override
	public boolean isFinalized() {
		return (this.body.getInt(0) > -1);
	}
	
	@Override
	public void writeString(Object obj) {
		if (obj instanceof String) {
			if (((String) obj).isEmpty()) {
				this.writeShort(0);
				return;
			}
		}
		
		try {
			String string = "";
			
			if (obj != null) {
				string = String.valueOf(obj);
			}
			
			byte[] data = string.getBytes(CharsetUtil.UTF_8);
			this.body.writeShort(data.length);
			this.body.writeBytes(data);
		} catch (Exception e) {
			exceptionCaught(e);
		}
	}
	
	@Override
	public void writeEmptyString() {
		this.writeShort(0);
	}
	
	@Override
	public void writeDouble(double d) {
		this.writeString(Double.toString(d));
	}
	
	@Override
	public void writeInt(int i) {
		try {
			this.body.writeInt(i);
		} catch (Exception e) {
			exceptionCaught(e);
		}
	}
	
	@Override
	public void writeLong(long i) {
		try {
			this.body.writeLong(i);
		} catch (Exception e) {
			exceptionCaught(e);
		}
	}
	
	@Override
	public void writeBoolean(boolean b) {
		try {
			this.body.writeByte(b ? 1 : 0);
		} catch (Exception e) {
			exceptionCaught(e);
		}
	}
	
	@Override
	public void writeByte(int b) {
		try {
			this.body.writeByte(b);
		} catch (Exception e) {
			exceptionCaught(e);
		}
	}
	
	@Override
	public void writeShort(int s) {
		try {
			this.body.writeShort((short) s);
		} catch (Exception e) {
			exceptionCaught(e);
		}
	}
	
}
