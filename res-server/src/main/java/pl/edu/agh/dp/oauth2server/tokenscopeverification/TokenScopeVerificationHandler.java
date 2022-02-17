package pl.edu.agh.dp.oauth2server.tokenscopeverification;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

public class TokenScopeVerificationHandler extends ChannelInboundHandlerAdapter {
    private FullHttpRequest request;
    private final static String CLIENT_TOKEN_DATA = "clientTokenData";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        request = (FullHttpRequest) msg;

        JSONObject clientTokenData = (JSONObject) ctx.channel().attr(AttributeKey.valueOf(CLIENT_TOKEN_DATA)).get();

        if (TokenScopeVerifier.isScopeSufficient(request, clientTokenData)) {
            ctx.fireChannelRead(msg);
        }
        else {
            writeInsufficientScopeResponse(ctx);
            ctx.fireChannelReadComplete();
        }
    }

    private void writeInsufficientScopeResponse(ChannelHandlerContext ctx) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        JSONObject error = new JSONObject().put("error", "insufficient_scope");
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN, Unpooled.copiedBuffer(error.toString(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        if (keepAlive) {
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
        if (!keepAlive) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable exception) {
        exception.printStackTrace();
        ctx.close();
    }
}
