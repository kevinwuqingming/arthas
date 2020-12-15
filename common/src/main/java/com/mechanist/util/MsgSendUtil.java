package com.mechanist.util;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

public class MsgSendUtil {
    public static void send(int msgid, GeneratedMessageV3 messageV3, Channel channel){
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(4 + messageV3.toByteArray().length);
        byteBuf.writeInt(msgid);
        byteBuf.writeBytes(messageV3.toByteArray());
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(byteBuf);
        channel.writeAndFlush(binaryWebSocketFrame);
    }
}
