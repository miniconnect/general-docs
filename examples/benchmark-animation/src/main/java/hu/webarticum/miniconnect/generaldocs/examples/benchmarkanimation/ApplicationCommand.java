package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation;

import hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation.animation.Animation;
import io.micronaut.context.BeanContext;
import io.micronaut.data.connection.jdbc.advice.DelegatingDataSource;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import javax.swing.SwingUtilities;
import picocli.CommandLine.Command;

@Singleton
@Command(
        name = "benchmark-animation",
        description = "Benchmark animation for performance comparison",
        mixinStandardHelpOptions = true
)
public class ApplicationCommand implements Runnable {

    private final List<DatasourceDefinition> datasourceDefinitions;
    private final List<Animation> animations;
    private final BeanContext beanContext;

    public ApplicationCommand(
            BeanContext beanContext,
            List<DatasourceDefinition> datasourceDefinitions,
            List<Animation> animations) {
        this.beanContext = beanContext;
        this.datasourceDefinitions = datasourceDefinitions.stream()
                .sorted(Comparator.comparingInt(DatasourceDefinition::getOrder))
                .toList();
        this.animations = animations.stream()
                .sorted(Comparator.comparingInt(Animation::order))
                .toList();
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(() -> {
            SwingSetup.configureLookAndFeel();
            IntroWindow.open(
                    datasourceDefinitions,
                    animations,
                    (datasourceDefinition, animation) ->
                            AnimationWindow.open(resolveConnection(datasourceDefinition), animation)
            );
        });
    }

    private Connection resolveConnection(DatasourceDefinition datasourceDefinition) {
        DataSource dataSource;
        if ("default".equals(datasourceDefinition.name())) {
            dataSource = beanContext.getBean(DataSource.class);
        } else {
            dataSource = beanContext.getBean(
                    DataSource.class,
                    Qualifiers.byName(datasourceDefinition.name()));
        }

        try {
            return DelegatingDataSource.unwrapDataSource(dataSource).getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to open connection", e);
        }
    }

}
