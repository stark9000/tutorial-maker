package tutorialmaker;

import tutorialmaker.canvas.CanvasPanel;
import tutorialmaker.toolbar.MainToolbar;
import tutorialmaker.properties.PropertiesPanel;
import javax.swing.*;
import java.awt.*;

public class TutorialMakerFrame extends JFrame {

    public TutorialMakerFrame() {
        super("Tutorial Maker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        PropertiesPanel propsPanel = new PropertiesPanel();
        CanvasPanel     canvasPanel = new CanvasPanel(propsPanel);
        MainToolbar     toolbar    = new MainToolbar(canvasPanel);

        JScrollPane scrollPane = new JScrollPane(canvasPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(60, 60, 60));
        scrollPane.getViewport().setBackground(new Color(60, 60, 60));
        canvasPanel.setScrollPane(scrollPane);   // enables viewport-aware tile placement

        JLabel statusBar = new JLabel("  Ready");
        statusBar.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(45, 45, 45));
        statusBar.setForeground(new Color(180, 180, 180));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        // Global keyboard shortcuts
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
                java.awt.event.InputEvent.CTRL_DOWN_MASK), "undo");
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y,
                java.awt.event.InputEvent.CTRL_DOWN_MASK), "redo");
        getRootPane().getActionMap().put("undo", new javax.swing.AbstractAction(){
            public void actionPerformed(java.awt.event.ActionEvent e){ canvasPanel.undo(); }});
        getRootPane().getActionMap().put("redo", new javax.swing.AbstractAction(){
            public void actionPerformed(java.awt.event.ActionEvent e){ canvasPanel.redo(); }});

        add(toolbar,     BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(propsPanel,  BorderLayout.EAST);
        add(statusBar,   BorderLayout.SOUTH);

        canvasPanel.setStatusBar(statusBar);
    }
}
