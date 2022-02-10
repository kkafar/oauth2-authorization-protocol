package pl.edu.agh.dp.tkgk.oauth2server.responsebuilder;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.util.HtmlWithTitleAndContent;

public class ResponseBuildingDirector {

    public ResponseBuildingDirector() { }

    public FullHttpResponse constructJsonResponse(ResponseBuilder<JSONObject> builder, JSONObject content,
                                                  HttpResponseStatus responseStatus, boolean includeCacheAndPragmaControl)
    {
        builder.setHttpResponseStatus(responseStatus);
        builder.setMessage(content);

        if (includeCacheAndPragmaControl) {
            builder.includeCacheAndPragmaControlHeaders();
        }

        return builder.getResponse();
    }

    public FullHttpResponse constructJsonServerErrorResponse(ResponseBuilder<JSONObject> builder, String errorMessage) {
        JSONObject errorJson = new JSONObject().put("error", errorMessage);

        return constructJsonResponse(builder, errorJson, HttpResponseStatus.INTERNAL_SERVER_ERROR, false);
    }

    public FullHttpResponse constructJsonBadRequestErrorResponse(ResponseBuilder<JSONObject> builder,
                                                                 String errorMessage,
                                                                 boolean includeCacheAndPragmaControl)
    {
        JSONObject errorJson = new JSONObject().put("error", errorMessage);

        return constructJsonResponse(builder, errorJson, HttpResponseStatus.BAD_REQUEST, includeCacheAndPragmaControl);
    }

    private FullHttpResponse constructUrlEncodedResponse(ResponseBuilder<String> builder, String urlMessage,
                                                         HttpResponseStatus responseStatus)
    {
        builder.setHttpResponseStatus(responseStatus);
        builder.setMessage(urlMessage);

        return builder.getResponse();
    }

    public FullHttpResponse constructUrlEncodedErrorResponse(ResponseBuilder<String> builder,
                                                             String redirectUri, String error,
                                                             String errorUri, String state)
    {
        String urlMessage = redirectUri + "?error=" + error + "&error_uri=" + errorUri + "&state=" + state;

        return constructUrlEncodedResponse(builder, urlMessage, HttpResponseStatus.FOUND);
    }

    public FullHttpResponse constructUrlEncodedAuthCodeResponse(ResponseBuilder<String> builder, String redirectUri,
                                                                String state, String code)
    {
        String urlMessage = redirectUri + "?state=" + state + "&code=" + code;

        return constructUrlEncodedResponse(builder, urlMessage, HttpResponseStatus.FOUND);
    }

    public FullHttpResponse constructHtmlServerErrorResponse(ResponseBuilder<String> builder, String errorMessage)
    {
        String htmlContent = new HtmlWithTitleAndContent("Error 500", errorMessage).getHtml();

        return constructHtmlResponse(builder, htmlContent, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    public FullHttpResponse constructHtmlResponse(ResponseBuilder<String> builder, String htmlContent,
                                                       HttpResponseStatus responseStatus)
    {
        builder.setHttpResponseStatus(responseStatus);
        builder.setMessage(htmlContent);

        return builder.getResponse();
    }
}
