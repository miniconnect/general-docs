package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model;

import hu.webarticum.holodb.jpa.annotation.HoloColumn;
import hu.webarticum.holodb.jpa.annotation.HoloColumnDummyTextKind;
import hu.webarticum.holodb.jpa.annotation.HoloColumnShuffleQuality;
import hu.webarticum.holodb.jpa.annotation.HoloTable;
import hu.webarticum.holodb.jpa.annotation.HoloWriteable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
@HoloTable(size = 20, writeable = HoloWriteable.WRITEABLE)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    @HoloColumn(valuesBundle = "lorem", shuffleQuality = HoloColumnShuffleQuality.NOOP)
    private String name;

    @Column(name = "description", nullable = false)
    @HoloColumn(valuesTextKind = HoloColumnDummyTextKind.PARAGRAPH)
    private String description;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
