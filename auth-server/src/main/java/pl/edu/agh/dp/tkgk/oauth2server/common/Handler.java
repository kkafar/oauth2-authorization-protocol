package pl.edu.agh.dp.tkgk.oauth2server.common;

import io.netty.handler.codec.http.FullHttpResponse;

import java.util.List;

public interface Handler<T, K> {

    void setNext(Handler<K, ?> handler);
    <S> Handler<K, S> setNextAndGet(Handler<K, S> handler);
    FullHttpResponse handle(T request);
    List<Handler<?,?>> getChain();
}
