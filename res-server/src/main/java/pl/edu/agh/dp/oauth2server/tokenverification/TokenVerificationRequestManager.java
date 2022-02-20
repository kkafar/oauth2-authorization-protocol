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
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;
import pl.edu.agh.dp.oauth2server.SSLContextManager;

import java.net.URI;
import java.net.URISyntaxException;

public class TokenVerificationRequestManager {
    private static final String RESOURCE_SERVER_AUTHENTICATION_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzY29wZSI6WyJpbnRyb3NwZWN0aW9uIl0sInVzZXJfbG9naW4iOiJhdXRoLXNlcnZlciIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJleHAiOjE2NTUzODQ4MzIsImlhdCI6MTY0NTM4NDgzMiwiaXNfYWNjZXNzX3Rva2VuIjp0cnVlLCJqdGkiOiIweGIyVDgxMFIxT1duN0xLdmFRYnBKazJJcmRmTTVPOCIsImF1dGhfY29kZSI6ImF1dGgtc2VydmVyIn0.c0rd29sz6IQI-7ejlfI5NOlj9EBt2LbRZtBVn-xv4JA";

    private static JSONObject clientTokenData;

    public static boolean isTokenValid(String token) {
        String tokenTypeHint = "access_token";
        return introspectToken(token, tokenTypeHint);
    }

    public static JSONObject getClientTokenData() {
        return clientTokenData;
    }

    private static boolean introspectToken(String token, String tokenTypeHint) {
        String host = "bd6a-91-123-181-221.ngrok.io";
        int port = 443;
        String url = "https://" + host + ":" + port + "/introspect";
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        IntrospectionHandler introspectionHandler = new IntrospectionHandler();

        try {
            URI uri = new URI(url);

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) {
                            SslContext sslContext = SSLContextManager.getClientSslContext();

                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(sslContext.newHandler(channel.alloc()));
                            pipeline.addLast(new HttpClientCodec());
                            pipeline.addLast(new HttpObjectAggregator(1048576, false));
                            pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                            pipeline.addLast(introspectionHandler);
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();

            JSONObject requestBody = new JSONObject().put("token", token).put("token_type_hint", tokenTypeHint);
            FullHttpRequest postRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath(), Unpooled.copiedBuffer(requestBody.toString(), CharsetUtil.UTF_8));

            postRequest.headers().set(HttpHeaderNames.HOST, host + ":" + port);
            postRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
            postRequest.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            postRequest.headers().set(HttpHeaderNames.AUTHORIZATION, RESOURCE_SERVER_AUTHENTICATION_TOKEN);
            postRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, postRequest.content().readableBytes());

            HttpPostRequestEncoder httpPostRequestEncoder = new HttpPostRequestEncoder(postRequest, false);
            httpPostRequestEncoder.addBodyAttribute("token", token);
            httpPostRequestEncoder.addBodyAttribute("token_type_hint", tokenTypeHint);
            HttpRequest request = httpPostRequestEncoder.finalizeRequest();

            future.channel().writeAndFlush(request).sync();

            future.channel().closeFuture().sync();
        }
        catch (InterruptedException | URISyntaxException | HttpPostRequestEncoder.ErrorDataEncoderException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }

        clientTokenData = introspectionHandler.getIntrospectionResponseBody();
        return introspectionHandler.getIntrospectionResult();
    }
}
