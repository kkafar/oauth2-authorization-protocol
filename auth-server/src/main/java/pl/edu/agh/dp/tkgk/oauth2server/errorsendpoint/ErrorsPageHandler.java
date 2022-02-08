package pl.edu.agh.dp.tkgk.oauth2server.errorsendpoint;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pl.edu.agh.dp.tkgk.oauth2server.AuthorizationServerUtil;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;

import java.io.FileNotFoundException;
import java.util.Optional;

public class ErrorsPageHandler extends BaseHandler<FullHttpRequest, Void> {

    public static final String ERRORS_PAGE_HTML_PATH = "html/errors_page.html";

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<String> responseBuilder = new ResponseWithCustomHtmlBuilder();

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        Optional<String> errorsPageHtmlOptional = tryToLoadErrorsPageHtml();
        if(errorsPageHtmlOptional.isPresent()){
            return director.constructHtmlResponse(responseBuilder, errorsPageHtmlOptional.get(), HttpResponseStatus.OK);
        }

        String errorDescription = "Couldn't load file " + ERRORS_PAGE_HTML_PATH;
        return director.constructHtmlServerErrorResponse(responseBuilder, errorDescription);
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
