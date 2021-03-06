package Controller;

import Piece.*;

import java.awt.event.MouseEvent;
import java.util.Scanner;

public class BasicController extends Controller {
    // Singleton
    public static BasicController INSTANCE = new BasicController();
    private Scanner in = new Scanner(System.in);

    // For the runtime loop
    private BasicController() {
        running = true;
    }

    public void init() {}

    /*public String[] gameType() {
        logger.log("Collecting information about game type...", true);

        display.netPrompt();
        String s, params[] = new String[2];
        s = in.nextLine().toLowerCase();
        params[0] = s;
        if (s.equals("local") || s.equals("server")) {
            params[1] = "";
        }
        else if (s.equals("client")) {
            display.clientPrompt();
            params[1] = in.nextLine();
        }

        logger.log("Collected!", false);
        return params;
    }*/

    // Receives a move from input
    public Turn getCommand() {
        //logger.log("Getting command...");

        // Ends the game if checkmate
        if (chessboard.isMate(chessboard.getTurn())) {
            display.mateHandler();
            super.quit();
            return new Turn('q');
        }

        // Informs the current player of check
        if (chessboard.isCheck(chessboard.getTurn())) {
            display.checkHandler();
        }

        String s = in.nextLine();

        // Exit command
        if (s.equals("exit") || s.equals("quit") || s.equals("stop")) {
            super.quit();
            return new Turn('q');
        }

        // Activate this if you want to allow users to make undo
        /*// Undo command
        if (s.equals("undo")) {
            chessboard.undo();
            display.update();
            return new Turn('u');
        }*/

        // Control input format
        if (s.length() != 5) {
            System.err.println("ERROR: Invalid command.");
            return null;
        }

        int x1 = s.charAt(0) - 'a', y1 = s.charAt(1) - '1',
                x2 = s.charAt(3) - 'a', y2 = s.charAt(4) - '1';

        if (x1 > 7 || x2 > 7 || y1 > 7 || y2 > 7 || x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0) {
            System.err.println("ERROR: Invalid command.");
            return null;
        }

        // Attempts to move, then take the target tile
        Piece p = chessboard.getPiece(x1, y1).get();
        if (p == null) return null;
        Turn t = new Turn ('m', x1, y1, x2, y2, p.getID(), p.getHasMoved());
        if (!chessboard.move(t)) {
            t = new Turn ('t', x1, y1, x2, y2, p.getID(), p.getHasMoved());
            if (!chessboard.take(t)) {
                System.err.println("ERROR: Illegal move." + (char)(x1 + 'a') + (y1 + 1) + "-" + (char)(x2 + 'a') + (y2 + 1));
                return null;
            }
        }
        display.update();

        int id = chessboard.needsPromotion(!chessboard.getTurn());

        if (id >= 0) {
            display.promotionHandler();
            String ss = in.nextLine();
            chessboard.promote(id, ss.charAt(0));

            display.update();
            Turn prevt = chessboard.log.pop();
            if (prevt.type == 'm')
                return new Turn('p', prevt.x, prevt.y, prevt.x2, prevt.y2, prevt.pieceID, ss.charAt(0));
            return new Turn('P', prevt.x, prevt.y, prevt.x2, prevt.y2, prevt.pieceID, prevt.targID, ss.charAt(0));
        }

        return chessboard.log.peek();
    }

    public void mouseClicked(MouseEvent mouseEvent) {}
}