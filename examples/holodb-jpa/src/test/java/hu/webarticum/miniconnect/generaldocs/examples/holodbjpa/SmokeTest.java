package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
@Tag("smoke")
class SmokeTest {

    private static final String FIELD_ID = "id";

    private static final String FIELD_CATEGORY_ID = "categoryId";

    private static final String FIELD_AUTHOR_ID = "authorId";

    private static final String FIELD_TITLE = "title";

    private static final String FIELD_HTML_CONTENT = "htmlContent";

    private static final String FIELD_TAGS = "tags";


    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testGetAnExistingPost() {
        HttpResponse<JsonNode> response = client.toBlocking().exchange(HttpRequest.GET("/posts/1"), JsonNode.class);
        assertThat(response.getStatus().getCode()).isEqualTo(200);
        assertThat(response.body().get("id").getNumberValue()).isEqualTo(1);
    }

    @Test
    void testPostAndGetANewPost() {
        Map<String, Object> entityInputData = Map.of(
            "categoryId", 3,
            "authorId", 5,
            "title", "New Post",
            "htmlContent", "<p>Lorem ipsum</p>",
            "tags", Set.of("dolor", "sit", "amet")
        );
        Map<String, Object> requestBody = Map.of("postDto", entityInputData);
        MutableHttpRequest<Map<String, Object>> createRequest = HttpRequest.POST("/posts", requestBody).accept(MediaType.APPLICATION_JSON);
        HttpResponse<JsonNode> createResponse = client.toBlocking().exchange(createRequest, JsonNode.class);
        assertThat(createResponse.getStatus().getCode()).isEqualTo(201);
        checkPostBody(entityInputData, createResponse.body());
        String location = createResponse.header(HttpHeaders.LOCATION);
        System.out.println(location);
        HttpResponse<JsonNode> getResponse = client.toBlocking().exchange(HttpRequest.GET(location), JsonNode.class);
        checkPostBody(entityInputData, getResponse.body());
    }

    private void checkPostBody(Map<String, Object> entityInputData, JsonNode responseBody) {
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get(FIELD_ID).isNumber()).isTrue();
        assertThat(responseBody.get(FIELD_CATEGORY_ID).getNumberValue().intValue()).isEqualTo(entityInputData.get(FIELD_CATEGORY_ID));
        assertThat(responseBody.get(FIELD_AUTHOR_ID).getNumberValue().intValue()).isEqualTo(entityInputData.get(FIELD_AUTHOR_ID));
        assertThat(responseBody.get(FIELD_TITLE).getStringValue()).isEqualTo(entityInputData.get(FIELD_TITLE));
        assertThat(responseBody.get(FIELD_HTML_CONTENT).getStringValue()).isEqualTo(entityInputData.get(FIELD_HTML_CONTENT));
        assertThat(responseBody.get(FIELD_TAGS).isArray()).isTrue();
        @SuppressWarnings("unchecked")
        Set<String> tags = (Set<String>) entityInputData.get(FIELD_TAGS);
        assertThat(responseBody.get(FIELD_TAGS).values()).map(n -> n.getStringValue()).containsExactlyInAnyOrderElementsOf(tags);
    }

}
