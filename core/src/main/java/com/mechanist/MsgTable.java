package com.mechanist;

import com.mechanist.event.EventRedefineReq;
import com.mechanist.event.EventRunBshReq;
import com.mechanist.event.EventSearchClassReq;

public class MsgTable {

	public static void init() {
		// 类加载查询消息，msgid=1000,msgtype=0;
		MsgFactory.add(new EventSearchClassReq());
		// 热更新消息,msgid=1002,msgtype=0;
		MsgFactory.add(new EventRedefineReq());
		// 执行bsh代码,msgid=1004,msgtype=0
		MsgFactory.add(new EventRunBshReq());
	}
}