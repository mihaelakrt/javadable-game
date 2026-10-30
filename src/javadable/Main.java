package javadable;

import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import javadable.ui.GameFrame;

/** Start here. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--selftest".equals(args[0])) {
            SelfTest.run();
            return;
        }
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("ToolTip.background", new java.awt.Color(0x19, 0x00, 0x19));
            UIManager.put("ToolTip.foreground", new java.awt.Color(0xFB, 0xE4, 0xD8));
        } catch (Exception ignored) {
            // the game paints itself, the look and feel barely matters
        }
        ToolTipManager.sharedInstance().setInitialDelay(350);
        SwingUtilities.invokeLater(() -> new GameFrame().setVisible(true));
    }
}
