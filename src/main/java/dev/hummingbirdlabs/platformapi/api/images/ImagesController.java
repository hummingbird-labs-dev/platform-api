package dev.hummingbirdlabs.platformapi.api.images;

import dev.hummingbirdlabs.platformapi.api.common.ApiResponse;
import dev.hummingbirdlabs.platformapi.api.common.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Images", description = "Container image repository management")
public class ImagesController {
    private final RegistryClient registryClient;

    public ImagesController(RegistryClient registryClient) {
        this.registryClient = registryClient;
    }

    /**
     * List all image repositories
     */
    @GetMapping("/repositories")
    @Operation(summary = "List all image repositories", 
        description = "Returns a list of all repositories available in the container registry")
    public ResponseEntity<?> listRepositories(
        @RequestParam(defaultValue = "50") int limit,
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(required = false) String search
    ) {
        try {
            List<String> allRepos = registryClient.listRepositories();
            
            // Filter by search if provided
            List<String> filteredRepos = allRepos.stream()
                .filter(repo -> search == null || repo.contains(search))
                .toList();

            // Apply pagination
            List<String> paginatedRepos = filteredRepos.stream()
                .skip(offset)
                .limit(Math.min(limit, 500))
                .toList();

            // Convert to ImageRepository objects
            List<ImageRepository> repositories = paginatedRepos.stream()
                .map(name -> new ImageRepository(
                    name,
                    "registry.lan.hummingbirdlabs.dev/" + name,
                    registryClient.listTags(name),
                    registryClient.listTags(name).size(),
                    0,
                    null,
                    Instant.now(),
                    0
                ))
                .toList();

            ApiResponse<ImageRepository> response = new ApiResponse<>("RepositoryList", repositories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to list repositories: " + e.getMessage(), 
                    "InternalError", 500));
        }
    }

    /**
     * Get repository details with all tags
     */
    @GetMapping("/repositories/{repository}")
    @Operation(summary = "Get repository details", 
        description = "Returns details for a specific repository including all available tags")
    public ResponseEntity<?> getRepository(@PathVariable String repository) {
        try {
            List<String> tags = registryClient.listTags(repository);
            if (tags.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Repository not found: " + repository, 
                        "NotFound", 404));
            }

            ImageRepository repo = new ImageRepository(
                repository,
                "registry.lan.hummingbirdlabs.dev/" + repository,
                tags,
                tags.size(),
                0,
                null,
                Instant.now(),
                0
            );

            ApiResponse<ImageRepository> response = new ApiResponse<>("Repository", List.of(repo));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to get repository: " + e.getMessage(), 
                    "InternalError", 500));
        }
    }

    /**
     * Get image metadata by tag
     */
    @GetMapping("/repositories/{repository}/{tag}")
    @Operation(summary = "Get image metadata", 
        description = "Returns metadata for a specific image identified by repository and tag")
    public ResponseEntity<?> getImageMetadata(
        @PathVariable String repository,
        @PathVariable String tag
    ) {
        try {
            String digest = registryClient.getManifestDigest(repository, tag);
            if (digest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Image not found: " + repository + ":" + tag, 
                        "NotFound", 404));
            }

            ImageMetadata metadata = new ImageMetadata(repository, tag, digest);
            ApiResponse<ImageMetadata> response = new ApiResponse<>("Image", List.of(metadata));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to get image metadata: " + e.getMessage(), 
                    "InternalError", 500));
        }
    }
}
