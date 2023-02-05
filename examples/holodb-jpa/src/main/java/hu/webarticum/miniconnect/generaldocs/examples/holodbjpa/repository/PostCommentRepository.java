package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.PostComment;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface PostCommentRepository extends CrudRepository<PostComment, Long> {
}
