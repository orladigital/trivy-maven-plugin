package br.com.orla.api;

import com.alibaba.fastjson2.JSON;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GithubTrivyRelease implements GithubTrivyReleaseApi {

    @Override
    public Release releaseByTag(String tag) {
        try {
            var httpClient = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .timeout(Duration.ofSeconds(10))
                    .uri(URI.create("https://api.github.com/repos/aquasecurity/trivy/releases/tags/".concat(tag)))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return JSON.parseObject(response.body(), Release.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
