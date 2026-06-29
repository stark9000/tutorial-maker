package tutorialmaker.properties;

import tutorialmaker.annotation.AnnotationTool;
import tutorialmaker.tiles.ImageTile;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class ImagePropertiesPanel extends JPanel {

    private static final AnnotationTool[] TOOLS = {
        AnnotationTool.SELECT, AnnotationTool.ARROW, AnnotationTool.LINE,
        AnnotationTool.RECT,   AnnotationTool.OVAL,  AnnotationTool.FREEHAND,
        AnnotationTool.TEXT_PIN
    };
    private static final String[] TOOL_LABELS = {
        "↖ Select", "➜ Arrow", "╱ Line", "▭ Rect", "⬭ Oval", "✏ Free", "📍 Pin"
    };

    public ImagePropertiesPanel(ImageTile tile) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        add(PropUtil.sectionLabel("Image"));

        JPanel replaceRow = PropUtil.rowBorder();
        JButton replaceBtn = new JButton("Replace Image...");
        replaceBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Images","png","jpg","jpeg","gif","bmp"));
            if (fc.showOpenDialog(replaceBtn)==JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                tile.setImage(new ImageIcon(f.getAbsolutePath()), f.getAbsolutePath());
            }
        });
        replaceRow.add(replaceBtn, BorderLayout.CENTER);
        add(replaceRow);

        JPanel sizeRow = PropUtil.rowBorder();
        JLabel sizeLabel = new JLabel(tile.getDocW()+" × "+tile.getDocH()+" px");
        sizeLabel.setFont(new Font("SansSerif",Font.PLAIN,11));
        sizeRow.add(PropUtil.rowLabel("Size"), BorderLayout.WEST);
        sizeRow.add(sizeLabel, BorderLayout.CENTER);
        add(sizeRow);

        JPanel fitRow = PropUtil.row();
        JButton orig = new JButton("Original");
        JButton fitW = new JButton("Fit Width");
        orig.addActionListener(e -> tile.resetToOriginalSize());
        fitW.addActionListener(e -> tile.fitToPageWidth());
        fitRow.add(orig); fitRow.add(fitW);
        add(fitRow);
    }
}
