package pl.edu.agh.dp.tkgk.oauth2server.errorsendpoint;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.io.FileNotFoundException;
import java.util.Optional;

public class ErrorsPageHandler extends BaseHandler<FullHttpRequest, Void> {

    public static final String ERRORS_PAGE_HTML_PATH = "html/errors_page.html";

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        Optional<String> errorsPageHtmlOptional = tryToLoadErrorsPageHtml();
        if(errorsPageHtmlOptional.isPresent()){
            FullHttpResponse errorsPageResponse =
                    AuthorizationServerUtil.buildSimpleHttpResponse(HttpResponseStatus.OK, errorsPageHtmlOptional.get());
            return errorsPageResponse;
        }

        String errorDescription = "Couldn't load file " + ERRORS_PAGE_HTML_PATH;
        return AuthorizationServerUtil.buildServerErrorResponse(errorDescription);
    }

    private Optional<String> tryToLoadErrorsPageHtml() {
        String errorsPageHtml = null;
        try {
            errorsPageHtml = AuthorizationServerUtil.loadTextResource(ERRORS_PAGE_HTML_PATH);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(errorsPageHtml);
    }

}
