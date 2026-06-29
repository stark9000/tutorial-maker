package tutorialmaker.tiles;

import tutorialmaker.canvas.CanvasPanel;
import tutorialmaker.commands.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class TileComponent extends JPanel {

    protected int docX, docY, docW, docH;
    protected CanvasPanel canvas;
    protected boolean selected=false, showBorder=true;

    private static final int   HS=8;
    private static final Color HC=new Color(0,120,215),SB=new Color(0,120,215),SC=new Color(0,0,0,40);

    private Point dragStartCanvas;
    private int   docXAtDrag,docYAtDrag,resizeHandle=-1;
    private int   docXAtResize,docYAtResize,docWAtResize,docHAtResize;
    // Snapshot at start of drag for undo
    private int   snapX,snapY,snapW,snapH;

    public TileComponent(int x,int y,int w,int h){
        docX=x;docY=y;docW=w;docH=h;
        setLayout(null);setOpaque(false);setBounds(x,y,w,h);setupInteraction();
    }

    private void setupInteraction(){
        MouseAdapter ma=new MouseAdapter(){
            @Override public void mousePressed(MouseEvent e){
                if(canvas!=null){canvas.selectTile(TileComponent.this);canvas.requestFocusInWindow();}
                resizeHandle=getHandleAt(e.getPoint());
                dragStartCanvas=SwingUtilities.convertPoint(TileComponent.this,e.getPoint(),getParent());
                if(resizeHandle>=0){docXAtResize=docX;docYAtResize=docY;docWAtResize=docW;docHAtResize=docH;}
                else{docXAtDrag=docX;docYAtDrag=docY;}
                // Snapshot for undo
                snapX=docX;snapY=docY;snapW=docW;snapH=docH;
                if(e.isPopupTrigger())showContextMenu(e);
            }
            @Override public void mouseReleased(MouseEvent e){
                // Register undo if position/size changed
                if(canvas!=null && dragStartCanvas!=null){
                    if(resizeHandle<0 && (snapX!=docX||snapY!=docY)){
                        final int ox=snapX,oy=snapY,nx=docX,ny=docY;
                        canvas.getUndoManager().execute(new MoveTileCommand(
                            canvas,TileComponent.this,ox,oy,nx,ny){
                            @Override public void execute(){}  // already moved
                        });
                    } else if(resizeHandle>=0 && (snapX!=docX||snapY!=docY||snapW!=docW||snapH!=docH)){
                        final int ox=snapX,oy=snapY,ow=snapW,oh=snapH,nx=docX,ny=docY,nw=docW,nh=docH;
                        canvas.getUndoManager().execute(new ResizeTileCommand(
                            canvas,TileComponent.this,ox,oy,ow,oh,nx,ny,nw,nh){
                            @Override public void execute(){}  // already resized
                        });
                    }
                }
                dragStartCanvas=null;resizeHandle=-1;
                if(e.isPopupTrigger())showContextMenu(e);
            }
            @Override public void mouseDragged(MouseEvent e){
                if(dragStartCanvas==null||canvas==null)return;
                Point cur=SwingUtilities.convertPoint(TileComponent.this,e.getPoint(),getParent());
                double zoom=canvas.getZoom();
                int ddx=(int)Math.round((cur.x-dragStartCanvas.x)/zoom);
                int ddy=(int)Math.round((cur.y-dragStartCanvas.y)/zoom);
                if(resizeHandle>=0)doResizeDoc(ddx,ddy);
                else{docX=canvas.snap(docXAtDrag+ddx);docY=canvas.snap(docYAtDrag+ddy);}
                canvas.applyZoomToTile(TileComponent.this);getParent().repaint();
            }
            @Override public void mouseMoved(MouseEvent e){updateCursor(e.getPoint());}
        };
        addMouseListener(ma);addMouseMotionListener(ma);
    }

    private void doResizeDoc(int ddx,int ddy){
        int x=docXAtResize,y=docYAtResize,w=docWAtResize,h=docHAtResize,mW=40,mH=30;
        switch(resizeHandle){
            case 0:x+=ddx;y+=ddy;w-=ddx;h-=ddy;break; case 1:y+=ddy;w+=ddx;h-=ddy;break;
            case 2:x+=ddx;w-=ddx;h+=ddy;break;         case 3:w+=ddx;h+=ddy;break;
            case 4:y+=ddy;h-=ddy;break;                 case 5:h+=ddy;break;
            case 6:x+=ddx;w-=ddx;break;                 case 7:w+=ddx;break;
        }
        if(w<mW){if(resizeHandle==0||resizeHandle==2||resizeHandle==6)x=docXAtResize+docWAtResize-mW;w=mW;}
        if(h<mH){if(resizeHandle==0||resizeHandle==1||resizeHandle==4)y=docYAtResize+docHAtResize-mH;h=mH;}
        docX=canvas.snap(x);docY=canvas.snap(y);docW=w;docH=h;
    }

    private int getHandleAt(Point p){
        if(!selected)return -1;int w=getWidth(),h=getHeight(),hs=HS+2;
        if(hits(p,0,0,hs))return 0;        if(hits(p,w-hs,0,hs))return 1;
        if(hits(p,0,h-hs,hs))return 2;     if(hits(p,w-hs,h-hs,hs))return 3;
        if(hits(p,w/2-hs/2,0,hs))return 4; if(hits(p,w/2-hs/2,h-hs,hs))return 5;
        if(hits(p,0,h/2-hs/2,hs))return 6; if(hits(p,w-hs,h/2-hs/2,hs))return 7;
        return -1;
    }
    private boolean hits(Point p,int hx,int hy,int hs){return new Rectangle(hx,hy,hs,hs).contains(p);}
    private void updateCursor(Point p){
        if(!selected){setCursor(Cursor.getDefaultCursor());return;}
        switch(getHandleAt(p)){
            case 0:case 3:setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));break;
            case 1:case 2:setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));break;
            case 4:case 5:setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));break;
            case 6:case 7:setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));break;
            default:setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));break;
        }
    }

    private void showContextMenu(MouseEvent e){
        JPopupMenu m=new JPopupMenu();
        addItem(m,"Duplicate  Ctrl+D",()->{ if(canvas!=null)canvas.duplicateTile(this);});
        addItem(m,"Bring to Front",()->{ if(canvas!=null){canvas.setComponentZOrder(this,0);canvas.positionDrawingLayer();canvas.repaint();}});
        addItem(m,"Send to Back",()->{ if(canvas!=null){canvas.setComponentZOrder(this,canvas.getComponentCount()-1);canvas.repaint();}});
        m.addSeparator();
        JCheckBoxMenuItem bi=new JCheckBoxMenuItem("Show Border",showBorder);
        bi.addActionListener(a->applyBorderStyle(!showBorder));m.add(bi);
        m.addSeparator();addItem(m,"Delete",()->{ if(canvas!=null)canvas.removeTile(this);});
        m.show(this,e.getX(),e.getY());
    }
    private void addItem(JPopupMenu m,String l,Runnable r){JMenuItem i=new JMenuItem(l);i.addActionListener(e->r.run());m.add(i);}

    @Override protected void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(),h=getHeight();
        if(selected){g2.setColor(SC);g2.fillRoundRect(3,3,w-2,h-2,4,4);}
        paintTile(g2);
        if(showBorder){g2.setColor(selected?SB:new Color(180,180,180));g2.setStroke(new BasicStroke(selected?1.5f:1f));g2.drawRect(0,0,w-1,h-1);}
        else if(selected){g2.setColor(SB);g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,1,new float[]{4,3},0));g2.drawRect(0,0,w-1,h-1);}
        if(selected)drawHandles(g2,w,h);g2.dispose();
    }

    private void drawHandles(Graphics2D g2,int w,int h){
        int hs=HS;int[][]pts={{0,0},{w-hs,0},{0,h-hs},{w-hs,h-hs},{w/2-hs/2,0},{w/2-hs/2,h-hs},{0,h/2-hs/2},{w-hs,h/2-hs/2}};
        g2.setStroke(new BasicStroke(1f));
        for(int[]p:pts){g2.setColor(Color.WHITE);g2.fillRect(p[0],p[1],hs,hs);g2.setColor(HC);g2.drawRect(p[0],p[1],hs,hs);}
    }

    public void paintForPrint(Graphics2D g2,int w,int h){
        paintTileAt(g2,w,h);
        if(showBorder){g2.setColor(new Color(180,180,180));g2.setStroke(new BasicStroke(1f));g2.drawRect(0,0,w-1,h-1);}
    }

    public abstract void paintTile(Graphics2D g2);
    public void paintTileAt(Graphics2D g2,int w,int h){ paintTile(g2); }

    public void setSelected(boolean s){selected=s;repaint();}
    public void applyBorderStyle(boolean b){showBorder=b;repaint();}
    public void setCanvas(CanvasPanel c){canvas=c;}

    public int getDocX(){return docX;} public void setDocX(int v){docX=v;}
    public int getDocY(){return docY;} public void setDocY(int v){docY=v;}
    public int getDocW(){return docW;} public void setDocW(int v){docW=v;}
    public int getDocH(){return docH;} public void setDocH(int v){docH=v;}
    public boolean isShowBorder(){return showBorder;}
    public CanvasPanel getCanvas(){return canvas;}

    public abstract TileComponent duplicate();
    public abstract String getTileTypeName();
}
