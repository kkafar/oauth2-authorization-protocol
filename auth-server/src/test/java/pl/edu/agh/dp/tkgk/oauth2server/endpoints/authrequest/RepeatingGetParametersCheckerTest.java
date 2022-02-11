package pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest;

import io.netty.handler.codec.http.FullHttpRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.endpoints.authrequest.RepeatingGetParametersChecker;

class RepeatingGetParametersCheckerTest {

    @Mock
    private BaseHandler<FullHttpRequest, Void> nextHandlerMock;
    @InjectMocks
    private RepeatingGetParametersChecker repeatingGetParametersChecker;

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
    @ValueSource(strings =
            {
                    "https://student.agh.edu.pl/~karczyk/dp/auth_endpoint_errors/",
                    "https://netty.io/4.0/api/io/netty/handler/codec/http/QueryStringEncoder.html",
                    "https://127.0.0.1:8080/authorize?redirect_uri=https%3A%2F%2Fwww%2Egoogle%2Epl&scope=all&state=all-good&code_challenge=aAxC5toEZRIpElxqRt71Mf58ov54QjlK71UFO1nF5Lo&code_challenge_method=sha256&client_id=some_fake_client_id&response_type=code",
                    "https://127.0.0.1:8080/authorize?state=all-good&ala=ma_kota"
            })
    public void whenGivenRequestWithNoRepeatingGetParameters_shouldCallNextHandler(String uri){
        FullHttpRequest request = getFullHttpRequestMock(uri);
        repeatingGetParametersChecker.handle(request);
        Mockito.verify(nextHandlerMock).handle(request);

    }

    @ParameterizedTest
    @ValueSource(strings =
            {
                    "https://127.0.0.1:8080/authorize?state=all-good&ala=ma_kota&ala=ma_kota",
                    "https://127.0.0.1:8080/authorize?ala=ma_kota&state=all-good&ala=ma_kota",
                    "https://127.0.0.1:8080/authorize?ala=ma_kota&ala=ma_kota&state=all-good",
                    "https://127.0.0.1:8080/authorize?ala=ma_kota&ala=ma_kota&state=all-good&state=all-good",
                    "https://127.0.0.1:8080/authorize?ala=ma_kota&state=all-good&state=all-good&state=all-good"
            })
    public void whenGivenRequestWithWrongContentType_shouldNotCallNextHandler(String uri){
        FullHttpRequest request = getFullHttpRequestMock(uri);
        repeatingGetParametersChecker.handle(request);
        Mockito.verifyNoInteractions(nextHandlerMock);
    }


    @NotNull
    private FullHttpRequest getFullHttpRequestMock(String uri) {
        FullHttpRequest request = Mockito.mock(FullHttpRequest.class);
        Mockito.when(request.uri()).thenReturn(uri);
        return request;
    }

}