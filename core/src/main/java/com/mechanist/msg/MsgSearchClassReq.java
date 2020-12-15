package com.mechanist.msg;

import com.mechanist.Msg;
import com.mechanist.MsgFactory;
import com.mechanist.proto.TunnelMessage.SearchClassReq;
import io.netty.buffer.ByteBuf;

public class MsgSearchClassReq extends Msg {
    protected SearchClassReq searchClassReq;
    public MsgSearchClassReq(){
        super(MsgCode.SearchClassReq, MsgFactory.MSG_TYPE_BASE);
    }
    @Override
    public Msg clone() {
        return null;
    }

    @Override
    public void read(ByteBuf buffer) throws Exception {
        byte[] content = new byte[buffer.readableBytes()];
        buffer.readBytes(content);
        searchClassReq = SearchClassReq.parseFrom(content);
    }
}
