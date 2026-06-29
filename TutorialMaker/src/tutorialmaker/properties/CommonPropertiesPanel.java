package tutorialmaker.properties;

import tutorialmaker.tiles.TileComponent;
import tutorialmaker.canvas.CanvasPanel;
import javax.swing.*;
import java.awt.*;

public class CommonPropertiesPanel extends JPanel {

    public CommonPropertiesPanel(TileComponent tile) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        add(PropUtil.sep());
        add(PropUtil.sectionLabel("Position & Size"));

        JSpinner xSpin = PropUtil.spinner(tile.getDocX(), -2000, 5000, 1);
        xSpin.addChangeListener(e -> { tile.setDocX((Integer)xSpin.getValue()); if(tile.getCanvas()!=null) tile.getCanvas().applyZoomToTile(tile); });
        JPanel xRow = PropUtil.rowBorder(); xRow.add(PropUtil.rowLabel("X"), BorderLayout.WEST); xRow.add(xSpin, BorderLayout.CENTER); add(xRow);

        JSpinner ySpin = PropUtil.spinner(tile.getDocY(), -2000, 5000, 1);
        ySpin.addChangeListener(e -> { tile.setDocY((Integer)ySpin.getValue()); if(tile.getCanvas()!=null) tile.getCanvas().applyZoomToTile(tile); });
        JPanel yRow = PropUtil.rowBorder(); yRow.add(PropUtil.rowLabel("Y"), BorderLayout.WEST); yRow.add(ySpin, BorderLayout.CENTER); add(yRow);

        JSpinner wSpin = PropUtil.spinner(tile.getDocW(), 20, 5000, 1);
        wSpin.addChangeListener(e -> { tile.setDocW((Integer)wSpin.getValue()); if(tile.getCanvas()!=null) tile.getCanvas().applyZoomToTile(tile); });
        JPanel wRow = PropUtil.rowBorder(); wRow.add(PropUtil.rowLabel("Width"), BorderLayout.WEST); wRow.add(wSpin, BorderLayout.CENTER); add(wRow);

        JSpinner hSpin = PropUtil.spinner(tile.getDocH(), 20, 5000, 1);
        hSpin.addChangeListener(e -> { tile.setDocH((Integer)hSpin.getValue()); if(tile.getCanvas()!=null) tile.getCanvas().applyZoomToTile(tile); });
        JPanel hRow = PropUtil.rowBorder(); hRow.add(PropUtil.rowLabel("Height"), BorderLayout.WEST); hRow.add(hSpin, BorderLayout.CENTER); add(hRow);

        add(PropUtil.sep());
        add(PropUtil.sectionLabel("Appearance"));

        JPanel borderRow = PropUtil.row();
        borderRow.add(PropUtil.rowLabel("Border"));
        borderRow.add(PropUtil.toggle("On", tile.isShowBorder(), on -> tile.applyBorderStyle(on)));
        add(borderRow);

        add(PropUtil.sep());
        add(PropUtil.sectionLabel("Layer"));

        JPanel zRow = PropUtil.row();
        JButton front = new JButton("▲ Front");
        JButton back  = new JButton("▼ Back");
        front.addActionListener(e -> { if(tile.getCanvas()!=null){ tile.getCanvas().setComponentZOrder(tile,0); tile.getCanvas().repaint(); }});
        back.addActionListener(e  -> { if(tile.getCanvas()!=null){ CanvasPanel c=tile.getCanvas(); c.setComponentZOrder(tile,c.getComponentCount()-1); c.repaint(); }});
        zRow.add(front); zRow.add(back);
        add(zRow);
    }
}
