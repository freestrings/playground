package fs.outbound;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Collections;

@Slf4j
public class BasicAuthRestTemplate extends RestTemplate {

    public BasicAuthRestTemplate(String username, String password) {
        super();
        addAuthentication(username, password);
    }

    private void addAuthentication(String username, String password) {
        if (username == null) {
            return;
        }
        setInterceptors(Collections.singletonList(new BasicAuthorizationInterceptor(username, password)));
    }

    private static class BasicAuthorizationInterceptor implements ClientHttpRequestInterceptor {

        private final String username;
        private final String password;

        public BasicAuthorizationInterceptor(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request,
                                            byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            byte[] token = Base64.getEncoder().encode((this.username + ":" + this.password).getBytes());
            request.getHeaders().add("Authorization", "Basic " + new String(token));
            traceRequest(request, body);
            // Response의 InputStream을 Intercepter에서 읽어 버리면 안된다
            ClientHttpResponse response = new ResponseWrapper(execution.execute(request, body));
            traceResponse(response);
            return response;
        }

        private void traceRequest(HttpRequest request, byte[] body) throws IOException {
            log.info("REQUEST: [{}][{}][{}][{}]",
                    request.getURI(), request.getMethod(), request.getHeaders(), new String(body, "UTF-8"));
        }

        private void traceResponse(ClientHttpResponse response) throws IOException {
            InputStream body = response.getBody();
            try {
                body.mark(0);
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(body, "UTF-8"));
                String line = br.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append('\n');
                    line = br.readLine();
                }
                log.info("RESPONSE: [{}][{}][{}]", response.getStatusCode(), response.getHeaders(), sb.toString());
            } finally {
                body.reset();
            }
        }

    }
}
