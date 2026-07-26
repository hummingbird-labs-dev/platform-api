package dev.hummingbirdlabs.platformapi.api.v1.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String apiVersion,
    String kind,
    String status,
    String message,
    String reason,
    Details details,
    int code
) {
    public ErrorResponse(String message, String reason, int code) {
        this("v1", "Status", "Failure", message, reason, null, code);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Details(
        String kind,
        String name,
        String cause
    ) {}
}
