package tutorialmaker.annotation;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
public class FreehandShape extends AnnotationShape {
    public List<float[]> pts=new ArrayList<>();
    public FreehandShape(Color c,float t){super(c,t);}
    public void addPoint(float fx,float fy){pts.add(new float[]{fx,fy});}
    @Override public void draw(Graphics2D g2,int w,int h){
        if(pts.size()<2)return;
        g2.setColor(color);g2.setStroke(new BasicStroke(thickness,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        for(int i=1;i<pts.size();i++){float[]a=pts.get(i-1),b=pts.get(i);g2.drawLine((int)(a[0]*w),(int)(a[1]*h),(int)(b[0]*w),(int)(b[1]*h));}
        if(selected)drawSelBox(g2,w,h);
    }
    @Override public boolean contains(Point p,int w,int h){
        for(int i=1;i<pts.size();i++){float[]a=pts.get(i-1),b=pts.get(i);if(new Line2D.Float(a[0]*w,a[1]*h,b[0]*w,b[1]*h).ptSegDist(p.x,p.y)<(thickness+6))return true;}
        return false;
    }
    @Override public Rectangle getBounds(int w,int h){
        if(pts.isEmpty())return new Rectangle(0,0,1,1);
        float mnx=Float.MAX_VALUE,mny=Float.MAX_VALUE,mxx=-Float.MAX_VALUE,mxy=-Float.MAX_VALUE;
        for(float[]p:pts){mnx=Math.min(mnx,p[0]);mny=Math.min(mny,p[1]);mxx=Math.max(mxx,p[0]);mxy=Math.max(mxy,p[1]);}
        return new Rectangle((int)(mnx*w),(int)(mny*h),(int)((mxx-mnx)*w),(int)((mxy-mny)*h));
    }
    @Override public void translate(float dfx,float dfy){for(float[]p:pts){p[0]+=dfx;p[1]+=dfy;}}
}
