package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.repository;

import hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model.GalleryImage;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface GalleryImageRepository extends CrudRepository<GalleryImage, Long> {
}
