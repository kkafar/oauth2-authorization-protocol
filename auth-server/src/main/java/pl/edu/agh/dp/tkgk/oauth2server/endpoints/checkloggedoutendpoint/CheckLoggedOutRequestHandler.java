package pl.edu.agh.dp.tkgk.oauth2server.endpoints.checkloggedoutendpoint;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.json.JSONObject;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.database.AuthorizationDatabaseProvider;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpParameters;
import pl.edu.agh.dp.tkgk.oauth2server.model.util.HttpRequestError;
import pl.edu.agh.dp.tkgk.oauth2server.requestbodydecoder.HttpPostRequestBodyDecoder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.ResponseBuildingDirector;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.JsonResponseBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckLoggedOutRequestHandler extends BaseHandler<HttpPostRequestDecoder, Object> {

    private final ResponseBuildingDirector director = new ResponseBuildingDirector();
    private final ResponseBuilder<JSONObject> responseBuilder = new JsonResponseBuilder();

    private final Logger logger = Logger.getGlobal();

    private boolean loggedOutByAdmin;

    @Override
    public FullHttpResponse handle(HttpPostRequestDecoder decoder) {
        HttpPostRequestBodyDecoder bodyDecoder = new HttpPostRequestBodyDecoder(decoder);
        try {
            if (!userLoginValid(bodyDecoder)) {
                return director.constructJsonBadRequestErrorResponse(responseBuilder, HttpRequestError.INVALID_REQUEST, false);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage());
            return director.constructJsonServerErrorResponse(responseBuilder, e.getMessage());
        }

        JSONObject content = new JSONObject().put("logged_out_by_admin", loggedOutByAdmin);
        return director.constructJsonResponse(responseBuilder, content, HttpResponseStatus.OK, false);
    }

    private boolean userLoginValid(HttpPostRequestBodyDecoder bodyDecoder) throws IOException {
        Optional<String> optionalUserLogin = bodyDecoder.fetchAttribute(HttpParameters.USER_LOGIN);

        if (optionalUserLogin.isPresent()) {
            String userLogin = optionalUserLogin.get();

            Database database = AuthorizationDatabaseProvider.getInstance();
            Optional<Credentials> optionalCredentials = database.getUserCredentialsAndResetLoggedOutStatus(userLogin);

            if (optionalCredentials.isEmpty()) return false;

            loggedOutByAdmin = optionalCredentials.get().getLoggedOutByAdmin();
            return true;
        }
        return false;
    }
}
