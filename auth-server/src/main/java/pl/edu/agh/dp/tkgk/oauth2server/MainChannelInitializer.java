package pl.edu.agh.dp.tkgk.oauth2server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.ssl.SslContext;
import pl.edu.agh.dp.tkgk.oauth2server.authrequest.*;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.pong.PingHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.TokenGrantTypeDispatcher;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.TokenGrantTypesHandlerChainsBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.tokenendpoint.TokenRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection.FetchTokenDataHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection.TokenDataResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.tokenintrospection.TokenIntrospectionRequestValidator;
import pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation.TokenRevocationHandler;
import pl.edu.agh.dp.tkgk.oauth2server.tokenrevocation.TokenRevocationRequestValidator;

import java.util.HashMap;

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

        // Token Request
        Handler<FullHttpRequest, HttpPostRequestDecoder> tokenRequestValidator = new TokenRequestValidator();
        Handler<HttpPostRequestDecoder, ?> tokenGrantTypeDispatcher = new TokenGrantTypeDispatcher();
        tokenRequestValidator.setNext(tokenGrantTypeDispatcher);

        endpointHandlerMap.put("/token", tokenRequestValidator);

        // Ping
        Handler<FullHttpRequest, ?> pingHandler = new PingHandler();
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

        // to make sure that handler chains serving different grant types on token endpoint are built before
        // the server receives any requests from the clients
        TokenGrantTypesHandlerChainsBuilder.buildChains();
    }
}
