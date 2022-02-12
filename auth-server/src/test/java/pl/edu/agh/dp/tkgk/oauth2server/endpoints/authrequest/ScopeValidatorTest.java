package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.jetbrains.annotations.NotNull;
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
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.requestwithparametershandlers.ScopeValidator;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ScopeValidatorTest {

    @Mock
    private BaseHandler<HttpRequestWithParameters, Void> nextHandlerMock;
    @InjectMocks
    private ScopeValidator scopeValidator;

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
    public void whenScopeIsNotPresent_thenReturnRedirectResponseWithCorrectFragment(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("state", List.of("sunny"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = scopeValidator.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(AuthErrorFragments.SCOPE_IS_MISSING_FRAGMENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ala", "all ala", "all some 3er", "rrr some all", "me and cat", "aaaaaaaaaaaaaaaaaaaaaaaa", ""})
    public void whenScopeEntryIsUnknown_thenShouldReturnRedirectResponseWithCorrectFragment(String scope){
        HttpRequestWithParameters request = getHttpRequestWithParametersMock(scope);

        FullHttpResponse response = scopeValidator.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(AuthErrorFragments.UNKNOWN_SCOPE_FRAGMENT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"all", "some", "me", "x", "all some", "x some all", "all some me x"})
    public void whenScopeEntryAreKnown_thenShouldCallNextHandler(String scope){
        HttpRequestWithParameters request = getHttpRequestWithParametersMock(scope);
        scopeValidator.handle(request);

        Mockito.verify(nextHandlerMock).handle(request);

    }

    @NotNull
    private HttpRequestWithParameters getHttpRequestWithParametersMock(String scope) {
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("http"));
        parameters.put("client_id", List.of("super_app"));
        parameters.put("state", List.of("sunny"));
        parameters.put("scope", List.of(scope));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        Database database = Mockito.mock(Database.class);
        Client client = new Client("super_app", "http", List.of("all", "some", "me", "x"));
        Mockito.when(database.fetchClient("super_app")).thenReturn(Optional.of(client));
        scopeValidator.setDatabase(database);
        return request;
    }


}