package dev.hummingbirdlabs.platformapi.api.v1.images;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RegistryClient {
    private static final Logger logger = LoggerFactory.getLogger(RegistryClient.class);
    private final String registryUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RegistryClient(
        @Value("${registry.url:http://registry.lan.hummingbirdlabs.dev}") String registryUrl
    ) {
        this.registryUrl = normalizeUrl(registryUrl);
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<String> listRepositories() {
        try {
            String url = registryUrl + "/v2/_catalog";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var catalog = objectMapper.readTree(response.body());
                List<String> repos = new ArrayList<>();
                if (catalog.has("repositories")) {
                    catalog.get("repositories").forEach(repo -> repos.add(repo.asText()));
                }
                return repos;
            } else {
                logger.warn("Registry returned status {}: {}", response.statusCode(), response.body());
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Failed to list repositories from registry", e);
            return List.of();
        }
    }

    public List<String> listTags(String repository) {
        try {
            String url = registryUrl + "/v2/" + repository + "/tags/list";
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var tagsResponse = objectMapper.readTree(response.body());
                List<String> tags = new ArrayList<>();
                if (tagsResponse.has("tags")) {
                    tagsResponse.get("tags").forEach(tag -> tags.add(tag.asText()));
                }
                return tags;
            } else if (response.statusCode() == 404) {
                logger.debug("Repository not found: {}", repository);
                return List.of();
            } else {
                logger.warn("Registry returned status {} for {}: {}", 
                    response.statusCode(), repository, response.body());
                return List.of();
            }
        } catch (Exception e) {
            logger.error("Failed to list tags for repository {}", repository, e);
            return List.of();
        }
    }

    public String getManifestDigest(String repository, String tag) {
        try {
            String url = registryUrl + "/v2/" + repository + "/manifests/" + tag;
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .header("Accept", "application/vnd.docker.distribution.manifest.v2+json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                var digest = response.headers().firstValue("Docker-Content-Digest");
                return digest.orElse(null);
            } else {
                logger.warn("Failed to get manifest for {}/{}: status {}", 
                    repository, tag, response.statusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to get manifest digest for {}/{}", repository, tag, e);
            return null;
        }
    }

    private String normalizeUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        return url.replaceAll("/$", "");
    }
}
