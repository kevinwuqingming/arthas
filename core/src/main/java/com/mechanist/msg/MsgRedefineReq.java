package com.mechanist.msg;

import com.mechanist.Msg;
import com.mechanist.MsgFactory;
import com.mechanist.proto.TunnelMessage;
import io.netty.buffer.ByteBuf;

public class MsgRedefineReq extends Msg {
    protected TunnelMessage.RedefineReq redefineReq;
    public MsgRedefineReq(){
        super(MsgCode.RedefineReq, MsgFactory.MSG_TYPE_BASE);
    }
    @Override
    public Msg clone() {
        return null;
    }

    @Override
    public void read(ByteBuf buffer) throws Exception {
        byte[] content = new byte[buffer.readableBytes()];
        buffer.readBytes(content);
        redefineReq = TunnelMessage.RedefineReq.parseFrom(content);
    }
}
