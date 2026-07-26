package dev.hummingbirdlabs.platformapi.api.v1.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    String apiVersion,
    String kind,
    Metadata metadata,
    List<T> items
) {
    public ApiResponse(String kind, List<T> items) {
        this(
            "v1",
            kind,
            new Metadata("v1", Instant.now(), items != null ? items.size() : 0),
            items
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Metadata(
        String resourceVersion,
        Instant timestamp,
        int total
    ) {}
}
