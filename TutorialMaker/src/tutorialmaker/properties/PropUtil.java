package tutorialmaker.properties;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class PropUtil {

    // Use system colours everywhere — no custom dark theme
    public static final Color BG     = null;   // null = don't set, let L&F decide
    public static final Color FG     = null;
    public static final Color FG_DIM = new Color(100, 100, 100);
    public static final Color ACCENT = new Color(0, 100, 200);

    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(80, 80, 80));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(10, 8, 3, 8));
        return l;
    }

    public static JLabel rowLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setPreferredSize(new Dimension(70, 22));
        l.setMinimumSize(new Dimension(70, 22));
        return l;
    }

    public static JPanel row() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return p;
    }

    public static JPanel rowBorder() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        p.setBorder(new EmptyBorder(2, 8, 2, 8));
        return p;
    }

    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cb.setPreferredSize(new Dimension(120, 24));
        return cb;
    }

    public static JSpinner spinner(int val, int min, int max, int step) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(val, min, max, step));
        sp.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sp.setPreferredSize(new Dimension(70, 24));
        return sp;
    }

    /** Colour swatch — just a button showing the chosen colour */
    public static JButton colorBtn(Color initial, ColorCallback cb) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(36, 24));
        btn.setBackground(initial);
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(btn, "Choose colour", btn.getBackground());
            if (chosen != null) { btn.setBackground(chosen); cb.onColor(chosen); }
        });
        return btn;
    }

    public static JToggleButton toggle(String label, boolean initial, ToggleCallback cb) {
        JToggleButton btn = new JToggleButton(label, initial);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.addActionListener(e -> cb.onToggle(btn.isSelected()));
        return btn;
    }

    public static JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    public interface ColorCallback  { void onColor(Color c); }
    public interface ToggleCallback { void onToggle(boolean on); }
}
