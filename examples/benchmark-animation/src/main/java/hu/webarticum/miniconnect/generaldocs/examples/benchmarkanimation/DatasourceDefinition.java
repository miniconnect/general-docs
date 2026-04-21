package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.order.Ordered;
import io.micronaut.jdbc.JdbcDataSourceEnabled;

@EachProperty(value = "datasources", primary = "default")
@Requires(condition = JdbcDataSourceEnabled.class)
public class DatasourceDefinition implements Ordered {

    private final String name;
    private String description;
    private int order = Ordered.LOWEST_PRECEDENCE;

    public DatasourceDefinition(@Parameter String name) {
        this.name = name;
        this.description = name;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return description;
    }

}
