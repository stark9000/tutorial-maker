package tutorialmaker.tiles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class TextTile extends TileComponent {

    private String text      = "";
    private Font   tileFont  = new Font("SansSerif", Font.PLAIN, 14);
    private Color  textColor = Color.BLACK;
    private Color  bgColor   = Color.WHITE;
    private String alignment = "Left";
    private boolean   editing  = false;
    private JTextArea editArea = null;

    public TextTile(int docX, int docY, int docW, int docH, String text) {
        super(docX, docY, docW, docH);
        this.text = text;
        addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){ if(e.getClickCount()==2) startEditing(); }
        });
    }

    private void startEditing(){
        if(editing) return; editing=true;
        editArea=new JTextArea(text); editArea.setFont(tileFont);
        editArea.setForeground(textColor); editArea.setBackground(bgColor);
        editArea.setLineWrap(true); editArea.setWrapStyleWord(true);
        editArea.setBorder(new EmptyBorder(4,4,4,4));
        editArea.setBounds(0,0,getWidth(),getHeight());
        add(editArea); editArea.requestFocusInWindow(); editArea.selectAll();
        revalidate(); repaint();
        editArea.addFocusListener(new FocusAdapter(){public void focusLost(FocusEvent e){stopEditing();}});
        editArea.addKeyListener(new KeyAdapter(){public void keyPressed(KeyEvent e){if(e.getKeyCode()==KeyEvent.VK_ESCAPE)stopEditing();}});
    }
    private void stopEditing(){
        if(!editing||editArea==null) return;
        text=editArea.getText(); remove(editArea); editArea=null; editing=false; revalidate(); repaint();
    }

    @Override public void setBounds(int x,int y,int w,int h){
        super.setBounds(x,y,w,h); if(editArea!=null) editArea.setBounds(0,0,w,h);
    }

    @Override public void paintTile(Graphics2D g2)          { paintTileAt(g2, getWidth(), getHeight()); }
    @Override public void paintTileAt(Graphics2D g2, int w, int h){
        g2.setColor(bgColor); g2.fillRect(0,0,w,h);
        if(editing) return;
        g2.setColor(textColor); g2.setFont(tileFont);
        drawWrappedText(g2,text,6,6,w-12,h-12);
    }

    private void drawWrappedText(Graphics2D g2,String text,int x,int y,int maxW,int maxH){
        FontMetrics fm=g2.getFontMetrics();
        if(text==null||text.isEmpty()){
            g2.setColor(new Color(160,160,160)); g2.setFont(tileFont.deriveFont(Font.ITALIC));
            g2.drawString("Double-click to edit...",x,y+fm.getAscent()); return;
        }
        int lineH=fm.getHeight(),curY=y+fm.getAscent();
        for(String line:text.split("\n",-1)){
            StringBuilder cur=new StringBuilder();
            for(String word:line.split(" ",-1)){
                String test=cur.length()==0?word:cur+" "+word;
                if(fm.stringWidth(test)<=maxW){cur=new StringBuilder(test);}
                else{if(curY<=y+maxH)drawLine(g2,cur.toString(),x,curY,maxW,fm);cur=new StringBuilder(word);curY+=lineH;}
            }
            if(curY<=y+maxH)drawLine(g2,cur.toString(),x,curY,maxW,fm);
            curY+=lineH;
        }
    }
    private void drawLine(Graphics2D g2,String s,int x,int y,int maxW,FontMetrics fm){
        int lx=x;
        if("Center".equals(alignment))lx=x+(maxW-fm.stringWidth(s))/2;
        else if("Right".equals(alignment))lx=x+maxW-fm.stringWidth(s);
        g2.drawString(s,lx,y);
    }

    @Override public TileComponent duplicate(){
        TextTile t=new TextTile(docX+20,docY+20,docW,docH,text);
        t.tileFont=tileFont; t.textColor=textColor; t.bgColor=bgColor; t.alignment=alignment; return t;
    }
    @Override public String getTileTypeName(){ return "Text"; }

    public Font   getTileFont()          { return tileFont; }
    public void   setTileFont(Font f)    { tileFont=f; repaint(); }
    public Color  getTextColor()         { return textColor; }
    public void   setTextColor(Color c)  { textColor=c; repaint(); }
    public Color  getBgColor()           { return bgColor; }
    public void   setBgColor(Color c)    { bgColor=c; repaint(); }
    public String getAlignment()         { return alignment; }
    public void   setAlignment(String a) { alignment=a; repaint(); }
    public String getText()              { return text; }
}
