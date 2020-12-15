package com.alibaba.arthas.tunnel.server.msg;

import io.netty.buffer.ByteBuf;

public interface InterfaceMsgDealer {
    void read(ByteBuf byteBuf);
}
