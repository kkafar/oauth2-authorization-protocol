package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.requestwithparametershandlers.RedirectionUriVerifier;
import pl.edu.agh.dp.tkgk.oauth2server.model.Client;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedirectionUriVerifierTest {
    @Mock
    private BaseHandler<HttpRequestWithParameters, Void> nextHandlerMock;
    @InjectMocks
    private RedirectionUriVerifier redirectionUriVerifier;

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
    public void whenGivenRequestWithNoRedirectUri_shouldReturnRedirectResponseToErrorPageWithCorrectFragment(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("client_id", List.of("super_app"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = redirectionUriVerifier.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(RedirectionUriVerifier.REDIRECT_URI_IS_MISSING_FRAGMENT));

    }

    @Test
    public void whenGivenRequestWithNoClientId_shouldReturnRedirectResponseToErrorPageWithCorrectFragment(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("redirect_uri", List.of("httpnt"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        FullHttpResponse response = redirectionUriVerifier.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(RedirectionUriVerifier.CLIENT_ID_IS_MISSING_FRAGMENT));

    }

    @Test
    public void whenGivenRequestWithUnknownClientId_shouldReturnRedirectResponseToErrorPageWithCorrectFragment(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("client_id", List.of("redirect_uri"));
        parameters.put("redirect_uri", List.of("httpnt"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        Database database = Mockito.mock(Database.class);
        redirectionUriVerifier.setDatabase(database);

        FullHttpResponse response = redirectionUriVerifier.handle(request);

        assertNotNull(response);
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(RedirectionUriVerifier.UNKNOWN_CLIENT_ID_FRAGMENT));

    }

    @Test
    public void whenGivenRequestWithRedirectUriNotMatchingClient_shouldReturnRedirectResponseToErrorPageWithCorrectFragment(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("client_id", List.of("super_app"));
        parameters.put("redirect_uri", List.of("httpsnt"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        Database database = Mockito.mock(Database.class);
        Client client = new Client("super_app", "https", List.of("all"));
        Mockito.when(database.fetchClient("super_app")).thenReturn(Optional.of(client));
        redirectionUriVerifier.setDatabase(database);

        FullHttpResponse response = redirectionUriVerifier.handle(request);

        assertNotNull(response);
        Mockito.verify(database).fetchClient("super_app");
        String errorUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertTrue(errorUri.contains(RedirectionUriVerifier.CLIENT_ID_REDIRECT_URI_MISMATCH_FRAGMENT));

    }

    @Test
    public void whenGivenRequestWithRedirectUriMatchingClient_shouldCallNextHandler(){
        HashMap<String, List<String>> parameters = new HashMap<>();
        parameters.put("client_id", List.of("super_app"));
        parameters.put("redirect_uri", List.of("https"));
        HttpRequestWithParameters request = new HttpRequestWithParameters(null,parameters,null,null);

        Database database = Mockito.mock(Database.class);
        Client client = new Client("super_app", "https", List.of("all"));
        Mockito.when(database.fetchClient("super_app")).thenReturn(Optional.of(client));
        redirectionUriVerifier.setDatabase(database);

        redirectionUriVerifier.handle(request);

        Mockito.verify(database).fetchClient("super_app");
        Mockito.verify(nextHandlerMock).handle(request);

    }




}