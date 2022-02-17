package pl.edu.agh.dp.oauth2server.datafetch;

import io.netty.handler.codec.http.FullHttpRequest;
import org.json.JSONObject;
import pl.edu.agh.dp.oauth2server.database.MongoDatabaseFacade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataFetcher {
    public static JSONObject fetchData(FullHttpRequest clientRequest, JSONObject clientTokenData){
        String userID = clientTokenData.getString("client_id");
        List<String> requestedData = new ArrayList<>(Arrays.asList(clientRequest.headers().get("Requested-Data").split(" ")));

        JSONObject data = new JSONObject();
        for (String dataType : requestedData) {
            switch (dataType) {
                case "username" -> data.put(dataType, MongoDatabaseFacade.getUsersUsername(userID));
                case "mail" -> data.put(dataType, MongoDatabaseFacade.getUsersMail(userID));
                case "post" -> data.put(dataType, MongoDatabaseFacade.getUsersPosts(userID));
            }
        }

        return data;
    }
}
