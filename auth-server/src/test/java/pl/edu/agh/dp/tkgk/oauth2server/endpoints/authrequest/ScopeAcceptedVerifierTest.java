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

import static org.junit.jupiter.api.Assertions.*;

class ScopeAcceptedVerifierTest {

    @Mock
    private BaseHandler<AuthorizationRequest, Void> nextHandlerMock;
    @InjectMocks
    private ScopeAcceptedVerifier scopeAcceptedVerifier;

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
    public void whenScopeIsNotAccepted_thenShouldReturnAcceptScopePage(){
        AuthorizationRequest request = AuthorizationRequestMother.getRequestWithSessionAndNoCredentials();

        FullHttpResponse response = scopeAcceptedVerifier.handle(request);

        assertNotNull(response);
        String contentType = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        assertNotNull(contentType);
        assertTrue(HttpHeaderValues.TEXT_HTML.contentEquals(contentType));

        String content = new String(response.content().array());
        assertTrue(content.contains(request.uri));
    }

    @Test
    public void whenScopeIsAccepted_thenShouldCallNextHandler(){
        AuthorizationRequest request = AuthorizationRequestMother.getRequestWithSessionAndNoCredentialsAndAcceptedScope();

        scopeAcceptedVerifier.handle(request);

        Mockito.verify(nextHandlerMock).handle(request);
    }

}