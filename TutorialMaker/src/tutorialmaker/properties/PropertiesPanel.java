package tutorialmaker.properties;

import tutorialmaker.tiles.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class PropertiesPanel extends JPanel {

    private JPanel contentArea;
    private JLabel titleLabel;

    public PropertiesPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));

        titleLabel = new JLabel("Properties");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setBorder(new EmptyBorder(8, 10, 8, 10));
        titleLabel.setOpaque(true);
        add(titleLabel, BorderLayout.NORTH);

        contentArea = new JPanel();
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(8);
        add(scroll, BorderLayout.CENTER);

        showEmpty();
    }

    public void showEmpty() {
        titleLabel.setText("Properties");
        contentArea.removeAll();
        JLabel hint = new JLabel("<html><center>Select a tile<br>to see its properties</center></html>");
        hint.setForeground(new Color(130, 130, 130));
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setBorder(new EmptyBorder(30, 10, 10, 10));
        contentArea.add(hint);
        refresh();
    }

    public void showTile(TileComponent tile) {
        contentArea.removeAll();
        if (tile instanceof TextTile) {
            titleLabel.setText("Text Properties");
            contentArea.add(new TextPropertiesPanel((TextTile) tile));
        } else if (tile instanceof ImageTile) {
            titleLabel.setText("Image Properties");
            contentArea.add(new ImagePropertiesPanel((ImageTile) tile));
        } else if (tile instanceof LabelTile) {
            titleLabel.setText("Label Properties");
            contentArea.add(new LabelPropertiesPanel((LabelTile) tile));
        }
        contentArea.add(Box.createVerticalStrut(8));
        contentArea.add(new CommonPropertiesPanel(tile));
        contentArea.add(Box.createVerticalGlue());
        refresh();
    }

    private void refresh() {
        contentArea.revalidate();
        contentArea.repaint();
    }
}
