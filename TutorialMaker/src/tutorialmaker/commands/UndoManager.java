package tutorialmaker.commands;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Command pattern — each user action is an UndoableCommand.
 * UndoManager keeps two stacks: undo and redo.
 */
public class UndoManager {

    private static final int MAX = 100;

    private final Deque<UndoableCommand> undoStack = new ArrayDeque<>();
    private final Deque<UndoableCommand> redoStack = new ArrayDeque<>();

    private Runnable changeListener;

    public void setChangeListener(Runnable r) { this.changeListener = r; }

    /** Execute a command and push it onto the undo stack. */
    public void execute(UndoableCommand cmd) {
        cmd.execute();
        undoStack.push(cmd);
        if (undoStack.size() > MAX) {
            // Trim oldest — convert to array, drop last
            UndoableCommand[] arr = undoStack.toArray(new UndoableCommand[0]);
            undoStack.clear();
            for (int i = 0; i < arr.length - 1; i++) undoStack.add(arr[i]);
        }
        redoStack.clear();   // new action clears redo history
        notifyChange();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        UndoableCommand cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        notifyChange();
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        UndoableCommand cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        notifyChange();
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public String undoDescription() {
        return undoStack.isEmpty() ? "" : undoStack.peek().getDescription();
    }
    public String redoDescription() {
        return redoStack.isEmpty() ? "" : redoStack.peek().getDescription();
    }

    public void clear() { undoStack.clear(); redoStack.clear(); notifyChange(); }

    private void notifyChange() { if (changeListener != null) changeListener.run(); }
}
