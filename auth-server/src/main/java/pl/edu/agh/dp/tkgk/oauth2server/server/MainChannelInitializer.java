package pl.edu.agh.dp.tkgk.oauth2server.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.tokenendpoint.TokenGrantTypesHandlerChainsBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.server.util.ServerEndpointsBuilder;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

public class MainChannelInitializer extends ChannelInitializer<Channel> {

    private final SslContext sslContext;
    
    public MainChannelInitializer(SslContext sslContext){
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(sslContext.newHandler(ch.alloc()));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast(new SwitchPipelineHandler(new ServerEndpointsBuilder().getEndpointHandlerMap()));

        // to make sure that handler chains serving different grant types on token endpoint are built before
        // the server receives any requests from the clients
        TokenGrantTypesHandlerChainsBuilder.getInstance().buildChains();
    }
}
