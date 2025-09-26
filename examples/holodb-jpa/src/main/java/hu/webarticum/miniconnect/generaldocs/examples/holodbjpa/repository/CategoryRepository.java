package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.Category;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
