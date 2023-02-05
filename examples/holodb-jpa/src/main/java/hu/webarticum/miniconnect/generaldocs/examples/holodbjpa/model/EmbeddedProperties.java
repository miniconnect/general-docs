package hu.webarticum.miniconnect.generaldocs.examples.holodbjpa.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EmbeddedProperties {

    @Column
    private Integer ord;

    @Column
    private String type;

    @Column
    private Long value;
    

    public Integer getOrd() {
        return ord;
    }

    public void getType(Integer ord) {
        this.ord = ord;
    }

    public String getType() {
        return type;
    }

    public void getType(String type) {
        this.type = type;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

}
