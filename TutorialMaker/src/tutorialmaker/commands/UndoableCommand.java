package tutorialmaker.commands;

public interface UndoableCommand {
    void execute();
    void undo();
    String getDescription();
}
