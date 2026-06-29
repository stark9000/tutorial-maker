package tutorialmaker.commands;

import tutorialmaker.canvas.CanvasPanel;
import tutorialmaker.tiles.TileComponent;

public class MoveTileCommand implements UndoableCommand {
    private final CanvasPanel canvas;
    private final TileComponent tile;
    private final int oldX, oldY, newX, newY;

    public MoveTileCommand(CanvasPanel canvas, TileComponent tile,
                           int oldX, int oldY, int newX, int newY) {
        this.canvas = canvas; this.tile = tile;
        this.oldX = oldX; this.oldY = oldY;
        this.newX = newX; this.newY = newY;
    }

    @Override public void execute() {
        tile.setDocX(newX); tile.setDocY(newY);
        canvas.applyZoomToTile(tile); canvas.repaint();
    }
    @Override public void undo() {
        tile.setDocX(oldX); tile.setDocY(oldY);
        canvas.applyZoomToTile(tile); canvas.repaint();
    }
    @Override public String getDescription() { return "Move " + tile.getTileTypeName(); }
}
