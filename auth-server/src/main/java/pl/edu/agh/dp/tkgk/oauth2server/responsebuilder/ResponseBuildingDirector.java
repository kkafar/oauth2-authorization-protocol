package pl.edu.agh.dp.tkgk.oauth2server.responsebuilder;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.json.JSONObject;

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
        JSONObject errorJson = new JSONObject("error", errorMessage);

        return constructJsonResponse(builder, errorJson, HttpResponseStatus.INTERNAL_SERVER_ERROR, false);
    }

    public FullHttpResponse constructJsonBadRequestErrorResponse(ResponseBuilder<JSONObject> builder,
                                                                 String errorMessage,
                                                                 boolean includeCacheAndPragmaControl)
    {
        JSONObject errorJson = new JSONObject("error", errorMessage);

        return constructJsonResponse(builder, errorJson, HttpResponseStatus.BAD_REQUEST, includeCacheAndPragmaControl);
    }

    public FullHttpResponse constructUrlEncodedServerErrorResponse(ResponseBuilder<String> builder,
                                                                   String redirectUri, String error,
                                                                   String errorUri, String state)
    {
        String urlMessage = redirectUri + "?error=" + error + "&error_uri=" + errorUri + "&state=" + state;

        builder.setHttpResponseStatus(HttpResponseStatus.FOUND);
        builder.setMessage(urlMessage);

        return builder.getResponse();
    }

    public FullHttpResponse constructHtmlServerErrorResponse(ResponseBuilder<String> builder, String errorMessage,
                                                             HttpResponseStatus responseStatus)
    {
        String htmlContent = buildSimpleHtml("Error 500", errorMessage);

        return constructHtmlErrorResponse(builder, htmlContent, responseStatus);
    }

    public FullHttpResponse constructHtmlErrorResponse(ResponseBuilder<String> builder, String htmlContent,
                                                       HttpResponseStatus responseStatus)
    {
        builder.setHttpResponseStatus(responseStatus);
        builder.setMessage(htmlContent);

        return builder.getResponse();
    }

    public String buildSimpleHtml(String title, String content){
        return "<html><body><h1>" +
                title +
                "</h1>" +
                content.replaceAll("\n", "</br>") +
                "</body></html>";
    }
}
