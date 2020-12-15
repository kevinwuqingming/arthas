package com.mechanist;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public abstract class Msg extends Event {

	public int msgid;
	public int msgtype;
	public Channel channel;

	public Msg(int _msgid, int _type) {
		msgid = _msgid;
		msgtype = _type;
	}

	public Msg(Object[] msgInfo, int _type) {
		msgid = Integer.valueOf(String.valueOf(msgInfo[0]));
		msgtype = _type;
	}

	abstract public Msg clone();

	abstract public void read(ByteBuf buffer) throws Exception;

	public void kick() {
		if (this.channel != null) {
			this.channel.disconnect();
		}
	}
	
	public void run() {
		super.run();
	}
}
