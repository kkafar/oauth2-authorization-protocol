package pl.edu.agh.dp.tkgk.oauth2server;

import io.netty.handler.codec.http.FullHttpResponse;

import java.util.List;
import java.util.function.Consumer;

public interface Handler<T, K> {

    void setNext(Handler<K, ?> handler);
    <S> Handler<K, S> setNextAndGet(Handler<K, S> handler);
    FullHttpResponse handle(T request);
    List<Handler<?,?>> getChain();
}
