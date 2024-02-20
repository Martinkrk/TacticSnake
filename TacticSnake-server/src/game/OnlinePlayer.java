package game;

import com.shared.game.Game;
import com.shared.player.Snake;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class OnlinePlayer extends Snake {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public OnlinePlayer(Game assignedGame, String nick, ObjectOutputStream out, ObjectInputStream in, Socket socket) {
        super(assignedGame, nick);
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public void setOnlineSnakeColor(int[] color) {

    }

    public void sendObject(Object object) {
        try {
            out.writeObject(object);
            out.flush();
        } catch (IOException e) {
            System.out.println("Couldn't send object to client " + this);
        }
    }
}
