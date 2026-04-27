package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JdbcSetupDialog extends JDialog {

    private static final int DEFAULT_WIDTH = 550;
    private static final int DEFAULT_HEIGHT = 270;
    private static final int INPUT_WIDTH = 320;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 35;

    private final JTextField jdbcUrlField;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton okButton;

    private JdbcSetupDialog(Window parent, Consumer<Connection> action) {
        super(parent, "JDBC connection");
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        setContentPane(panel);

        jdbcUrlField = createTextField();
        usernameField = createTextField();
        passwordField = new JPasswordField();
        Dimension passwordFieldPreferredSize = passwordField.getPreferredSize();
        passwordFieldPreferredSize.width = INPUT_WIDTH;
        passwordField.setPreferredSize(passwordFieldPreferredSize);

        okButton = new JButton("OK");
        Dimension okButtonPreferredSize = okButton.getPreferredSize();
        okButtonPreferredSize.width = BUTTON_WIDTH;
        okButtonPreferredSize.height = BUTTON_HEIGHT;
        okButton.setPreferredSize(okButtonPreferredSize);
        okButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okButton.setEnabled(false);
        okButton.addActionListener(event -> onOkButtonClicked(action));

        jdbcUrlField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent event) {
                updateOkButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateOkButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateOkButtonState();
            }

        });

        addLabel(panel, "JDBC URL:", 0);
        addInput(panel, jdbcUrlField, 0);
        addLabel(panel, "Username:", 1);
        addInput(panel, usernameField, 1);
        addLabel(panel, "Password:", 2);
        addInput(panel, passwordField, 2);

        GridBagConstraints okButtonConstraints = new GridBagConstraints();
        okButtonConstraints.gridx = 0;
        okButtonConstraints.gridy = 3;
        okButtonConstraints.gridwidth = 2;
        okButtonConstraints.anchor = GridBagConstraints.CENTER;
        okButtonConstraints.insets = new Insets(24, 0, 0, 0);
        panel.add(okButton, okButtonConstraints);
    }

    public static void open(Window parent, Consumer<Connection> action) {
        JdbcSetupDialog dialog = new JdbcSetupDialog(parent, action);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        Dimension preferredSize = textField.getPreferredSize();
        preferredSize.width = INPUT_WIDTH;
        textField.setPreferredSize(preferredSize);
        return textField;
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

    private void addInput(JPanel panel, JTextField input, int row) {
        GridBagConstraints inputConstraints = new GridBagConstraints();
        inputConstraints.gridx = 1;
        inputConstraints.gridy = row;
        inputConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputConstraints.insets = new Insets(0, 0, 10, 0);
        panel.add(input, inputConstraints);
    }

    private void updateOkButtonState() {
        okButton.setEnabled(!jdbcUrlField.getText().isEmpty());
    }

    private void onOkButtonClicked(Consumer<Connection> action) {
        String jdbcUrl = jdbcUrlField.getText().trim();
        String username = usernameField.getText();
        String password = String.valueOf(passwordField.getPassword());

        try {
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
            dispose();
            action.accept(connection);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Connection error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
