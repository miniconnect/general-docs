package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.Author;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    public Page<Author> findAll(Pageable pageable);

    public Page<Author> findByFirstname(String firstname, Pageable pageable);

    public Page<Author> findByLastname(String lastname, Pageable pageable);

    public Page<Author> findByFirstnameAndLastname(String firstname, String lastname, Pageable pageable);

    public default Page<Author> findOptionally(@Nullable String firstname, @Nullable String lastname, Pageable pageable) {
        if (firstname != null && lastname != null) {
            return findByFirstnameAndLastname(firstname, lastname, pageable);
        } else if (firstname != null) {
            return findByFirstname(firstname, pageable);
        } else if (lastname != null) {
            return findByLastname(lastname, pageable);
        } else {
            return findAll(pageable);
        }
    }

}
