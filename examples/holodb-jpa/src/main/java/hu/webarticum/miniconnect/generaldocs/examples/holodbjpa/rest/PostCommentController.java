package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.Post;
import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.PostComment;
import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository.PostCommentRepository;
import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository.PostRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

@Controller(PostCommentController.PATH_TEMPLATE)
@Tag(name = "Comments", description = "Endpoints for accessing comments for each post")
class PostCommentController {

    static final String PATH_TEMPLATE = "/posts/{postId}/comments";

    private final PostRepository postRepository;

    private final PostCommentRepository postCommentRepository;


    public PostCommentController(PostRepository postRepository, PostCommentRepository postCommentRepository) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
    }

    @Get("/")
    @Transactional
    public List<PostCommentDto> list(@PathVariable long postId) {
        checkPostId(postId);
        return postCommentRepository.findAllByPostIdOrderByCreatedAt(postId).stream().map(PostCommentDto::from).toList();
    }

    @Get("/{id}")
    @Transactional
    public Optional<PostCommentDto> get(@PathVariable long postId, @PathVariable Long id) {
        checkPostId(postId);
        return postCommentRepository.findByPostIdAndId(postId, id).map(PostCommentDto::from);
    }

    @io.micronaut.http.annotation.Post("/")
    @Status(HttpStatus.CREATED)
    @Transactional
    public HttpResponse<PostCommentDto> create(@PathVariable long postId, PostCommentDto postCommentDto) {
        Post post = getPostById(postId);
        PostComment postComment = postCommentDto.toPostComment(post);
        PostComment savedPostComment = postCommentRepository.save(postComment);
        String path = PATH_TEMPLATE.replace("{postId}", "" + postId);
        return RestUtil.createdResponse(path, PostCommentDto.from(savedPostComment));
    }

    @Put("/{id}")
    @Transactional
    public PostCommentDto update(@PathVariable long postId, @PathVariable Long id, PostCommentDto postCommentDto) {
        Post post = getPostById(postId);
        PostComment postComment = postCommentRepository.findByPostIdAndId(postId, id).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No such comment"));
        Long givenId = postCommentDto.getId();
        if (givenId != null && givenId != id) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Changing 'id' is not allowed");
        }
        postCommentDto.mergeToPostComment(postComment, post);
        PostComment savedPostComment = postCommentRepository.update(postComment);
        postCommentRepository.flush();
        return PostCommentDto.from(savedPostComment);
    }

    private void checkPostId(long postId) {
        if (!postRepository.existsById(postId)) {
            throw createNoSuchPostException();
        }
    }

    private Post getPostById(long postId) {
        return postRepository.findById(postId).orElseThrow(this::createNoSuchPostException);
    }

    private HttpStatusException createNoSuchPostException() {
        return new HttpStatusException(HttpStatus.NOT_FOUND, "No such post");
    }


    @Serdeable
    public static class PostCommentDto implements HasId {

        private final Long id;

        private final LocalDateTime createdAt;

        private final LocalDateTime updatedAt;

        private final String username;

        private final String content;


        @JsonCreator
        public PostCommentDto(
                @JsonProperty(value = "id", required = false) Long id,
                @JsonProperty(value = "createdAt", required = false) LocalDateTime createdAt,
                @JsonProperty(value = "updatedAt", required = false) LocalDateTime updatedAt,
                @JsonProperty(value = "username", required = false) String username,
                @JsonProperty(value = "content", required = false) String content) {
            this.id = id;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.username = username;
            this.content = content;
        }

        public static PostCommentDto from(PostComment comment) {
            return new PostCommentDto(comment.getId(), comment.getCreatedAt(), comment.getUpdatedAt(), comment.getUsername(), comment.getContent());
        }

        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        @JsonInclude(Include.ALWAYS)
        @Override
        public Long getId() {
            return id;
        }

        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        @JsonInclude(Include.ALWAYS)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        @JsonInclude(Include.ALWAYS)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        @JsonInclude(Include.ALWAYS)
        public String getUsername() {
            return username;
        }

        @JsonInclude(Include.ALWAYS)
        public String getContent() {
            return content;
        }

        public PostComment toPostComment(Post post) {
            PostComment postComment = new PostComment();
            mergeToPostComment(postComment, post);
            return postComment;
        }

        public void mergeToPostComment(PostComment postComment, Post post) {
            if (id != null) {
                postComment.setId(id);
            }
            if (createdAt != null) {
                postComment.setCreatedAt(createdAt);
            }
            if (updatedAt != null) {
                postComment.setUpdatedAt(updatedAt);
            }
            if (username != null) {
                postComment.setUsername(username);
            }
            if (content != null) {
                postComment.setContent(content);
            }
            if (post != null) {
                postComment.setPost(post);
            }
        }

    }

}
