package pl.edu.agh.dp.tkgk.oauth2server.responsebuilder.util;

public class HtmlWithTitleAndContent {
    private final String html;

    public HtmlWithTitleAndContent(String title, String content) {
        html = constructHtml(title, content);
    }

    private String constructHtml(String title, String content) {
        return "<html><body><h1>" +
                title +
                "</h1>" +
                content.replaceAll("\n", "</br>") +
                "</body></html>";
    }

    public String getHtml() {
        return html;
    }
}
