package com.mechanist;

public interface BinaryWebSocketHandler<B,C> {
    void handle(B b, C c);
}
