package pl.edu.agh.dp.tkgk.oauth2server.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import pl.edu.agh.dp.tkgk.oauth2server.common.Handler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.pagenotfound.PageNotFoundHandler;

import java.util.HashMap;

public class SwitchPipelineHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final HashMap<String, Handler<FullHttpRequest, ?>> endpointHandlerMap;
    private final Handler<FullHttpRequest, ?> pageNotFoundHandler;

    public SwitchPipelineHandler(HashMap<String, Handler<FullHttpRequest, ?>> endpointHandlerMap) {
        this.endpointHandlerMap = endpointHandlerMap;
        pageNotFoundHandler = new PageNotFoundHandler();
    }


    private String extractEndpoint(FullHttpRequest request) {
        String uri = request.uri();
        return uri.split("\\?")[0];
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        String endpoint = extractEndpoint(msg);
        Handler<FullHttpRequest, ?> requestHandler = endpointHandlerMap.getOrDefault(endpoint, pageNotFoundHandler);
        ctx.writeAndFlush(requestHandler.handle(msg))
                .addListener(ChannelFutureListener.CLOSE);
    }
}
