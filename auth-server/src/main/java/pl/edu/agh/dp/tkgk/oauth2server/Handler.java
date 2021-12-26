package pl.edu.agh.dp.tkgk.oauth2server;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public interface Handler {

    Handler setNext(Handler handler);
    FullHttpResponse handle(FullHttpRequest request);
}
