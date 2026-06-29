package tutorialmaker.properties;

import tutorialmaker.tiles.LabelTile;
import javax.swing.*;
import java.awt.*;

public class LabelPropertiesPanel extends JPanel {

    public LabelPropertiesPanel(LabelTile tile) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        add(PropUtil.sectionLabel("Label"));

        JPanel textRow = PropUtil.rowBorder();
        JTextField tf = new JTextField(tile.getText());
        tf.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tf.addActionListener(e -> tile.setText(tf.getText()));
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { tile.setText(tf.getText()); }
        });
        textRow.add(PropUtil.rowLabel("Text"), BorderLayout.WEST);
        textRow.add(tf, BorderLayout.CENTER);
        add(textRow);

        add(PropUtil.sep());
        add(PropUtil.sectionLabel("Colours"));

        JPanel bgRow = PropUtil.row();
        bgRow.add(PropUtil.rowLabel("Fill"));
        bgRow.add(PropUtil.colorBtn(tile.getBgColor(), c -> tile.setBgColor(c)));
        add(bgRow);

        JPanel fgRow = PropUtil.row();
        fgRow.add(PropUtil.rowLabel("Text"));
        fgRow.add(PropUtil.colorBtn(tile.getTextColor(), c -> tile.setTextColor(c)));
        add(fgRow);
    }
}
