package tutorialmaker.properties;

import tutorialmaker.tiles.TextTile;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class TextPropertiesPanel extends JPanel {

    private static final String[] FONTS = {
        "SansSerif","Serif","Monospaced","Dialog","Arial","Times New Roman"
    };
    private static final Integer[] SIZES = {
        8,9,10,11,12,14,16,18,20,24,28,32,36,48,60,72
    };

    public TextPropertiesPanel(TextTile tile) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        add(PropUtil.sectionLabel("Font"));

        JComboBox<String> fontCombo = PropUtil.combo(FONTS);
        fontCombo.setSelectedItem(tile.getTileFont().getFamily());
        fontCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JPanel fontRow = PropUtil.rowBorder();
        fontRow.add(PropUtil.rowLabel("Family"), BorderLayout.WEST);
        fontRow.add(fontCombo, BorderLayout.CENTER);
        add(fontRow);

        JComboBox<Integer> sizeCombo = PropUtil.combo(SIZES);
        sizeCombo.setSelectedItem(tile.getTileFont().getSize());
        sizeCombo.setEditable(true);
        sizeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JPanel sizeRow = PropUtil.rowBorder();
        sizeRow.add(PropUtil.rowLabel("Size"), BorderLayout.WEST);
        sizeRow.add(sizeCombo, BorderLayout.CENTER);
        add(sizeRow);

        Font cur = tile.getTileFont();
        JPanel styleRow = PropUtil.row();
        JToggleButton boldBtn   = PropUtil.toggle("B", cur.isBold(),   on -> updateFont(tile, fontCombo, sizeCombo, on, cur.isItalic()));
        JToggleButton italicBtn = PropUtil.toggle("I", cur.isItalic(), on -> updateFont(tile, fontCombo, sizeCombo, cur.isBold(), on));
        boldBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        italicBtn.setFont(new Font("SansSerif", Font.ITALIC, 11));
        styleRow.add(PropUtil.rowLabel("Style"));
        styleRow.add(boldBtn); styleRow.add(italicBtn);
        add(styleRow);

        ActionListener fontUpdater = e -> updateFont(tile, fontCombo, sizeCombo, boldBtn.isSelected(), italicBtn.isSelected());
        fontCombo.addActionListener(fontUpdater);
        sizeCombo.addActionListener(fontUpdater);

        add(PropUtil.sep());
        add(PropUtil.sectionLabel("Colours"));

        JPanel tcRow = PropUtil.row();
        tcRow.add(PropUtil.rowLabel("Text"));
        tcRow.add(PropUtil.colorBtn(tile.getTextColor(), c -> tile.setTextColor(c)));
        add(tcRow);

        JPanel bgRow = PropUtil.row();
        bgRow.add(PropUtil.rowLabel("Background"));
        bgRow.add(PropUtil.colorBtn(tile.getBgColor(), c -> tile.setBgColor(c)));
        add(bgRow);

        add(PropUtil.sep());
        add(PropUtil.sectionLabel("Alignment"));

        JPanel alignRow = PropUtil.row();
        JComboBox<String> alignCombo = PropUtil.combo(new String[]{"Left","Center","Right"});
        alignCombo.setSelectedItem(tile.getAlignment());
        alignCombo.addActionListener(e -> tile.setAlignment((String)alignCombo.getSelectedItem()));
        alignRow.add(PropUtil.rowLabel("Align"));
        alignRow.add(alignCombo);
        add(alignRow);
    }

    private void updateFont(TextTile tile, JComboBox<String> fontCombo,
                            JComboBox<Integer> sizeCombo, boolean bold, boolean italic) {
        String family = (String) fontCombo.getSelectedItem();
        int size;
        try { size = Integer.parseInt(sizeCombo.getSelectedItem().toString()); }
        catch (Exception ex) { size = 14; }
        int style = (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0);
        tile.setTileFont(new Font(family, style, size));
    }
}
