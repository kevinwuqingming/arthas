package com.alibaba.arthas.tunnel.server.msg;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mechanist.proto.TunnelMessage;
import com.mechanist.util.SyncMessageUtil;
import io.netty.buffer.ByteBuf;

public class MsgRunBashRespDealer implements InterfaceMsgDealer {
    @Override
    public void read(ByteBuf byteBuf) {
        byte[] content = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(content);
        try {
            TunnelMessage.RunBshResp runBashResp = TunnelMessage.RunBshResp.parseFrom(content);
            long requestId = runBashResp.getRequestId();
            SyncMessageUtil.exchangeResponse(requestId, runBashResp);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
