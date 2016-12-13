package Network;

import java.io.*;
import Piece.Turn;

/**
 * Created by yury on 01.12.16.
 */
public abstract class Net {
    String ServerIP;
    public abstract void init(String ip);
    public abstract void quit();
    public abstract String getIP();
    public abstract ObjectInputStream getInStream();
    public abstract ObjectOutputStream getOutStream();
    public abstract void sendTurn(Turn t);
    public abstract Turn receiveTurn();
}
