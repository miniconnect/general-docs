package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository;

import java.util.List;
import java.util.Optional;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.PostComment;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    public Optional<PostComment> findByPostIdAndId(long postId, long id);

    public List<PostComment> findAllByPostIdOrderByCreatedAt(long postId);

}
