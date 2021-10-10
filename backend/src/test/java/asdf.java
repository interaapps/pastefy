import org.javawebstack.httpclient.HTTPClient;

public class asdf {
    public static void main(String[] args) {
        HTTPClient httpClient = new HTTPClient();
        System.out.println(httpClient.get("http://js.gjni.eu").string());
    }
}
