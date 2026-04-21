package hu.webarticum.miniconnect.generaldocs.examples.benchmarkanimation;

import javax.swing.UIManager;

public class SwingSetup {

    private SwingSetup() {
    }

    static void configureLookAndFeel() {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                setLookAndFeel(info.getClassName());
                return;
            }
        }
    }

    private static void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to set Nimbus look and feel", e);
        }
    }
}
