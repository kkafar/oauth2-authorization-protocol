package pl.edu.agh.dp.tkgk.oauth2server.endpoints.adminpanel;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pl.edu.agh.dp.tkgk.oauth2server.common.BaseHandler;
import pl.edu.agh.dp.tkgk.oauth2server.common.DatabaseInjectable;
import pl.edu.agh.dp.tkgk.oauth2server.database.Database;
import pl.edu.agh.dp.tkgk.oauth2server.model.Credentials;
import pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.concretebuilders.ResponseWithCustomHtmlBuilder;
import pl.edu.agh.dp.tkgk.oauth2server.server.util.AuthorizationServerUtil;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminPageResponder extends BaseHandler<FullHttpRequest, Void> implements DatabaseInjectable {
    private Database database;
    private ResponseWithCustomHtmlBuilder builder = new ResponseWithCustomHtmlBuilder();

    @Override
    public FullHttpResponse handle(FullHttpRequest request) {
        List<String> users = getUsers();
        String adminPage, userFragment;
        try {
            adminPage = AuthorizationServerUtil.loadTextResource("html/admin_page.html");
            userFragment = AuthorizationServerUtil.loadTextResource("html/user_fragment.html");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        for(String user : users){
            String concreteUserFragment = userFragment;
            concreteUserFragment = concreteUserFragment.replace("$USER_NAME", user);
            adminPage = adminPage.replace("$USER_FRAGMENT", concreteUserFragment);
        }
        adminPage = adminPage.replace("$USER_FRAGMENT", "");

        builder.setHttpResponseStatus(HttpResponseStatus.OK);
        builder.setMessage(adminPage);

        return builder.getResponse();
    }

    private List<String> getUsers() {
        List<String> users = database.getUserLoginsWithActiveInfo().entrySet()
                .stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
        return users;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }
}
