package com.mechanist.msg;

import com.mechanist.Msg;
import com.mechanist.MsgFactory;
import com.mechanist.proto.TunnelMessage;
import io.netty.buffer.ByteBuf;

public class MsgRunBshReq extends Msg {
    protected TunnelMessage.RunBshReq runBshReq;
    public MsgRunBshReq(){
        super(MsgCode.RunBashReq, MsgFactory.MSG_TYPE_BASE);
    }
    @Override
    public Msg clone() {
        return null;
    }

    @Override
    public void read(ByteBuf buffer) throws Exception {
        byte[] content = new byte[buffer.readableBytes()];
        buffer.readBytes(content);
        runBshReq = TunnelMessage.RunBshReq.parseFrom(content);
    }
}
