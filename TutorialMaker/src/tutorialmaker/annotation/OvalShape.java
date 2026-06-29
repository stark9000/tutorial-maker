package tutorialmaker.annotation;
import java.awt.*;
public class OvalShape extends AnnotationShape {
    public float fx,fy,fw,fh; public boolean filled;
    public OvalShape(float x,float y,float w,float h,Color c,float t,boolean f){super(c,t);fx=x;fy=y;fw=w;fh=h;filled=f;}
    @Override public void draw(Graphics2D g2,int w,int h){
        int ox=(int)(fx*w),oy=(int)(fy*h),ow=(int)(fw*w),oh=(int)(fh*h);
        if(filled){g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),60));g2.fillOval(ox,oy,ow,oh);}
        g2.setColor(color);g2.setStroke(new BasicStroke(thickness));g2.drawOval(ox,oy,ow,oh);
        if(selected)drawSelBox(g2,w,h);
    }
    @Override public boolean contains(Point p,int w,int h){
        double rx=(fw*w)/2,ry=(fh*h)/2,cx=fx*w+rx,cy=fy*h+ry;
        double dx=(p.x-cx)/rx,dy=(p.y-cy)/ry;return dx*dx+dy*dy<=1.0;
    }
    @Override public Rectangle getBounds(int w,int h){return new Rectangle((int)(fx*w),(int)(fy*h),(int)(fw*w),(int)(fh*h));}
    @Override public void translate(float dfx,float dfy){fx+=dfx;fy+=dfy;}
}
