package tutorialmaker.tiles;

import javax.swing.ImageIcon;
import java.awt.*;

public class ImageTile extends TileComponent {

    private ImageIcon image;
    private String    imagePath;
    private int       originalW, originalH;

    public ImageTile(int docX, int docY, int docW, int docH, ImageIcon img, String path) {
        super(docX, docY, docW, docH);
        this.image = img; this.imagePath = path;
        if (img != null) { originalW = img.getIconWidth(); originalH = img.getIconHeight(); }
    }

    @Override public void paintTile(Graphics2D g2)          { paintTileAt(g2, getWidth(), getHeight()); }
    @Override public void paintTileAt(Graphics2D g2, int w, int h) {
        if (image != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(image.getImage(), 0, 0, w, h, this);
        } else {
            g2.setColor(new Color(220,220,220)); g2.fillRect(0,0,w,h);
            g2.setColor(new Color(150,150,150));
            g2.setFont(new Font("SansSerif",Font.PLAIN,12));
            FontMetrics fm = g2.getFontMetrics(); String msg = "No Image";
            g2.drawString(msg, (w-fm.stringWidth(msg))/2, h/2);
        }
    }

    public void resetToOriginalSize() {
        if (originalW>0&&originalH>0) { docW=originalW; docH=originalH; if(canvas!=null) canvas.applyZoomToTile(this); }
    }
    public void fitToPageWidth() {
        if (canvas!=null&&originalW>0&&originalH>0) {
            docW=canvas.getPageW(); docH=(int)((double)originalH/originalW*docW); canvas.applyZoomToTile(this);
        }
    }
    public void setImage(ImageIcon img, String path) {
        this.image=img; this.imagePath=path;
        if(img!=null){originalW=img.getIconWidth();originalH=img.getIconHeight();} repaint();
    }

    public String getImagePath() { return imagePath; }

    @Override public TileComponent duplicate() { return new ImageTile(docX+20,docY+20,docW,docH,image,imagePath); }
    @Override public String getTileTypeName()  { return "Image"; }
}
