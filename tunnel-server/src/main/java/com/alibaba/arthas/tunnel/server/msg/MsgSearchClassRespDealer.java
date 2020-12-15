package com.alibaba.arthas.tunnel.server.msg;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mechanist.proto.TunnelMessage;
import com.mechanist.util.SyncMessageUtil;
import io.netty.buffer.ByteBuf;

public class MsgSearchClassRespDealer implements InterfaceMsgDealer {
    @Override
    public void read(ByteBuf byteBuf) {
        byte[] content = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(content);
        try {
            TunnelMessage.SearchClassResp searchClassResp = TunnelMessage.SearchClassResp.parseFrom(content);
            long requestId = searchClassResp.getRequestId();
            SyncMessageUtil.exchangeResponse(requestId, searchClassResp);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
