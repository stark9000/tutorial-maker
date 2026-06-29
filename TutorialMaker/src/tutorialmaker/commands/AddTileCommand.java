package tutorialmaker.commands;

import tutorialmaker.canvas.CanvasPanel;
import tutorialmaker.tiles.TileComponent;

import java.awt.*;

/** Add a tile to the canvas. */
public class AddTileCommand implements UndoableCommand {
    private final CanvasPanel canvas;
    private final TileComponent tile;

    public AddTileCommand(CanvasPanel canvas, TileComponent tile) {
        this.canvas = canvas; this.tile = tile;
    }

    @Override public void execute() { canvas.addTileDirect(tile); }
    @Override public void undo()    { canvas.removeTileDirect(tile); }
    @Override public String getDescription() { return "Add " + tile.getTileTypeName(); }
}
