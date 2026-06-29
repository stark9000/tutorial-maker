package tutorialmaker.commands;

import tutorialmaker.canvas.CanvasPanel;
import tutorialmaker.tiles.TileComponent;

public class DeleteTileCommand implements UndoableCommand {
    private final CanvasPanel canvas;
    private final TileComponent tile;

    public DeleteTileCommand(CanvasPanel canvas, TileComponent tile) {
        this.canvas = canvas; this.tile = tile;
    }

    @Override public void execute() { canvas.removeTileDirect(tile); }
    @Override public void undo()    { canvas.addTileDirect(tile); }
    @Override public String getDescription() { return "Delete " + tile.getTileTypeName(); }
}
