package dev.hummingbirdlabs.platformapi.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    String kind,
    Metadata metadata,
    List<T> items
) {
    public ApiResponse(String kind, List<T> items) {
        this(
            kind,
            new Metadata(Instant.now(), items != null ? items.size() : 0),
            items
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Metadata(
        Instant timestamp,
        int total
    ) {}
}
