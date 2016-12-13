package Controller;

import Piece.*;
import View.*;

public abstract class Controller {
    protected boolean running;

    protected Board chessboard = Board.INSTANCE;
    protected View display;

    // Tell the controller where to output
    public void init(View v) { display = v; }

    abstract public String[] gameType();

    // Input a command, relay it to Board
    abstract public Turn getCommand();
    public void turnHandler(Turn t) {
        switch (t.type) {
            case 'q':
                quit();
                return;
            case 'p':
                chessboard.move(t);
                chessboard.promote(t.pieceID, t.newPiece);
                break;
            case 'P':
                chessboard.take(t);
                chessboard.promote(t.pieceID, t.newPiece);
            case 'm':case 'o':case 'O':
                chessboard.move(t);
                break;
            case 't':
                chessboard.take(t);
                break;
            case 'u':
                chessboard.undo();
                break;
            default:
                return;
        }
        display.update();
    }

    // Instead of infinity loop
    public boolean isRunning() { return running; }

    // Quits the program
    public void quit() { running = false; }
}
