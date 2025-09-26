package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.rest;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.Author;
import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.Category;
import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.Post;
import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository.AuthorRepository;
import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository.CategoryRepository;
import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository.PostRepository;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Controller(PostController.PATH)
@Tag(name = "Posts", description = "Endpoints for accessing posts")
class PostController {

    static final String PATH = "/posts";

    private final EntityManager entityManager;

    private final PostRepository postRepository;

    private final CategoryRepository categoryRepository;

    private final AuthorRepository authorRepository;


    public PostController(EntityManager entityManager, PostRepository postRepository, CategoryRepository categoryRepository, AuthorRepository authorRepository) {
        this.entityManager = entityManager;
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
    }

    @Get("/{?categoryId,authorId}")
    @Transactional
    public Page<PostDto> list(
                @QueryValue("categoryId") @Nullable Long categoryId,
                @QueryValue("authorId") @Nullable Long authorId,
                @QueryValue(value = "page", defaultValue = "0") @Nullable Integer page,
                @QueryValue(value = "size", defaultValue = "20") @Nullable Integer size) {
        return postRepository.findOptionally(categoryId, authorId, Pageable.from(page, size)).map(PostDto::from);
    }

    @Get("/{id}")
    @Transactional
    public Optional<PostDto> get(@PathVariable Long id) {
        return postRepository.findById(id).map(PostDto::from);
    }

    @io.micronaut.http.annotation.Post("/")
    @Status(HttpStatus.CREATED)
    @Transactional
    public HttpResponse<PostDto> create(PostDto postDto) {
        Post post = postDto.toPost(categoryRepository, authorRepository);
        Post savedPost = postRepository.save(post);
        return RestUtil.createdResponse(PATH, PostDto.from(savedPost));
    }

    @Put("/{id}")
    @Transactional
    public PostDto update(@PathVariable Long id, PostDto postDto) {
        Post post = postRepository.findById(id).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No such post"));
        Long givenId = postDto.getId();
        if (givenId != null && givenId != id) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Changing 'id' is not allowed");
        }
        postDto.mergeToPost(post, categoryRepository, authorRepository);
        Post savedPost = postRepository.update(post);
        postRepository.flush();
        entityManager.refresh(savedPost);
        return PostDto.from(savedPost);
    }


    @Serdeable
    public static class PostDto implements HasId {

        private final Long id;

        private final Long categoryId;

        private final Long authorId;

        private final String title;

        private final String htmlContent;

        private final Set<String> tags;


        @JsonCreator
        public PostDto(
                @JsonProperty(value = "id", required = false) Long id,
                @JsonProperty(value = "categoryId", required = false) Long categoryId,
                @JsonProperty(value = "authorId", required = false) Long authorId,
                @JsonProperty(value = "title", required = false) String title,
                @JsonProperty(value = "htmlContent", required = false) String htmlContent,
                @JsonProperty(value = "tags", required = false) Set<String> tags) {
            this.categoryId = categoryId;
            this.authorId = authorId;
            this.id = id;
            this.title = title;
            this.htmlContent = htmlContent;
            this.tags =
                    (tags != null && !tags.isEmpty()) ?
                    Collections.unmodifiableSet(new TreeSet<>(tags)) :
                    Collections.emptySet();
        }

        public static PostDto from(Post post) {
            return new PostDto(post.getId(), post.getCategoryId(), post.getAuthorId(), post.getTitle(), post.getHtmlContent(), post.getTags());
        }

        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        @JsonInclude(Include.ALWAYS)
        @Override
        public Long getId() {
            return id;
        }

        @JsonInclude(Include.ALWAYS)
        public Long getCategoryId() {
            return categoryId;
        }

        @JsonInclude(Include.ALWAYS)
        public Long getAuthorId() {
            return authorId;
        }

        @JsonInclude(Include.ALWAYS)
        public String getTitle() {
            return title;
        }

        @JsonInclude(Include.ALWAYS)
        public String getHtmlContent() {
            return htmlContent;
        }

        @JsonInclude(Include.ALWAYS)
        public Set<String> getTags() {
            return tags;
        }

        public Post toPost(CategoryRepository categoryRepository, AuthorRepository authorRepository) {
            Post post = new Post();
            mergeToPost(post, categoryRepository, authorRepository);
            return post;
        }

        public void mergeToPost(Post post, CategoryRepository categoryRepository, AuthorRepository authorRepository) {
            if (id != null) {
                post.setId(id);
            }
            if (categoryId != null) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "No such category"));
                post.setCategory(category);
            }
            if (authorId != null) {
                Author author = authorRepository.findById(authorId)
                        .orElseThrow(() -> new HttpStatusException(HttpStatus.BAD_REQUEST, "No such author"));
                post.setAuthor(author);
            }
            if (title != null) {
                post.setTitle(title);
            }
            if (htmlContent != null) {
                post.setHtmlContent(htmlContent);
            }
            if (tags != null) {
                post.setTags(tags);
            }
        }

    }

}
