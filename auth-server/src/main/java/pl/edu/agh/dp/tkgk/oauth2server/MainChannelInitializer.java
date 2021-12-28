package pl.edu.agh.dp.tkgk.oauth2server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.ssl.SslContext;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.pong.PingHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection.FetchTokenDataHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection.TokenDataResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection.TokenIntrospectionRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation.TokenRevocationHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation.TokenRevocationRequestValidator;

import pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation.TokenRevocationHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation.TokenRevocationRequestValidator;

import java.util.HashMap;
import java.util.List;

public class MainChannelInitializer extends ChannelInitializer<Channel> {

    private final SslContext sslContext;

    public MainChannelInitializer(SslContext sslContext){
        this.sslContext = sslContext;
    }

    private HashMap<String, Handler<FullHttpRequest, ?>> buildEndpointHandlerMap(){
        HashMap<String, Handler<FullHttpRequest, ?>> endpointHandlerMap = new HashMap<>();

        // Token Revocation
        Handler<FullHttpRequest, HttpPostRequestDecoder> tokenRevocationRequestValidator = new TokenRevocationRequestValidator();
        Handler<HttpPostRequestDecoder, ?> tokenRevocationHandler = new TokenRevocationHandler();
        tokenRevocationRequestValidator.setNext(tokenRevocationHandler);

        endpointHandlerMap.put("/revoke", tokenRevocationRequestValidator);

        // Token Introspection
        Handler<FullHttpRequest, HttpPostRequestDecoder> tokenIntrospectionRequestValidator = new TokenIntrospectionRequestValidator();
        Handler<HttpPostRequestDecoder, JSONObject> fetchTokenDataHandler = new FetchTokenDataHandler();
        Handler<JSONObject, ?> tokenDataResponseBuilder = new TokenDataResponseBuilder();
        tokenIntrospectionRequestValidator.setNext(fetchTokenDataHandler);
        fetchTokenDataHandler.setNext(tokenDataResponseBuilder);

        endpointHandlerMap.put("/introspect", tokenIntrospectionRequestValidator);


        // Ping
        Handler<FullHttpRequest, ?> pingHandler = new PingHandler();
        endpointHandlerMap.put("/ping", pingHandler);

        return endpointHandlerMap;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(sslContext.newHandler(ch.alloc()));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        pipeline.addLast(new SwitchPipelineHandler(buildEndpointHandlerMap()));

    }
}
