package pl.edu.agh.dp.oauth2server.tokenverification;

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
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

public class TokenVerificationHandler extends ChannelInboundHandlerAdapter {
    private FullHttpRequest request;
    private final StringBuilder responseData = new StringBuilder();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        request = (FullHttpRequest) msg;

        String token = request.headers().get(HttpHeaderNames.AUTHORIZATION).split(" ")[0];
        String tokenTypeHint = request.headers().get(HttpHeaderNames.AUTHORIZATION).split(" ")[1];

        if (TokenVerificationRequestManager.isTokenValid(token, tokenTypeHint)) {
            responseData.setLength(0);
            responseData.append("Everything went fine thus far!");
            writeDefaultResponse(ctx);
            ctx.fireChannelRead(msg);
        }
        else {
            writeTokenInvalidResponse(ctx);
            ctx.fireChannelReadComplete();
        }
    }

    private void writeTokenInvalidResponse(ChannelHandlerContext ctx) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        JSONObject error = new JSONObject().put("error", "insufficient_scope");
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED, Unpooled.copiedBuffer(error.toString(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, "Bearer sufficient_scope_for_requested_resources");
        if (keepAlive) {
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.writeAndFlush(response);
        if (!keepAlive) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void writeDefaultResponse(ChannelHandlerContext ctx) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
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
