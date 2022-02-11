package pl.edu.agh.dp.oauth2server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import pl.edu.agh.dp.oauth2server.datafetch.DataFetchHandler;
import pl.edu.agh.dp.oauth2server.requestvalidation.RequestValidationHandler;
import pl.edu.agh.dp.oauth2server.response.ResponseHandler;
import pl.edu.agh.dp.oauth2server.tokenscopeverification.TokenScopeVerificationHandler;
import pl.edu.agh.dp.oauth2server.tokenverification.TokenVerificationHandler;

public class ResourceServer {
    private final int port;

    public ResourceServer(int port) {
        this.port = port;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) {
                            SslContext sslContext = SSLContextManager.getResourceServerSslContext();

                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(sslContext.newHandler(channel.alloc()));
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(1048576, false));
                            pipeline.addLast(new RequestValidationHandler());
                            pipeline.addLast(new TokenVerificationHandler());
                            pipeline.addLast(new TokenScopeVerificationHandler());
                            pipeline.addLast(new DataFetchHandler());
                            pipeline.addLast(new ResponseHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
