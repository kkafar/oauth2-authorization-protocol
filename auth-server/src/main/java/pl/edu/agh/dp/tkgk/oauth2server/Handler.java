package pl.edu.agh.dp.tkgk.oauth2server;

import io.netty.handler.codec.http.FullHttpResponse;

public interface Handler<T, K> {

    Handler<K, ?> setNext(Handler<K, ?> handler);
    FullHttpResponse handle(T request);
}
