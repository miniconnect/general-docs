package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.Post;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface PostRepository extends CrudRepository<Post, Long> {
}
