package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.rest;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.Author;
import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository.AuthorRepository;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;

@Controller(AuthorController.PATH)
@Tag(name = "Authors", description = "Endpoints for accessing post authors")
class AuthorController {

    static final String PATH = "/authors";

    private final AuthorRepository authorRepository;


    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Get("/{?firstname,lastname}")
    @Transactional
    public Page<AuthorDto> list(
                @QueryValue("firstname") @Nullable String firstname,
                @QueryValue("lastname") @Nullable String lastname,
                @QueryValue(value = "page", defaultValue = "0") @Nullable Integer page,
                @QueryValue(value = "size", defaultValue = "20") @Nullable Integer size) {
        return authorRepository.findOptionally(firstname, lastname, Pageable.from(page, size)).map(AuthorDto::from);
    }

    @Get("/{id}")
    @Transactional
    public Optional<AuthorDto> get(@PathVariable Long id) {
        return authorRepository.findById(id).map(AuthorDto::from);
    }

    @Post("/")
    @Status(HttpStatus.CREATED)
    @Transactional
    public HttpResponse<AuthorDto> create(AuthorDto authorDto) {
        Author author = authorDto.toAuthor();
        Author savedAuthor = authorRepository.save(author);
        return RestUtil.createdResponse(PATH, AuthorDto.from(savedAuthor));
    }

    @Put("/{id}")
    @Transactional
    public AuthorDto update(@PathVariable Long id, AuthorDto authorDto) {
        Author author = authorRepository.findById(id).orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "No such author"));
        Long givenId = authorDto.getId();
        if (givenId != null && givenId != id) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Changing 'id' is not allowed");
        }
        authorDto.mergeToAuthor(author);
        Author savedAuthor = authorRepository.update(author);
        return AuthorDto.from(savedAuthor);
    }


    @Serdeable
    public static class AuthorDto implements HasId {

        private final Long id;

        private final String firstname;

        private final String lastname;


        @JsonCreator
        public AuthorDto(
                @JsonProperty(value = "id", required = false) Long id,
                @JsonProperty(value = "firstname", required = false) String firstname,
                @JsonProperty(value = "lastname", required = false) String lastname) {
            this.id = id;
            this.firstname = firstname;
            this.lastname = lastname;
        }

        public static AuthorDto from(Author author) {
            return new AuthorDto(author.getId(), author.getFirstname(), author.getLastname());
        }

        @Schema(accessMode = Schema.AccessMode.READ_ONLY)
        @JsonInclude(Include.ALWAYS)
        @Override
        public Long getId() {
            return id;
        }

        @JsonInclude(Include.ALWAYS)
        public String getFirstname() {
            return firstname;
        }

        @JsonInclude(Include.ALWAYS)
        public String getLastname() {
            return lastname;
        }

        public Author toAuthor() {
            Author author = new Author();
            mergeToAuthor(author);
            return author;
        }

        public void mergeToAuthor(Author author) {
            if (id != null) {
                author.setId(id);
            }
            if (firstname != null) {
                author.setFirstname(firstname);
            }
            if (lastname != null) {
                author.setLastname(lastname);
            }
        }

    }

}
