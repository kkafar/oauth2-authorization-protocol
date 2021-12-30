package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.nio.charset.StandardCharsets;

public class StateValidator extends BaseHandler<HttpRequestWithParameters, HttpRequestWithParameters> {
    @Override
    public FullHttpResponse handle(HttpRequestWithParameters request) {
        if(!request.urlParameters.containsKey("state")){
            return buildErrorResponse("state not preset");
        }

        String state = request.urlParameters.get("state").get(0);
        if(!isStateValid(state)){
            return buildErrorResponse( "state format is invalid");
        }

        return next.handle(request);
    }

    private boolean isStateValid(String state){
        return state.matches("[ -~]+");
    }

    private FullHttpResponse buildErrorResponse(String msg){
        ByteBuf content = Unpooled.copiedBuffer(AuthorizationServerUtil.buildSimpleHtml("State error",msg), StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
        return response;
    }
}
