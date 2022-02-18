package pl.edu.agh.dp.tkgk.oauth2server.endpoints.autherrorpage;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.util.HtmlWithTitleAndContent;
import pl.edu.agh.dp.tkgk.oauth2server.server.util.AuthorizationServerUtil;

import java.io.FileNotFoundException;
import java.util.Optional;

public class ErrorPageResponder extends BaseHandler<FullHttpRequest, Void> {

    private final ResponseBuilder<String> builder;

    public ErrorPageResponder(){
        builder = new ResponseWithCustomHtmlBuilder();
    }

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        Optional<String> errorPageContent = getErrorPageContent();
        if(errorPageContent.isEmpty()){
            builder.setMessage("Server error");
            builder.setHttpResponseStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return builder.getResponse();
        }

        builder.setMessage(errorPageContent.get());
        builder.setHttpResponseStatus(HttpResponseStatus.OK);
        return builder.getResponse();

    }

    private Optional<String> getErrorPageContent() {
        try {
            String content = AuthorizationServerUtil.loadTextResource("html/error_page.html");
            return Optional.of(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
