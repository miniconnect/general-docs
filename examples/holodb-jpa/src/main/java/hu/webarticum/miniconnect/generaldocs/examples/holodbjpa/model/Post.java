package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import hu.webarticum.holodb.jpa.annotation.HoloColumn;
import hu.webarticum.holodb.jpa.annotation.HoloTable;
import hu.webarticum.holodb.jpa.annotation.HoloVirtualColumn;

@Entity
@Table(name = "posts")
@HoloTable(size = 20)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "tag")
    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = { @JoinColumn(name = "post_id") })
    @HoloTable(size = 20)
    @HoloColumn(values = { "tag1", "tag2", "tag3" })
    @HoloVirtualColumn(name = "virt_col", type = String.class, values = { "v1", "v2" })
    private Set<String> tags;

    @ElementCollection
    @CollectionTable(name = "post_values")
    @JoinColumn(name = "post_id")
    @OrderBy("ord")
    @HoloTable(size = 30)
    @HoloVirtualColumn(name = "prop_x", type = String.class, values = { "x1", "x2", "x3" })
    private List<EmbeddedProperties> values;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private List<PostComment> comments;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
        name = "post_images", 
        joinColumns = { @JoinColumn(name = "post_id") }, 
        inverseJoinColumns = { @JoinColumn(name = "image_id") }
    )
    @HoloTable(size = 100)
    @HoloVirtualColumn(name = "img_x", type = Integer.class, valuesRange = { 1, 100 })
    private List<GalleryImage> images;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getTags() {
        return new HashSet<>(tags);
    }

    public void setTags(Set<String> tags) {
        this.tags = new HashSet<>(tags);
    }

    public List<EmbeddedProperties> getValues() {
        return new ArrayList<>(values);
    }

    public void setValues(List<EmbeddedProperties> values) {
        this.values = new ArrayList<>(values);
    }

    public List<PostComment> getComments() {
        return new ArrayList<>(comments);
    }

    public void setComments(List<PostComment> comments) {
        this.comments = new ArrayList<>(comments);
    }

    public List<GalleryImage> getImages() {
        return new ArrayList<>(images);
    }

    public void setImages(List<GalleryImage> images) {
        this.images = new ArrayList<>(images);
    }

}
