package com.mechanist;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

public class MsgFactory {

	private static Logger logger = LoggerFactory.getLogger(MsgFactory.class);

	public final static int MSG_TYPE_BASE = 0;
	public final static int MSG_TYPE_ASYNC = 1;
	public final static int MSG_TYPE_SYNC = 2;
	public static void init() {
		MsgTable.init();
	}

	private static Msg[] msgs = new Msg[0xffff];

	public static void add(Msg msg) {
		logger.info("注册监听消息：" + msg.getClass().getName() + " = " + msg.msgid);
		msgs[msg.msgid] = msg;
	}

	public static Msg create(int msgid) {
		if (msgs[msgid] == null)
			return null;

		return msgs[msgid].clone();
	}
}
