package pl.edu.agh.dp.tkgk.oauth2server.authrequest;

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
import pl.edu.agh.dp.tkgk.oauth2server.BaseHandler;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeChallengeValidatorTest {

    @Mock
    private BaseHandler<HttpRequestWithParameters, Void> nextHandlerMock;
    @InjectMocks
    private CodeChallengeValidator codeChallengeValidator;

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
    public void whenCodeChallengeIsNotPresent_thenShouldReturnResponseContainingCorrectFragment(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("state", List.of("sunny"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = codeChallengeValidator.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(CodeChallengeValidator.CODE_CHALLENGE_IS_MISSING_FRAGMENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"as c", " ala", "12\n3", "g\r5g5", "pla#in w", "s256!plain"})
    public void whenCodeChallengeIsMalformed_thenShouldReturnResponseContainingCorrectFragment(String code){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("state", List.of("sunny"));
        parameters.put("code_challenge", List.of(code));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = codeChallengeValidator.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(CodeChallengeValidator.INVALID_CODE_CHALLENGE_FRAGMENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"asc", "ala", "123", "g5g5", "plain w", "s256 plain"})
    public void whenCodeChallengeMethodIsUnknown_thenShouldReturnResponseContainingCorrectFragment(String codeMethod){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("state", List.of("sunny"));
        parameters.put("code_challenge", List.of("plain"));
        parameters.put("code_challenge_method", List.of(codeMethod));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = codeChallengeValidator.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(CodeChallengeValidator.UNKNOWN_CODE_CHALLENGE_METHOD_FRAGMENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plain", "s256"})
    public void whenEverythingIsRight_thenShouldCallNextHandler(String codeMethod){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("state", List.of("sunny"));
        parameters.put("code_challenge", List.of("challenge"));
        parameters.put("code_challenge_method", List.of(codeMethod));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        codeChallengeValidator.handle(request);

        Mockito.verify(nextHandlerMock).handle(request);
    }

    @Test
    public void whenOnlyChallengeMethodIsMissing_thenShouldCallNextHandler(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("state", List.of("sunny"));
        parameters.put("code_challenge", List.of("challenge"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        codeChallengeValidator.handle(request);

        Mockito.verify(nextHandlerMock).handle(request);
    }

}