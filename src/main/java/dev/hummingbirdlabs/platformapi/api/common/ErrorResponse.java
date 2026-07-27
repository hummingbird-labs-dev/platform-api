package dev.hummingbirdlabs.platformapi.api.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String kind,
    String status,
    String message,
    String reason,
    Details details,
    int code
) {
    public ErrorResponse(String message, String reason, int code) {
        this("Status", "Failure", message, reason, null, code);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Details(
        String kind,
        String name,
        String cause
    ) {}
}
