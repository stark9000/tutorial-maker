package tutorialmaker.annotation;
import java.awt.*;
public class RectShape extends AnnotationShape {
    public float fx,fy,fw,fh; public boolean filled;
    public RectShape(float x,float y,float w,float h,Color c,float t,boolean f){super(c,t);fx=x;fy=y;fw=w;fh=h;filled=f;}
    @Override public void draw(Graphics2D g2,int w,int h){
        int rx=(int)(fx*w),ry=(int)(fy*h),rw=(int)(fw*w),rh=(int)(fh*h);
        if(filled){g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),60));g2.fillRect(rx,ry,rw,rh);}
        g2.setColor(color);g2.setStroke(new BasicStroke(thickness));g2.drawRect(rx,ry,rw,rh);
        if(selected)drawSelBox(g2,w,h);
    }
    @Override public boolean contains(Point p,int w,int h){return getBounds(w,h).contains(p);}
    @Override public Rectangle getBounds(int w,int h){return new Rectangle((int)(fx*w),(int)(fy*h),(int)(fw*w),(int)(fh*h));}
    @Override public void translate(float dfx,float dfy){fx+=dfx;fy+=dfy;}
}
