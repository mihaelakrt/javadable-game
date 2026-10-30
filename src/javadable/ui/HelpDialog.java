package javadable.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** The rules, in plain words. */
public class HelpDialog extends JDialog {

    private static final String TEXT =
            "Every level shows a target picture on the right. Your job is to write code that "
            + "paints the same picture on the left.\n\n"
            + "The editor is the body of a draw() method, so you write ordinary Java: loops, "
            + "conditions, arrays, Math, whatever you like.\n\n"
            + "The whole drawing language is one method:\n\n"
            + "    p(x, y, colour)\n"
            + "    p(x, y, colour, mark)\n\n"
            + "W and H give you the size of the grid, in(x, y) tells you whether a cell exists, "
            + "and print(...) writes to the Console tab.\n\n"
            + "Colours: ink, deep, plum, mauve, rose, cream, white, black, red, coral, orange, "
            + "gold, yellow, lime, green, mint, teal, cyan, sky, blue, navy, purple, pink, brown.\n\n"
            + "The palette bar under the grids writes a colour into your code when you click it. "
            + "Click PALETTE on its left to flip the bar over to the marks: stars, hearts, dots "
            + "and some emoji. A mark is the fourth argument of p, as in p(2, 2, \"gold\", \"*\").\n\n"
            + "The budget counts every character that is not whitespace. Finish under par for "
            + "three stars, inside the budget for two, and anything that matches the picture at "
            + "all keeps one. Indentation and line breaks are free, so lay the code out nicely.\n\n"
            + "Ctrl and Enter runs the sketch. Escape takes you back to the levels.\n\n"
            + "Four helpers can be bought with coins: Hint spells out the rule, Diff outlines the "
            + "cells that are still wrong, Elbow room adds fifty characters to the budget, and "
            + "Skip opens the next level without solving this one.\n\n"
            + "The daily puzzle is the same for every day. Solve one each day to grow the streak.";

    public HelpDialog(Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        try {
            setBackground(new java.awt.Color(0, 0, 0, 0));
        } catch (Exception ignored) {
            // per pixel translucency is not available everywhere
        }

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Palette.smooth(g.create());
                Glass.dark(g2, this, 0, 0, getWidth(), getHeight(), 26, 168, 120);
                g2.setFont(Palette.tracked(22, Font.BOLD, 0.1));
                g2.setColor(Palette.CREAM);
                Palette.centerString(g2, "How to play", getWidth() / 2.0, 44);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(62, 26, 18, 26));

        JTextArea text = new JTextArea(TEXT);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setForeground(Palette.alpha(Palette.CREAM, 235));
        text.setFont(Palette.font(14, Font.PLAIN));
        JScrollPane scroll = new JScrollPane(text);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        SlimScrollBar.apply(scroll);
        card.add(scroll, BorderLayout.CENTER);

        Pill close = new Pill("Got it", Pill.Style.PRIMARY);
        close.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        south.setOpaque(false);
        south.add(close);
        card.add(south, BorderLayout.SOUTH);

        setContentPane(card);
        setSize(new Dimension(620, 560));
        setLocationRelativeTo(owner);
    }
}
