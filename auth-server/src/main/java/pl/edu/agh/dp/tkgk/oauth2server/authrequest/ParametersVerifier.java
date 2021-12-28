package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.util.HashMap;

/**
 * Check if the following parameters exist and are valid:
 * <ul>
 *     <li>response_type</li>
 *     <li>scope</li>
 *     <li>state</li>
 *     <li>code_challenge</li>
 *     <li>code_challenge_method</li>
 * </ul>
 */

public class ParametersVerifier extends BaseHandler {
    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        HashMap<String,String> parameters = AuthorizationServerUtil.extractParameters(request);




        return null;
    }
}
