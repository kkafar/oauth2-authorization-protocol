package pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;


public class UrlEncodedResponseBuilder extends ResponseBuilder<String> {

    public UrlEncodedResponseBuilder() {
        this.reset();
    }

    @Override
    public void reset() {
        super.reset();
        setHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        setHeader(HttpHeaderNames.CONTENT_LENGTH, 0);
    }

    @Override
    public void setMessage(String message) {
        setHeader(HttpHeaderNames.LOCATION, message);
    }

}
