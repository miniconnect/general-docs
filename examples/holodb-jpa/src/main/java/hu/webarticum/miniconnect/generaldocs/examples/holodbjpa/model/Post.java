package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model;

import java.util.Set;
import java.util.TreeSet;

import hu.webarticum.holodb.jpa.annotation.HoloColumn;
import hu.webarticum.holodb.jpa.annotation.HoloColumnDummyTextKind;
import hu.webarticum.holodb.jpa.annotation.HoloTable;
import hu.webarticum.holodb.jpa.annotation.HoloWriteable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "posts")
@HoloTable(size = 1000, writeable = HoloWriteable.WRITEABLE)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "category_id", insertable = false, updatable = false)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Column(name = "author_id", insertable = false, updatable = false)
    private Long authorId;

    @Column(name = "title", nullable = false)
    @HoloColumn(valuesTextKind = HoloColumnDummyTextKind.TITLE)
    private String title;

    @Column(name = "html_content", nullable = false)
    @HoloColumn(valuesTextKind = HoloColumnDummyTextKind.HTML)
    private String htmlContent;

    @Column(name = "tag")
    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = { @JoinColumn(name = "post_id") })
    @HoloTable(size = 1500, writeable = HoloWriteable.WRITEABLE)
    @HoloColumn(values = { "educational", "news", "review", "tutorial" })
    private Set<String> tags;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getCategoryId() {
        if (categoryId != null) {
            return categoryId;
        } else if (category != null) {
            return category.getId();
        } else {
            return null;
        }
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Long getAuthorId() {
        if (authorId != null) {
            return authorId;
        } else if (author != null) {
            return author.getId();
        } else {
            return null;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public Set<String> getTags() {
        return new TreeSet<>(tags);
    }

    public void setTags(Set<String> tags) {
        this.tags = new TreeSet<>(tags);
    }

}
