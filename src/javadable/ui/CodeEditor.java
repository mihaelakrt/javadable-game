package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/** A small code editor with line numbers and just enough syntax colour. */
public class CodeEditor extends JPanel {

    private static final char QUOTE = 34;
    private static final char APOSTROPHE = 39;

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "int", "for", "if", "else", "while", "String", "boolean", "double", "char",
            "new", "return", "break", "continue", "true", "false", "null", "do", "long",
            "float", "void", "final", "switch", "case", "default", "Math", "Integer"));

    private static final Set<String> API = new HashSet<>(Arrays.asList("p", "print", "in", "W", "H"));

    private final JTextPane pane = new JTextPane();
    private final Gutter gutter = new Gutter();
    private Runnable onChange;
    private boolean highlighting;

    public CodeEditor() {
        setOpaque(false);
        setLayout(new BorderLayout());

        pane.setFont(codeFont());
        pane.setOpaque(false);
        pane.setForeground(Palette.CREAM);
        pane.setCaretColor(Palette.ROSE);
        pane.setSelectionColor(Palette.alpha(Palette.MAUVE, 150));
        pane.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(pane);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setRowHeaderView(gutter);
        scroll.getRowHeader().setOpaque(false);
        SlimScrollBar.apply(scroll);
        add(scroll, BorderLayout.CENTER);

        pane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private static Font codeFont() {
        Font consolas = new Font("Consolas", Font.PLAIN, 15);
        if (consolas.getFamily().toLowerCase().contains("consolas")) return consolas;
        return new Font(Font.MONOSPACED, Font.PLAIN, 15);
    }

    private void changed() {
        if (highlighting) return;
        SwingUtilities.invokeLater(() -> {
            highlight();
            gutter.revalidate();
            gutter.repaint();
            if (onChange != null) onChange.run();
        });
    }

    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    public String getCode() {
        return pane.getText();
    }

    public void setCode(String code) {
        pane.setText(code == null ? "" : code);
        pane.setCaretPosition(pane.getDocument().getLength());
        highlight();
    }

    /** Drops text in at the caret, used by the palette. */
    public void insert(String text) {
        pane.replaceSelection(text);
        pane.requestFocusInWindow();
    }

    public JTextPane textPane() {
        return pane;
    }

    @Override
    public void requestFocus() {
        pane.requestFocusInWindow();
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        Graphics2D g2 = Palette.smooth(g.create());
        Glass.dark(g2, this, 0, 0, getWidth(), getHeight(), 20, 238, 60);
        g2.dispose();
    }

    // ---------------------------------------------------------- highlighting

    private void highlight() {
        highlighting = true;
        try {
            StyledDocument document = pane.getStyledDocument();
            String text = pane.getText().replace("\r", "");
            document.setCharacterAttributes(0, Math.max(1, text.length()),
                    style(Palette.CREAM, false), true);

            int i = 0;
            while (i < text.length()) {
                char c = text.charAt(i);

                if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                    int end = text.indexOf('\n', i);
                    if (end < 0) end = text.length();
                    document.setCharacterAttributes(i, end - i,
                            style(Palette.alpha(Palette.ROSE, 140), false), true);
                    i = end;
                } else if (c == QUOTE || c == APOSTROPHE) {
                    int end = i + 1;
                    while (end < text.length() && text.charAt(end) != c) end++;
                    end = Math.min(text.length(), end + 1);
                    document.setCharacterAttributes(i, end - i,
                            style(new Color(0xEF, 0xC9, 0x5C), false), true);
                    i = end;
                } else if (Character.isDigit(c)) {
                    int end = i;
                    while (end < text.length()
                            && (Character.isDigit(text.charAt(end)) || text.charAt(end) == '.')) {
                        end++;
                    }
                    document.setCharacterAttributes(i, end - i,
                            style(new Color(0xE6, 0x8F, 0xC0), false), true);
                    i = end;
                } else if (Character.isJavaIdentifierStart(c)) {
                    int end = i;
                    while (end < text.length() && Character.isJavaIdentifierPart(text.charAt(end))) {
                        end++;
                    }
                    String word = text.substring(i, end);
                    if (KEYWORDS.contains(word)) {
                        document.setCharacterAttributes(i, end - i, style(Palette.ROSE, true), true);
                    } else if (API.contains(word)) {
                        document.setCharacterAttributes(i, end - i,
                                style(new Color(0x7F, 0xD1, 0xC6), true), true);
                    }
                    i = end;
                } else {
                    i++;
                }
            }
        } catch (Exception ignored) {
            // colouring is a nicety, never a reason to break typing
        } finally {
            highlighting = false;
        }
    }

    private SimpleAttributeSet style(Color color, boolean bold) {
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setForeground(attributes, color);
        StyleConstants.setBold(attributes, bold);
        return attributes;
    }

    /** The little column of line numbers down the left. */
    private final class Gutter extends JComponent {

        @Override
        public Dimension getPreferredSize() {
            FontMetrics metrics = pane.getFontMetrics(pane.getFont());
            int lines = Math.max(1, pane.getText().split("\n", -1).length);
            return new Dimension(44, Math.max(lines * metrics.getHeight() + 20, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Palette.smooth(g.create());
            g2.setColor(new java.awt.Color(255, 255, 255, 12));
            g2.fillRect(0, 0, getWidth(), getHeight());

            FontMetrics metrics = pane.getFontMetrics(pane.getFont());
            int lineHeight = metrics.getHeight();
            int lines = Math.max(1, pane.getText().split("\n", -1).length);

            g2.setFont(pane.getFont().deriveFont(Font.PLAIN, 12f));
            g2.setColor(Palette.alpha(Palette.MAUVE, 200));
            for (int line = 1; line <= lines; line++) {
                String label = String.valueOf(line);
                int x = getWidth() - 12 - g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x, 10 + metrics.getAscent() + (line - 1) * lineHeight);
            }
            g2.dispose();
        }
    }
}
