package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;

import static org.junit.jupiter.api.Assertions.*;

class IdentityVerifierTest {

    @Mock
    private BaseHandler<AuthorizationRequest, Void> nextHandlerMock;
    @InjectMocks
    private IdentityVerifier identityVerifier;

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
    public void whenNeitherSessionIdNorCredentialsArePresent_thenShouldReturnLoginPageResponse(){
        AuthorizationRequest request = AuthorizationRequestMother.getRequestWithNeitherSessionIdNorCredentials();
        Database database = Mockito.mock(Database.class);
        identityVerifier.setDatabase(database);

        FullHttpResponse response = identityVerifier.handle(request);

        String contentType = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        assertNotNull(contentType);
        assertTrue(HttpHeaderValues.TEXT_HTML.contentEquals(contentType));
        assertTrue(new String(response.content().array()).contains(request.uri));
    }

    @Test
    public void whenSessionIdIsPresentButNotValid_thenShouldReturnLoginPage(){
        AuthorizationRequest request = AuthorizationRequestMother.getRequestWithSessionAndNoCredentials();
        Database database = Mockito.mock(Database.class);
        identityVerifier.setDatabase(database);

        FullHttpResponse response = identityVerifier.handle(request);

        String contentType = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        assertNotNull(contentType);
        assertTrue(HttpHeaderValues.TEXT_HTML.contentEquals(contentType));
        assertTrue(new String(response.content().array()).contains(request.uri));
    }

    @Test
    public void whenOnlyCredentialsArePresentButNotValid_thenShouldReturnResponseWithCorrectErrorUriFragment(){
        AuthorizationRequest request = AuthorizationRequestMother.getRequestWithNoSessionButPresentCredentials();
        Database database = Mockito.mock(Database.class);
        identityVerifier.setDatabase(database);

        FullHttpResponse response = identityVerifier.handle(request);

        String redirectUri = response.headers().get(HttpHeaderNames.LOCATION);
        assertNotNull(redirectUri);
        assertTrue(redirectUri.contains(IdentityVerifier.INVALID_CREDENTIALS_FRAGMENT));
    }

    @Test
    public void whenOnlyCredentialsArePresentAndAreValid_thenShouldReturnResponseWithSetCookie(){
        AuthorizationRequest request = AuthorizationRequestMother.getRequestWithNoSessionButPresentCredentials();
        Database database = Mockito.mock(Database.class);
        Mockito.when(database.areCredentialsValid(request.credentials)).thenReturn(true);
        Mockito.when(database.createNewSession(request.credentials.login())).thenReturn("session_ala");
        identityVerifier.setDatabase(database);

        FullHttpResponse response = identityVerifier.handle(request);

        String cookies = response.headers().get(HttpHeaderNames.SET_COOKIE);
        assertNotNull(cookies);
        assertTrue(cookies.contains("session_id"));
    }

    @Test
    public void whenCredentialsArePresentAndAreValidButSessionIdIsInvalid_thenShouldReturnResponseWithSetCookie(){
        AuthorizationRequest request = AuthorizationRequestMother.getRequestWithSessionAndCredentials();
        Database database = Mockito.mock(Database.class);
        Mockito.when(database.areCredentialsValid(request.credentials)).thenReturn(true);
        Mockito.when(database.createNewSession(request.credentials.login())).thenReturn("session_ala");
        identityVerifier.setDatabase(database);

        FullHttpResponse response = identityVerifier.handle(request);

        String cookies = response.headers().get(HttpHeaderNames.SET_COOKIE);
        assertNotNull(cookies);
        assertTrue(cookies.contains("session_id"));
    }

    @Test
    public void whenSessionIdIsValid_thenShouldCallNextHandler(){
        AuthorizationRequest request = AuthorizationRequestMother.getRequestWithSessionAndNoCredentials();
        Database database = Mockito.mock(Database.class);
        Mockito.when(database.isSessionIdValid(request.sessionId)).thenReturn(true);
        identityVerifier.setDatabase(database);

        identityVerifier.handle(request);

        Mockito.verify(nextHandlerMock).handle(request);


    }




}