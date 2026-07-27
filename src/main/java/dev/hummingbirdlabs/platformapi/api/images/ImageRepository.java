package dev.hummingbirdlabs.platformapi.api.images;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImageRepository(
    String name,
    String uri,
    List<String> tags,
    int tagCount,
    long totalSize,
    Instant created,
    Instant lastUpdated,
    int pullCount
) {}
