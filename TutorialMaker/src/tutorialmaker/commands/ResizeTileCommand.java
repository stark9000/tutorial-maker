package tutorialmaker.commands;

import tutorialmaker.canvas.CanvasPanel;
import tutorialmaker.tiles.TileComponent;

public class ResizeTileCommand implements UndoableCommand {
    private final CanvasPanel canvas;
    private final TileComponent tile;
    private final int oldX, oldY, oldW, oldH;
    private final int newX, newY, newW, newH;

    public ResizeTileCommand(CanvasPanel canvas, TileComponent tile,
                             int oldX, int oldY, int oldW, int oldH,
                             int newX, int newY, int newW, int newH) {
        this.canvas = canvas; this.tile = tile;
        this.oldX=oldX; this.oldY=oldY; this.oldW=oldW; this.oldH=oldH;
        this.newX=newX; this.newY=newY; this.newW=newW; this.newH=newH;
    }

    @Override public void execute() {
        tile.setDocX(newX); tile.setDocY(newY);
        tile.setDocW(newW); tile.setDocH(newH);
        canvas.applyZoomToTile(tile); canvas.repaint();
    }
    @Override public void undo() {
        tile.setDocX(oldX); tile.setDocY(oldY);
        tile.setDocW(oldW); tile.setDocH(oldH);
        canvas.applyZoomToTile(tile); canvas.repaint();
    }
    @Override public String getDescription() { return "Resize " + tile.getTileTypeName(); }
}
