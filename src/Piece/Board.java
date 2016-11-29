package Piece; /**
 * Created by daniel on 21.11.16.
 */

import java.lang.ref.WeakReference;

public class Board {

    boolean whiteTurn;

    String lastTurn;

    // Singleton
    public static final Board INSTANCE = new Board(new Piece[]{new Pawn(0, 6, true, true, true), new King(0, 0, true, true, true),
    new King(7, 7, false, true, true)}, true);

    // Фигуры
    public Piece[] pieces;

    // newPieces - состояние доски, turn - принадлежность хода
    public Board(Piece[] newPieces, boolean turn) {
        pieces = newPieces;
        whiteTurn = turn;
        lastTurn = "-";
    }

    public Board() {
        whiteTurn = true;
        pieces = new Piece[32];
        lastTurn = "-";
        // Пешки
        for (int i = 0; i < 8; ++i)
            pieces[i] = new Pawn(i, 1, true, true, false);
        for (int i = 8; i < 16; ++i)
            pieces[i] = new Pawn(i - 8, 6, false, true, false);
        // Слоны
        pieces[16] = new Bishop(2, 0, true, true, false);
        pieces[17] = new Bishop(5, 0, true, true, false);
        pieces[18] = new Bishop(2, 7, false, true, false);
        pieces[19] = new Bishop(5, 7, false, true, false);
        // Ладьи
        pieces[20] = new Rook(0, 0, true, true, false);
        pieces[21] = new Rook(7, 0, true, true, false);
        pieces[22] = new Rook(0, 7, false, true, false);
        pieces[23] = new Rook(7, 7, false, true, false);
        // Короли и королевы
        pieces[24] = new Queen(3, 0, true, true, false);
        pieces[25] = new Queen(3, 7, false, true, false);
        pieces[26] = new King(4, 0, true, true, false);
        pieces[27] = new King(4, 7, false, true, false);
        // Кони
        pieces[28] = new Knight(1, 0, true, true, false);
        pieces[29] = new Knight(6, 0, true, true, false);
        pieces[30] = new Knight(1, 7, false, true, false);
        pieces[31] = new Knight(6, 7, false, true, false);
    }

    // Возвращает ссылку на фигуру в x, y, при условии, что фигура жива
    public WeakReference<Piece> getPiece(int x, int y) {
        for (Piece piece : pieces) {
            if (piece.getX() == x && piece.getY() == y && piece.isAlive()) {
                return new WeakReference<Piece>(piece);
            }
        }
        return new WeakReference<Piece>(null);
    }

    // Returns a reference to a piece by id
    public WeakReference<Piece> getPiece(int id) {
        return new WeakReference<Piece>(pieces[id]);
    }

    // Проверяет, находится ли клетка под ударом (нужно для короля)
    public boolean isThreatened(int x, int y, boolean colour) {
        for (Piece piece : pieces) {
            if (piece.isAlive() && piece.getColour() != colour && piece.checkAttack(x, y))
                return true;
        }
        return false;
    }

    // Moves piece from (x1, y1) to (x2, y2), returns true if successful
    public boolean move(int x1, int y1, int x2, int y2) {
        try {
            Piece p = getPiece(x1,y1).get();
            if (p.checkMove(x2, y2) && p.getColour() == whiteTurn) {
                p.move(x2, y2);

                // Generating move signature
                if (!p.getHasMoved()) {
                    p.setHasMoved(true);
                    lastTurn = "m" + x1 + y1 + x2 + y2 + "@";
                }
                else
                    lastTurn = "m" + x1 + y1 + x2 + y2 + "$";
                if (isCheck(!whiteTurn)) {
                    System.err.println("Moving into check");
                    undo();
                    return false;
                }
                return true;
            }
        } catch (NullPointerException e) { System.err.println("Attempted to move from empty tile " + x2 + " " + y2);}
        return false;
    }

    // Attempt to take (x2, y2) with (x1, y1), returns true if successful
    public boolean take(int x1, int y1, int x2, int y2) {
        try {
            Piece p = getPiece(x1,y1).get();
            if (p.checkAttack(x2, y2) && p.getColour() == whiteTurn) {
                int id = getPiece(x2, y2).get().getID();
                getPiece(x2, y2).get().die();
                p.move(x2, y2);
                // Generates move for undo
                if (!p.getHasMoved()) {
                    p.setHasMoved(true);
                    lastTurn = "t" + x1 + y1 + x2 + y2 + "@" + id; // "@ - фигура подвинулась в этом ходу, $ - нет"
                }
                else
                    lastTurn = "t" + x1 + y1 + x2 + y2 + "$" + id;
                if (isCheck(!whiteTurn)) {
                    System.err.println("Moving into check");
                    undo();
                }
                return true;
            }
        } catch (NullPointerException e) {}
        return false;
    }

    public boolean isCheck(boolean curr_colour) {
        Piece king = null;
        for (Piece p : pieces) { // Ищем своего короля
            if (p.getType() == 'K' && p.colour == curr_colour) {
                king = p;
                break;
            }
        }
        if (isThreatened(king.x, king.y, king.colour))
            return true;
        return false;
    }

    public boolean isMate(boolean curr_colour) {
        String oldTurn = lastTurn;
        // Checks all living loyal pieces, attempts to move them to all possible tiles - if at least one move can
        // prevent check, then returns false
        for (Piece p : pieces) {
            if (p.getColour() == curr_colour && p.isAlive()) {
                for (int i = 0; i < 8; ++i) {
                    for (int j = 0; j < 8; ++j) {
                        if (p.checkMove(i, j)) {
                            if (!p.getHasMoved()) {
                                p.setHasMoved(true);
                                lastTurn = "m" + p.getX() + p.getY() + i + j + "@";
                            }
                            else
                                lastTurn = "m" + p.getX() + p.getY() + i + j + "$";
                            p.move(i, j);
                            if (!isCheck(!whiteTurn)) {
                                undo();
                                lastTurn = oldTurn;
                                return false;
                            }
                            undo();
                        }
                        try {
                            if (p.checkAttack(i, j)) {
                                int id = getPiece(i, j).get().getID();
                                if (!p.getHasMoved()) {
                                    p.setHasMoved(true);
                                    lastTurn = "t" + p.getX() + p.getY() + i + j + "@" + id;
                                } else
                                    lastTurn = "t" + p.getX() + p.getY() + i + j + "$" + id;
                                getPiece(i, j).get().die();
                                p.move(i, j);
                                if (!isCheck(!whiteTurn)) {
                                    undo();
                                    lastTurn = oldTurn;
                                    return false;
                                }
                                undo();
                            }
                        } catch (NullPointerException e) {}
                    }
                }
            }
        }
        lastTurn = oldTurn;
        return true;
    }

    public void undo()
    {
        if (lastTurn.equals("-")) {
            System.err.println("Nothing to undo!");
            return;
        }
        try {
            Piece p = getPiece(lastTurn.charAt(3) - '0', lastTurn.charAt(4) - '0').get();
            p.move(lastTurn.charAt(1) - '0', lastTurn.charAt(2) - '0');
            if (lastTurn.charAt(5) == '@')
                p.setHasMoved(false);
        } catch (NullPointerException e) { System.err.println("Can't undo, the turn must be fucked up! " + lastTurn); }

        if (lastTurn.charAt(0) == 't') {
            int id = Integer.parseInt(lastTurn.substring(6));
            try {
                getPiece(id).get().respawn();
            } catch (NullPointerException e) { System.err.println("Attempt to respawn nonexistent piece"); }

        }
    }

    public int needsPromotion(boolean colour) {
        if (colour) {
            for (Piece p : pieces)
                if (p.getType() == 'p' && p.isAlive() && p.colour == colour && p.y == 7)
                    return p.getID();
        }
        else {
            for (Piece p : pieces)
                if (p.getType() == 'p' && p.isAlive() && p.colour == colour && p.y == 0)
                    return p.getID();
        }
        return -1;
    }

    public void promote(int id, char type) {
        int i;
        for (i = 0; i < pieces.length; ++i) {
            if (pieces[i].getID() == id) break;
        }

        if (i == pieces.length) {
            System.err.println("Attempted to promote nonexistent piece at id " + id);
            return;
        }

        switch (type) {
            case 'R':
                pieces[i] = new Rook(pieces[i].getX(), pieces[i].getY(), pieces[i].colour, true, true);
                break;
            case 'N':
                pieces[i] = new Knight(pieces[i].getX(), pieces[i].getY(), pieces[i].colour, true, true);
                break;
            case 'B':
                pieces[i] = new Bishop(pieces[i].getX(), pieces[i].getY(), pieces[i].colour, true, true);
                break;
            case 'Q':
                pieces[i] = new Queen(pieces[i].getX(), pieces[i].getY(), pieces[i].colour, true, true);
                break;
            default:
                System.err.println("Illegal promotion, use RNBQ");
        }
    }
    public boolean getTurn() { return whiteTurn; }
}
