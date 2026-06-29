package tutorialmaker.annotation;
import java.awt.*;
public class TextPinShape extends AnnotationShape {
    public float fx,fy; public String text;
    public TextPinShape(float x,float y,String text,Color c,float t){super(c,t);fx=x;fy=y;this.text=text;}
    @Override public void draw(Graphics2D g2,int w,int h){
        if(text==null||text.isEmpty())return;
        int px=(int)(fx*w),py=(int)(fy*h);
        Font font=new Font("SansSerif",Font.BOLD,13);g2.setFont(font);
        FontMetrics fm=g2.getFontMetrics();
        int tw=fm.stringWidth(text)+10,th=fm.getHeight()+6;
        g2.setColor(color);g2.fillOval(px-5,py-5,10,10);
        int bx=px+8,by=py-th/2;
        g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),220));
        g2.fillRoundRect(bx,by,tw,th,8,8);
        g2.setColor(color);g2.setStroke(new BasicStroke(1f));g2.drawRoundRect(bx,by,tw,th,8,8);
        g2.setColor(Color.WHITE);g2.drawString(text,bx+5,by+fm.getAscent()+3);
        if(selected)drawSelBox(g2,w,h);
    }
    @Override public boolean contains(Point p,int w,int h){
        int px=(int)(fx*w),py=(int)(fy*h);
        if(Math.abs(p.x-px)<12&&Math.abs(p.y-py)<12)return true;
        Font font=new Font("SansSerif",Font.BOLD,13);
        FontMetrics fm=new Canvas().getFontMetrics(font);
        int tw=fm.stringWidth(text)+10,th=fm.getHeight()+6;
        return new Rectangle(px+8,py-th/2,tw,th).contains(p);
    }
    @Override public Rectangle getBounds(int w,int h){return new Rectangle((int)(fx*w)-5,(int)(fy*h)-5,20,20);}
    @Override public void translate(float dfx,float dfy){fx+=dfx;fy+=dfy;}
}
