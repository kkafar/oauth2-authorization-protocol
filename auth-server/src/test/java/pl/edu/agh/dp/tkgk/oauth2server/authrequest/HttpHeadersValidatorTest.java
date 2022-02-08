package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

public class HttpHeadersValidatorTest {

    @Mock
    private BaseHandler<FullHttpRequest, Void> nextHandlerMock;
    @InjectMocks
    private HttpHeadersValidator headersValidator;

    private AutoCloseable closeable;

    @BeforeEach
    public void init(){
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void deInit() throws Exception {
        closeable.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/x-www-form-urlencoded", "text/html", "APPlication/x-www-form-urlencoded", "TEXT/htMl"})
    public void whenGivenRequestWithRightContentType_callNextHandler(String contentType){
        FullHttpRequest request = getFullHttpRequestMock(contentType);
        headersValidator.handle(request);
        Mockito.verify(nextHandlerMock).handle(request);

    }

    @ParameterizedTest
    @ValueSource(strings = {"application", "text/css", "audio/als", "someRandomType"})
    public void whenGivenRequestWithWrongContentType_dontCallNextHandler(String contentType){
        FullHttpRequest request = getFullHttpRequestMock(contentType);
        headersValidator.handle(request);
        Mockito.verifyNoInteractions(nextHandlerMock);
    }


    @NotNull
    private FullHttpRequest getFullHttpRequestMock(String contentType) {
        FullHttpRequest request = Mockito.mock(FullHttpRequest.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        Mockito.when(headers.get(HttpHeaderNames.CONTENT_TYPE)).thenReturn(contentType);
        Mockito.when(headers.contains(HttpHeaderNames.CONTENT_TYPE)).thenReturn(true);
        Mockito.when(request.headers()).thenReturn(headers);
        return request;
    }


}
