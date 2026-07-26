package dev.hummingbirdlabs.platformapi.api.v1.images;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImageMetadata(
    String apiVersion,
    String kind,
    String repository,
    String tag,
    String digest,
    String mediaType,
    Size size,
    Config config,
    List<Layer> layers,
    Instant created,
    String author,
    Map<String, String> labels
) {
    public ImageMetadata(String repository, String tag, String digest) {
        this("v1", "Image", repository, tag, digest, null, null, null, null, null, null, null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Size(long compressed, long uncompressed) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Config(String architecture, String os, String osVersion) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Layer(String digest, String mediaType, long size) {}
}
