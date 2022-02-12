package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.requestwithparametershandlers.ResponseTypeVerifier;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTypeVerifierTest {

    @Mock
    private BaseHandler<HttpRequestWithParameters, Void> nextHandlerMock;
    @InjectMocks
    private ResponseTypeVerifier responseTypeVerifier;

    private AutoCloseable closeable;

    @BeforeEach
    public void init(){
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void deInit() throws Exception {
        closeable.close();
    }

    @Test
    public void whenResponseTypeIsMissing_shouldReturnRedirectResponseToErrorPageWithCorrectFragment(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("state", List.of("sunny"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = responseTypeVerifier.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(ResponseTypeVerifier.RESPONSE_TYPE_IS_MISSING_FRAGMENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "ala", "some_response", "hax", "1233", ")1923", "ko4rj34vu938235 9t239 b925t5"})
    public void whenResponseTypeIsUnknown_shouldReturnRedirectResponseToErrorPageWithCorrectFragment(String responseType){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("state", List.of("sunny"));
        parameters.put("response_type", List.of(responseType));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = responseTypeVerifier.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(ResponseTypeVerifier.UNKNOWN_RESPONSE_TYPE_FRAGMENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"code"})
    public void whenResponseTypeIsKnown_shouldCallNextHandler(String responseType){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("state", List.of("sunny"));
        parameters.put("response_type", List.of(responseType));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        responseTypeVerifier.handle(request);

        Mockito.verify(nextHandlerMock).handle(request);
    }



}