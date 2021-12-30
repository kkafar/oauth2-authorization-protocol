package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.util.HashSet;
import java.util.Set;

public class RepeatingGetParametersChecker extends BaseHandler<FullHttpRequest, FullHttpRequest> {


    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        if(areThereRepeatingParameters(request.uri())){
            FullHttpResponse response = buildRepeatingParametersResponse();
            return response;
        }

        FullHttpResponse nexHandlerResponse = next.handle(request);
        return nexHandlerResponse;
    }

    private FullHttpResponse buildRepeatingParametersResponse(){
        String pageContent = AuthorizationServerUtil.buildSimpleHtml("Error",
                "One or more parameters are repeated");
        FullHttpResponse repeatingParametersResponse = AuthorizationServerUtil.buildSimpleHttpResponse(HttpResponseStatus.OK, pageContent);
        return repeatingParametersResponse;
    }

    private boolean areThereRepeatingParameters(String uri){
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        return queryStringDecoder.parameters().size() != uri.split("&").length;
    }






}
