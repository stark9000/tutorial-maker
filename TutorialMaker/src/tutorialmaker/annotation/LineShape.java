package tutorialmaker.annotation;
import java.awt.*;
import java.awt.geom.Line2D;
public class LineShape extends AnnotationShape {
    public float fx1,fy1,fx2,fy2;
    public LineShape(float x1,float y1,float x2,float y2,Color c,float t){super(c,t);fx1=x1;fy1=y1;fx2=x2;fy2=y2;}
    @Override public void draw(Graphics2D g2,int w,int h){
        g2.setColor(color);g2.setStroke(new BasicStroke(thickness,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g2.drawLine((int)(fx1*w),(int)(fy1*h),(int)(fx2*w),(int)(fy2*h));
        if(selected)drawSelBox(g2,w,h);
    }
    @Override public boolean contains(Point p,int w,int h){return new Line2D.Float(fx1*w,fy1*h,fx2*w,fy2*h).ptSegDist(p.x,p.y)<(thickness+6);}
    @Override public Rectangle getBounds(int w,int h){int x1=(int)(fx1*w),y1=(int)(fy1*h),x2=(int)(fx2*w),y2=(int)(fy2*h);return new Rectangle(Math.min(x1,x2),Math.min(y1,y2),Math.abs(x2-x1)+1,Math.abs(y2-y1)+1);}
    @Override public void translate(float dfx,float dfy){fx1+=dfx;fy1+=dfy;fx2+=dfx;fy2+=dfy;}
}
