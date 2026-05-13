package com.minicompiler.exception;

/**
 * Thrown when a requested resource cannot be found in the data store.
 * Produces a standardized message handled by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param resource the entity type that was not found (e.g. {@code Snippet})
     * @param id       the ID that yielded no result
     */
    public ResourceNotFoundException(String resource, Long id) {
        super("%s with id %d not found".formatted(resource, id));
    }
}