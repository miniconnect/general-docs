package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.rest;

import java.net.URI;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.uri.UriBuilder;

public final class RestUtil {

    private RestUtil() {
        // utility class
    }

    public static <T extends HasId> HttpResponse<T> createdResponse(String basePath, T body) {
        Object id = body.getId();
        if (id == null) {
            throw new IllegalStateException("Created entity has no id");
        }
        URI location = UriBuilder.of(basePath).path(id.toString()).build();
        return HttpResponse.created(body).headers(h -> h.location(location));
    }

}

