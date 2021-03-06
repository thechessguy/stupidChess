package View;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import Events.*;
import Logging.*;
import Piece.*;

/**
 * Created by yury on 13.12.16.
 */
public class ScreenDisplay extends JFrame implements View {
    private int width = 545, height = 545 + 20;
    private ChessPanel boardPane;
    private JPanel settingsPane;

    private Board chessboard = Board.INSTANCE;

    private LinkedList<SettingsEventListener> listeners = new LinkedList<>();
    private LinkedList<LogEventListener> logListeners = new LinkedList<>();
    private LinkedList<SaveLoadListener> saveListeners = new LinkedList<>();

    // Singleton
    public static ScreenDisplay INSTANCE = new ScreenDisplay();
    private ScreenDisplay() {
        super("Chess");
        throwLogEvent("Initializing display...", true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel contentPane = new JPanel();

        contentPane.setBounds(0, 0, width, height);
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(null);

        setContentPane(contentPane);
        setBounds(200, 100, width, height);

        boardPane = new ChessPanel();
        settingsPane = new JPanel();
        contentPane.add(boardPane);
        boardPane.setBounds(0, 0, width, height);
        boardPane.setVisible(false);
        contentPane.add(settingsPane);
        settingsPane.setBounds(0, 0, width, height);
        settingsPane.setVisible(true);

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        menuBar.add(gameMenu);
        JMenu exitItem = new JMenu("Exit");
        menuBar.add(exitItem);

        JMenu saveItem = new JMenu("Save");
        gameMenu.add(saveItem);
        JMenu loadItem = new JMenu("Load");
        gameMenu.add(loadItem);
        JMenu settingsMenu = new JMenu("Settings");
        gameMenu.add(settingsMenu);

        saveItem.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
                saveHandler();
            }
            public void mousePressed(MouseEvent mouseEvent) {}
            public void mouseReleased(MouseEvent mouseEvent) {}
            public void mouseEntered(MouseEvent mouseEvent) {}
            public void mouseExited(MouseEvent mouseEvent) {}
        });

        loadItem.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
                loadHandler();
            }
            public void mousePressed(MouseEvent mouseEvent) {}
            public void mouseReleased(MouseEvent mouseEvent) {}
            public void mouseEntered(MouseEvent mouseEvent) {}
            public void mouseExited(MouseEvent mouseEvent) {}
        });

        exitItem.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
                // TODO: Do smth to exit game
                System.exit(0);
            }
            public void mousePressed(MouseEvent mouseEvent) {}
            public void mouseReleased(MouseEvent mouseEvent) {}
            public void mouseEntered(MouseEvent mouseEvent) {}
            public void mouseExited(MouseEvent mouseEvent) {}
        });

        settingsMenu.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
                SettingsWindow win = new SettingsWindow(INSTANCE, throwGetSettingsEvent());
            }
            public void mousePressed(MouseEvent mouseEvent) {}
            public void mouseReleased(MouseEvent mouseEvent) {}
            public void mouseEntered(MouseEvent mouseEvent) {}
            public void mouseExited(MouseEvent mouseEvent) {}
        });

        boardPane.add(menuBar);
        this.setJMenuBar(menuBar); // This sets menuBar at the top of the window

        this.setVisible(true);
        throwLogEvent("Display has been successfully initialized!", false);
    }

    public void addLogEventListener(LogEventListener listener) {
        boardPane.addLogEventListener(listener);
        logListeners.add(listener);
    }

    public void addSaveLoadListener(SaveLoadListener listener) { saveListeners.add(listener); }

    public void update() {
        // Activate this to monitor the state of every board piece
        /*for (int i = 0; i < chessboard.pieces.length; ++i) {
            Piece p = chessboard.pieces[i];
            System.out.println(p.getX() + " " + p.getY() + " " + p.getType() + " " + p.isAlive());
        }*/
        //System.out.println("turn");

        boardPane.resetBoard();

        for (int i = 0; i < 32; ++i) {
            Piece p = chessboard.getPiece(i).get();
            if (!p.isAlive()) continue;
            boardPane.drawFigure(p.getType(), p.getColour(), p.getX(), p.getY());
        }

        boardPane.repaint();
    }

    public void checkHandler() {
        JOptionPane.showMessageDialog(this, "Check!", "Watch out!", JOptionPane.WARNING_MESSAGE);
    }

    public void mateHandler() {
        String s = "Checkmate!\n";
        if (!chessboard.getTurn())
            s += "White ";
        else
            s += "Black ";
        s += "wins!";
        JOptionPane.showMessageDialog(this, s, "Game over!", JOptionPane.WARNING_MESSAGE);
    }

    public String promotionHandler() {
        Object[] possibilities = {"Queen", "Rook", "Knight", "Bishop"};
        String s = (String)JOptionPane.showInputDialog(this, "Please, choose figure, you want your pawn to be promoted:",
                "Promotion!", JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);
        if (s == null) s = "Queen";
        if (s.equals("Knight")) s = "NKnight"; // Костыль (у коня в обозначении первый символ N)
        return s;
    }

    public void waitHandler() {
        int style = Font.BOLD, size = 14;
        int x = width / 2 - 30, y = 20;
        Graphics g = boardPane.getGraphics();
        Font f = new Font("Dialog", style, size);
        g.setFont(f);
        g.drawString("Waiting...", x, y);
    }

    public String netPrompt() {
        Object[] possibilities = {"Local game", "Become a server", "Become a client"};
        String s = (String)JOptionPane.showInputDialog(this, "Choose the type of game:", "Welcome!",
                JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);
        if (s == null) return null;
        if (s.equals(possibilities[0])) s = "local";
        else if (s.equals(possibilities[1])) s = "server";
        else s = "client";

        return s;
    }

    public String clientPrompt() {
        String ip;
        do {
            ip = (String) JOptionPane.showInputDialog(this, "Enter server IP address:", "Connecting to a server",
                    JOptionPane.PLAIN_MESSAGE, null, null, "192.168.");
        } while (ip == null);
        return ip;
    }

    public void serverPrompt(String ip) {
        JOptionPane.showMessageDialog(this, "Your ip address is:\n" + ip + "\n(Please, close this window before connecting to a client)");
    }

    public void startHandler() {
        settingsPane.setVisible(false);
        boardPane.setVisible(true);
        update();
    }

    public boolean colourPrompt() {
        Object[] possibilities = {"White", "Black"};
        String s = (String)JOptionPane.showInputDialog(this, "Choose your color:", "Welcome!",
                JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);
        if (s == null) return true;
        return s.equals(possibilities[0]);
    }

    public JPanel getSettingsPanel() { return settingsPane; }
    public ChessPanel getChessPanel() { return boardPane; }

    public void addSettingsEventListener(SettingsEventListener listener) {
        boardPane.addSettingsEventListener(listener);
        listeners.add(listener);
    }
    public void throwSettingsEvent(Settings s) {
        for (SettingsEventListener listener : listeners)
            listener.updateSettings(new SettingsEvent(this, s));
    }

    private Settings throwGetSettingsEvent() {
        Settings s = null;
        for (SettingsEventListener listener : listeners)
            if (listener.getCurrentSettings() != null) s = new Settings(listener.getCurrentSettings());
        return s;
    }

    private void saveHandler() {
        String filename;
        filename = (String) JOptionPane.showInputDialog(this, "Enter save name:\n", "Saving",
                JOptionPane.PLAIN_MESSAGE, null, null, "");
        if (filename != null) throwSaveLoadEvent(new SaveLoadEvent(this, filename), 's');
    }

    private void loadHandler() {
        String path = getCurr_path() + "/saves";
        File folder = new File(path);
        if (!folder.exists()) {
            System.err.println("Can't find any saved game!");
            throwLogEvent("Error: Can't find any saved game!");
            return;
        }

        File[] flist = folder.listFiles();
        if (flist == null || flist.length == 0) {
            System.err.println("ERROR: There are no saved games!");
            throwLogEvent("Error: Can't find any saved games");
            return;
        }

        int cnt = 0;
        for (int i = 0; i < flist.length; ++i)
            if (flist[i].isFile() && flist[i].getName().endsWith(".sav")) cnt++;
        if (cnt == 0) {
            System.err.println("ERROR: There are no saved games!");
            throwLogEvent("Error: Can't find any saved games");
            return;
        }

        Object[] files = new Object[cnt];
        int id = 0;
        for (int i = 0; i < flist.length; ++i) {
            if (flist[i].isFile() && flist[i].getName().endsWith(".sav")) {
                String name = flist[i].getName();
                files[id] = name.substring(0, name.length() - 4); // Delete ".sav" at the end of the filename
                id++;
            }
        }
        String filename = (String)JOptionPane.showInputDialog(this, "Choose saved game:", "Loading",
                JOptionPane.PLAIN_MESSAGE, null, files, files[0]);
        if (filename == null) return;
        throwSaveLoadEvent(new SaveLoadEvent(this, filename + ".sav"), 'l');
        update();
    }

    private void throwLogEvent(String msg) {
        for (LogEventListener listener : logListeners)
            listener.logMessage(new LogEvent(this, msg));
    }
    private void throwLogEvent(String msg, boolean enter) {
        for (LogEventListener listener : logListeners)
            listener.logFunction(new LogEvent(this, msg), enter);
    }
    private void throwLogEvent(String msg, Exception ex) {
        for (LogEventListener listener : logListeners)
            listener.logError(new LogEvent(this, msg, ex));
    }

    private String getCurr_path() {
        String s = null;
        for (LogEventListener listener : logListeners)
            s = listener.getCurrentPath();
        return s;
    }

    private void throwSaveLoadEvent(SaveLoadEvent e, char type) {
        for (SaveLoadListener listener : saveListeners) {
            if (type == 's')
                listener.save(e);
            else if (type == 'l')
                listener.load(e);
        }
    }
}

class ChessPanel extends JPanel {
    private BufferedImage currentBoard;
    private BufferedImage boardImg;
    private BufferedImage attack, move, select;
    private BufferedImage[] figuresImg = new BufferedImage[12];

    private Board chessboard = Board.INSTANCE;

    private Graphics g;

    private static final int tileWidth = 60, tileHeight = 60;
    private static final Point startPoint = new Point(27, 27);

    private LinkedList<SettingsEventListener> listeners = new LinkedList<>();
    private LinkedList<LogEventListener> logListeners = new LinkedList<>();

    public ChessPanel() {
        throwLogEvent("Initializing board...", true);
        try {
            boardImg = ImageIO.read(getClass().getResourceAsStream("/res/images/board.jpg"));
        } catch (IOException e) {
            System.err.println(e);
        }

        loadImages();
        throwLogEvent("Board has been successfully initialized!", false);
    }

    public void addLogEventListener(LogEventListener listener) { logListeners.add(listener); }

    public void paintComponent(Graphics g) {
        //super.paintComponent(g);
        if (throwGetSettingsEvent().isHighlightingEnabled) drawFrames();
        g.drawImage(currentBoard, 0, 0, null);
        chessboard.setSelectedFigure(null);
        chessboard.setBoardState();
    }

    public void resetBoard() {
        ColorModel cm = boardImg.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = boardImg.copyData(null);
        currentBoard = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public void drawFigure(char type, boolean colour, int x, int y) {
        int startX = startPoint.x + tileWidth * x + x, startY = startPoint.y + tileHeight * (7 - y) + (7 - y);

        if (g == null) g = getGraphics();
        Graphics2D g2 = currentBoard.createGraphics();

        switch (type) {
            case 'K':
                if (colour) g2.drawImage(figuresImg[10], startX, startY, null);
                else g2.drawImage(figuresImg[11], startX, startY, null);
                break;
            case 'N':
                if (colour) g2.drawImage(figuresImg[4], startX, startY, null);
                else g2.drawImage(figuresImg[5], startX, startY, null);
                break;
            case 'B':
                if (colour) g2.drawImage(figuresImg[6], startX, startY, null);
                else g2.drawImage(figuresImg[7], startX, startY, null);
                break;
            case 'p':
                if (colour) g2.drawImage(figuresImg[0], startX, startY, null);
                else g2.drawImage(figuresImg[1], startX, startY, null);
                break;
            case 'Q':
                if (colour) g2.drawImage(figuresImg[8], startX, startY, null);
                else g2.drawImage(figuresImg[9], startX, startY, null);
                break;
            case 'R':
                if (colour) g2.drawImage(figuresImg[2], startX, startY, null);
                else g2.drawImage(figuresImg[3], startX, startY, null);
                break;
        }
    }

    public void drawFrames() {
        if (chessboard.getSelectedFigure() == null) return;

        char[][] boardState = chessboard.getBoardState();
        Graphics2D g2 = (Graphics2D)currentBoard.getGraphics();

        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                char ch = boardState[i][j];
                int startX = startPoint.x + tileWidth * i + i, startY = startPoint.y + tileHeight * (7 - j) + (7 - j);
                if (ch == 'a') {
                    g2.drawImage(attack, startX, startY, null);
                } else if (ch == 'm') {
                    g2.drawImage(move, startX, startY, null);
                } else if (ch == 's') {
                    g2.drawImage(select, startX, startY, null);
                }
            }
        }
    }

    public void addSettingsEventListener(SettingsEventListener listener) { listeners.add(listener); }

    private Settings throwGetSettingsEvent() {
        Settings s = null;
        for (SettingsEventListener listener : listeners)
            if (listener.getCurrentSettings() != null) s = new Settings(listener.getCurrentSettings());
        return s;
    }

    private void throwLogEvent(String msg) {
        for (LogEventListener listener : logListeners)
            listener.logMessage(new LogEvent(this, msg));
    }
    private void throwLogEvent(String msg, boolean enter) {
        for (LogEventListener listener : logListeners)
            listener.logFunction(new LogEvent(this, msg), enter);
    }
    private void throwLogEvent(String msg, Exception ex) {
        for (LogEventListener listener : logListeners)
            listener.logError(new LogEvent(this, msg, ex));
    }

    private void loadImages() {
        throwLogEvent("Loading images", true);
        try {
            figuresImg[0] = ImageIO.read(getClass().getResourceAsStream("/res/images/pawn-w.png"));
            figuresImg[1] = ImageIO.read(getClass().getResourceAsStream("/res/images/pawn-b.png"));
            figuresImg[2] = ImageIO.read(getClass().getResourceAsStream("/res/images/rook-w.png"));
            figuresImg[3] = ImageIO.read(getClass().getResourceAsStream("/res/images/rook-b.png"));
            figuresImg[4] = ImageIO.read(getClass().getResourceAsStream("/res/images/knight-w.png"));
            figuresImg[5] = ImageIO.read(getClass().getResourceAsStream("/res/images/knight-b.png"));
            figuresImg[6] = ImageIO.read(getClass().getResourceAsStream("/res/images/bishop-w.png"));
            figuresImg[7] = ImageIO.read(getClass().getResourceAsStream("/res/images/bishop-b.png"));
            figuresImg[8] = ImageIO.read(getClass().getResourceAsStream("/res/images/queen-w.png"));
            figuresImg[9] = ImageIO.read(getClass().getResourceAsStream("/res/images/queen-b.png"));
            figuresImg[10] = ImageIO.read(getClass().getResourceAsStream("/res/images/king-w.png"));
            figuresImg[11] = ImageIO.read(getClass().getResourceAsStream("/res/images/king-b.png"));

            attack = ImageIO.read(getClass().getResourceAsStream("/res/images/red-sqr.png"));
            move = ImageIO.read(getClass().getResourceAsStream("/res/images/blue-sqr.png"));
            select = ImageIO.read(getClass().getResourceAsStream("/res/images/yellow-sqr.png"));
            throwLogEvent("Successfully!", false);
        } catch (IOException e) {
            System.err.println("Error while loading images: " + e);
            throwLogEvent("Error while loading images: ", e);
        }
    }
}