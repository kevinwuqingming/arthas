package com.alibaba.arthas.tunnel.server.msg;

import com.mechanist.msg.MsgCode;

import java.util.HashMap;
import java.util.Map;

public class MsgFactory {
    Map<Integer, InterfaceMsgDealer> msgMap = new HashMap<>();
    private MsgFactory(){
        msgMap.put((Integer) MsgCode.SearchClassResp[0], new MsgSearchClassRespDealer());
        msgMap.put((Integer) MsgCode.RedefineResp[0], new MsgRedefineRespDealer());
        msgMap.put((Integer) MsgCode.RunBashResp[0], new MsgRunBashRespDealer());
    }
    private static class SingletonHolder{
        protected static final MsgFactory instance = new MsgFactory();
    }

    public static MsgFactory getInstance(){
        return SingletonHolder.instance;
    }

    public InterfaceMsgDealer getMsgDealer(int msgid){
        return msgMap.get(msgid);
    }


}
