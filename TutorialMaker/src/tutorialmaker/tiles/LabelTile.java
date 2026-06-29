package tutorialmaker.tiles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LabelTile extends TileComponent {

    public enum Style { CAPTION, STEP_BADGE, CALLOUT }

    private String style_text;  // displayed text
    private Style  style;
    private Color  bgColor, textColor;
    private int    stepNumber = 1;
    private boolean    editing   = false;
    private JTextField editField = null;

    public LabelTile(int docX, int docY, String text, Style style) {
        super(docX, docY, style==Style.STEP_BADGE?48:180, style==Style.STEP_BADGE?48:40);
        this.style_text = text; this.style = style;
        switch(style) {
            case STEP_BADGE: bgColor=new Color(0,120,215); textColor=Color.WHITE; showBorder=false; break;
            case CALLOUT:    bgColor=new Color(255,245,180); textColor=new Color(80,60,0); break;
            default:         bgColor=new Color(240,240,240); textColor=Color.BLACK; break;
        }
        try { stepNumber=Integer.parseInt(text.trim()); } catch(Exception ignored){}
        addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){ if(e.getClickCount()==2) startEditing(); }
        });
    }

    private void startEditing(){
        if(editing) return; editing=true;
        editField=new JTextField(style_text);
        editField.setFont(labelFont(getHeight())); editField.setForeground(textColor); editField.setBackground(bgColor);
        editField.setHorizontalAlignment(JTextField.CENTER);
        editField.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
        editField.setBounds(4,4,getWidth()-8,getHeight()-8);
        add(editField); editField.requestFocusInWindow(); editField.selectAll();
        revalidate(); repaint();
        editField.addActionListener(a->stopEditing());
        editField.addFocusListener(new FocusAdapter(){public void focusLost(FocusEvent e){stopEditing();}});
        editField.addKeyListener(new KeyAdapter(){public void keyPressed(KeyEvent e){if(e.getKeyCode()==KeyEvent.VK_ESCAPE)stopEditing();}});
    }

    private void stopEditing(){
        if(!editing||editField==null) return;
        style_text=editField.getText();
        try{stepNumber=Integer.parseInt(style_text.trim());}catch(Exception ignored){}
        remove(editField); editField=null; editing=false; revalidate(); repaint();
    }

    @Override public void paintTile(Graphics2D g2)          { paintTileAt(g2, getWidth(), getHeight()); }
    @Override public void paintTileAt(Graphics2D g2, int w, int h) {
        if(editing) return;
        switch(style){ case STEP_BADGE:paintBadge(g2,w,h);break; case CALLOUT:paintCallout(g2,w,h);break; default:paintCaption(g2,w,h); }
    }

    private void paintBadge(Graphics2D g2, int w, int h){
        int d=Math.min(w,h)-4, ox=(w-d)/2, oy=(h-d)/2;
        g2.setColor(bgColor); g2.fillOval(ox,oy,d,d);
        g2.setColor(bgColor.darker()); g2.setStroke(new BasicStroke(2f)); g2.drawOval(ox,oy,d,d);
        Font f=new Font("SansSerif",Font.BOLD,Math.max(8,d/2)); g2.setFont(f); g2.setColor(textColor);
        FontMetrics fm=g2.getFontMetrics();
        String s=style_text.isEmpty()?String.valueOf(stepNumber):style_text;
        g2.drawString(s, ox+(d-fm.stringWidth(s))/2, oy+(d-fm.getHeight())/2+fm.getAscent());
    }
    private void paintCallout(Graphics2D g2, int w, int h){
        g2.setColor(bgColor); g2.fillRoundRect(0,0,w-1,h-1,10,10);
        g2.setColor(new Color(200,180,0)); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(0,0,w-1,h-1,10,10);
        g2.setColor(textColor); g2.setFont(labelFont(h)); FontMetrics fm=g2.getFontMetrics();
        g2.drawString(trunc(style_text,w-12,fm),6,(h-fm.getHeight())/2+fm.getAscent());
    }
    private void paintCaption(Graphics2D g2, int w, int h){
        g2.setColor(bgColor); g2.fillRect(0,0,w,h);
        g2.setColor(new Color(0,120,215)); g2.fillRect(0,0,3,h);
        g2.setColor(textColor); g2.setFont(labelFont(h)); FontMetrics fm=g2.getFontMetrics();
        g2.drawString(trunc(style_text,w-16,fm),10,(h-fm.getHeight())/2+fm.getAscent());
    }

    private Font labelFont(int h){ return new Font("SansSerif",style==Style.STEP_BADGE?Font.BOLD:Font.PLAIN,Math.max(9,(int)(h*0.4))); }
    private String trunc(String s,int maxW,FontMetrics fm){ while(s.length()>0&&fm.stringWidth(s+"…")>maxW)s=s.substring(0,s.length()-1); return s; }

    @Override public TileComponent duplicate(){
        LabelTile t=new LabelTile(docX+20,docY+20,style_text,style);
        t.bgColor=bgColor; t.textColor=textColor; t.stepNumber=stepNumber; t.docW=docW; t.docH=docH; return t;
    }
    @Override public String getTileTypeName(){ switch(style){case STEP_BADGE:return"Step Badge";case CALLOUT:return"Callout";default:return"Label";} }

    public String getText()             { return style_text; }
    public void   setText(String t)     { style_text=t; try{stepNumber=Integer.parseInt(t.trim());}catch(Exception ignored){} repaint(); }
    public int    getStepNumber()       { return stepNumber; }
    public Color  getBgColor()          { return bgColor; }
    public Color  getTextColor()        { return textColor; }
    public void   setBgColor(Color c)   { bgColor=c; repaint(); }
    public void   setTextColor(Color c) { textColor=c; repaint(); }
    public Style  getLabelStyle()       { return style; }
}
