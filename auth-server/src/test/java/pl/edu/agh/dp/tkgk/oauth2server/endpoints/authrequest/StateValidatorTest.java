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
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.requestwithparametershandlers.StateValidator;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateValidatorTest {

    @Mock
    private BaseHandler<HttpRequestWithParameters, Void> nextHandlerMock;
    @InjectMocks
    private StateValidator stateValidator;

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
    public void whenStateParameterIsMissing_shouldReturnRedirectResponseToCorrectFragment(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = stateValidator.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(StateValidator.STATE_IS_MISSING_FRAGMENT));
    }

    @Test
    public void whenStateParameterIsMalformed_shouldReturnRedirectResponseToCorrectFragment(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("state", List.of("als\nas"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = stateValidator.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(StateValidator.STATE_IS_MALFORMED_FRAGMENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ala","mak545ota","ka la sd ","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    public void whenStateParameterIsRight_shouldCallNextHandler(String state){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("state", List.of(state));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        stateValidator.handle(request);

        Mockito.verify(nextHandlerMock).handle(request);

    }

}