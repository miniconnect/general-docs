package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.Post;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    public Page<Post> findAll(Pageable pageable);

    public Page<Post> findByCategoryId(long categoryId, Pageable pageable);

    public Page<Post> findByAuthorId(long authorId, Pageable pageable);

    public Page<Post> findByCategoryIdAndAuthorId(long categoryId, long authorId, Pageable pageable);

    public default Page<Post> findOptionally(@Nullable Long categoryId, @Nullable Long authorId, Pageable pageable) {
        if (categoryId != null && authorId != null) {
            return findByCategoryIdAndAuthorId(categoryId, authorId, pageable);
        } else if (categoryId != null) {
            return findByCategoryId(categoryId, pageable);
        } else if (authorId != null) {
            return findByAuthorId(authorId, pageable);
        } else {
            return findAll(pageable);
        }
    }

}
