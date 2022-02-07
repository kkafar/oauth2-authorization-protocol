package pl.edu.agh.dp.oauth2server.tokenverification;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;
import pl.edu.agh.dp.oauth2server.SSLContextManager;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class TokenVerificationRequestManager {
    public static boolean isTokenValid(String token, String tokenTypeHint) {
        return introspectToken(token, tokenTypeHint);
    }

    private static boolean introspectToken(String token, String tokenTypeHint) {
        String host = "localhost";
        int port = 8008;
        String url = "https://" + host + ":" + port + "/introspect?param1=value";
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        IntrospectionHandler introspectionHandler = new IntrospectionHandler();

        try {
            URI uri = new URI(url);

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            SslContext sslContext = SSLContextManager.getSslContext();

                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(SslContextBuilder.forClient().build().newHandler(channel.alloc()));
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(1048576, false));
                            pipeline.addLast(introspectionHandler);
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();

            JSONObject requestBody = new JSONObject().put("token", token).put("token_type_hint", tokenTypeHint);
            FullHttpRequest postRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath(), Unpooled.copiedBuffer(requestBody.toString(), CharsetUtil.UTF_8));
            postRequest.headers().set(HttpHeaderNames.HOST, host + ":" + port);
            postRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
            postRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            postRequest.headers().set(HttpHeaderNames.AUTHORIZATION, "resource_server_bearer_token");

            System.out.println(postRequest + "\n");

            future.channel().writeAndFlush(postRequest);

            future.channel().closeFuture().sync();
        }
        catch (InterruptedException | URISyntaxException e) {
            e.printStackTrace();
        }
        finally {
            workerGroup.shutdownGracefully();
        }

        return introspectionHandler.getIntrospectionResult();
    }
}
