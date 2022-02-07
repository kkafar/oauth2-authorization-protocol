package pl.edu.agh.dp.oauth2server.tokenverification;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import org.json.JSONObject;

public class IntrospectionHandler extends ChannelInboundHandlerAdapter {
    private boolean introspectionResult;

    public IntrospectionHandler() {
        super();
        this.introspectionResult = false;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpResponse response = (FullHttpResponse) msg;
        //System.out.println("response:\n" + response + "\n");
        JSONObject responseBody = new JSONObject(response.content().toString(CharsetUtil.UTF_8));
        introspectionResult = responseBody.has("active") && responseBody.get("active").equals("true");

        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable exception) {
        exception.printStackTrace();
        ctx.close();
    }

    public synchronized boolean getIntrospectionResult() {
        return introspectionResult;
    }
}
