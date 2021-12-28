package pl.edu.agh.dp.tkgk.oauth2server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.*;
import pl.edu.agh.dp.tkgk.oauth2server.pong.PingHandler;

import java.util.HashMap;

public class MainChannelInitializer extends ChannelInitializer<Channel> {

    private final SslContext sslContext;

    public MainChannelInitializer(SslContext sslContext){
        this.sslContext = sslContext;
    }

    private HashMap<String, Handler> buildEndpointHandlerMap(){
        HashMap<String, Handler> endpointHandlerMap = new HashMap<>();

        // Ping
        Handler pingHandler = new PingHandler();
        endpointHandlerMap.put("/ping", pingHandler);

        // Authorization request module
        Handler authFirstHandler = new RedirectionUriVerifier();
        authFirstHandler.setNext(new ParametersVerifier())
                .setNext(new UserIdentityVerifier())
                .setNext(new ScopeValidator())
                .setNext(new AuthorizationCodeResponder());
        endpointHandlerMap.put("/authorize", authFirstHandler);

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
