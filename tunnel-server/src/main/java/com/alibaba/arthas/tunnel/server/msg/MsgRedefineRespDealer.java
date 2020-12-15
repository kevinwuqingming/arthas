package com.alibaba.arthas.tunnel.server.msg;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mechanist.proto.TunnelMessage;
import com.mechanist.util.SyncMessageUtil;
import io.netty.buffer.ByteBuf;

public class MsgRedefineRespDealer implements InterfaceMsgDealer {
    @Override
    public void read(ByteBuf byteBuf) {
        byte[] content = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(content);
        try {
            TunnelMessage.RedefineResp redefineResp = TunnelMessage.RedefineResp.parseFrom(content);
            long requestId = redefineResp.getRequestId();
            SyncMessageUtil.exchangeResponse(requestId, redefineResp);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
