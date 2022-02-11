package pl.edu.agh.dp.oauth2server.datafetch;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import org.json.JSONObject;

public class DataFetchHandler extends ChannelInboundHandlerAdapter {
    private FullHttpRequest request;
    private final static String CLIENT_TOKEN_DATA = "clientTokenData";
    private final static String CLIENT_DATA = "clientData";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        request = (FullHttpRequest) msg;

        JSONObject clientTokenData = (JSONObject) ctx.channel().attr(AttributeKey.valueOf(CLIENT_TOKEN_DATA)).get();
        ctx.channel().attr(AttributeKey.valueOf(CLIENT_TOKEN_DATA)).set(null);

        JSONObject clientData = DataFetcher.fetchData(request, clientTokenData);

        ctx.channel().attr(AttributeKey.valueOf(CLIENT_DATA)).set(clientData);

        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable exception) {
        exception.printStackTrace();
        ctx.close();
    }
}
