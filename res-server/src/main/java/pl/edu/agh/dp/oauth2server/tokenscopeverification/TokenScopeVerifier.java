package pl.edu.agh.dp.oauth2server.tokenscopeverification;

import io.netty.handler.codec.http.FullHttpRequest;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TokenScopeVerifier {
    public static boolean isScopeSufficient(FullHttpRequest clientRequest, JSONObject clientTokenData) {
        List<String> requestedScope = getScopeAsList(clientRequest);
        List<String> accessibleScope = getScopeAsList(clientTokenData);
        accessibleScope.add("");
        return accessibleScope.containsAll(requestedScope) || accessibleScope.contains("all");
    }

    private static List<String> getScopeAsList(FullHttpRequest request) {
        String[] scope = request.headers().get("Requested-Data").split(" ");
        return new ArrayList<String>(Arrays.asList(scope));
    }

    private static List<String> getScopeAsList(JSONObject data) {
        String[] scope = data.getString("scope").split(" ");
        return new ArrayList<String>(Arrays.asList(scope));
    }
}
