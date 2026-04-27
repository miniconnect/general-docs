package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation;

import hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation.animation.Animation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class IntroWindow extends JFrame {

    private enum DatasourceType {

        HOLODB("HoloDB embedded"),
        CUSTOM("Other JDBC connection")
        ;
    
        private final String label;

        private DatasourceType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

    }

    private static final int DEFAULT_WIDTH = 550;
    private static final int DEFAULT_HEIGHT = 270;
    private static final int COMBO_BOX_WIDTH = 320;
    private static final int BUTTON_WIDTH = 210;
    private static final int BUTTON_HEIGHT = 35;

    private final BiConsumer<Animation, Connection> action;
    private final Function<String, Connection> namedConnectionResolver;

    private final JComboBox<DatasourceType> datasourceTypeComboBox;
    private final JComboBox<Animation> animationComboBox;
    private final JButton startButton;

    private IntroWindow(
            List<Animation> animations,
            BiConsumer<Animation, Connection> action,
            Function<String, Connection> namedConnectionResolver) {
        super("Benchmark Animation");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.action = action;
        this.namedConnectionResolver = namedConnectionResolver;

        JPanel panel = new JPanel(new GridBagLayout());
        setContentPane(panel);

        datasourceTypeComboBox = new JComboBox<>(DatasourceType.values());
        Dimension comboBoxPreferredSize = datasourceTypeComboBox.getPreferredSize();
        comboBoxPreferredSize.width = COMBO_BOX_WIDTH;
        datasourceTypeComboBox.setPreferredSize(comboBoxPreferredSize);

        animationComboBox = new JComboBox<>(animations.toArray(Animation[]::new));
        Dimension animationComboBoxPreferredSize = animationComboBox.getPreferredSize();
        animationComboBoxPreferredSize.width = COMBO_BOX_WIDTH;
        animationComboBox.setPreferredSize(animationComboBoxPreferredSize);

        startButton = new JButton("Start animation");
        Dimension startButtonPreferredSize = startButton.getPreferredSize();
        startButtonPreferredSize.width = BUTTON_WIDTH;
        startButtonPreferredSize.height = BUTTON_HEIGHT;
        startButton.setPreferredSize(startButtonPreferredSize);
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startButton.addActionListener(event -> onStartClicked());

        addLabel(panel, "Database engine:", 0);
        addInput(panel, datasourceTypeComboBox, 0);
        addLabel(panel, "Animation to run:", 1);
        addInput(panel, animationComboBox, 1);

        GridBagConstraints startButtonConstraints = new GridBagConstraints();
        startButtonConstraints.gridx = 1;
        startButtonConstraints.gridy = 2;
        startButtonConstraints.anchor = GridBagConstraints.WEST;
        startButtonConstraints.insets = new Insets(24, 0, 0, 0);
        panel.add(startButton, startButtonConstraints);
    }

    public static void open(
            List<Animation> animations,
            BiConsumer<Animation, Connection> action,
            Function<String, Connection> namedConnectionResolver) {
        IntroWindow frame = new IntroWindow(animations, action, namedConnectionResolver);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    private void addLabel(JPanel panel, String text, int row) {
        JLabel label = new JLabel(text);
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.EAST;
        labelConstraints.insets = new Insets(0, 0, 10, 12);
        panel.add(label, labelConstraints);
    }

    private void addInput(JPanel panel, JComboBox<?> input, int row) {
        GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.gridx = 1;
        inputConstraints.gridy = row;
        inputConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputConstraints.insets = new Insets(0, 0, 10, 0);
        panel.add(input, inputConstraints);
    }

    private void onStartClicked() {
        DatasourceType datasourceType = (DatasourceType) datasourceTypeComboBox.getSelectedItem();
        Animation animation = (Animation) animationComboBox.getSelectedItem();
        if (datasourceType == DatasourceType.HOLODB) {
            showAnimation(namedConnectionResolver.apply("default"), animation);
        } else if (datasourceType == DatasourceType.CUSTOM) {
            JdbcSetupDialog.open(this, c -> showAnimation(c, animation));
        }
    }

    private void showAnimation(Connection connection, Animation animation) {
        dispose();
        action.accept(animation, connection);
    }

}
