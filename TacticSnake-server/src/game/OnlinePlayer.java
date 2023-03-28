package game;

import com.shared.events.Event;
import com.shared.game.Game;
import com.shared.player.Snake;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class OnlinePlayer extends Snake {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final String name;

    public OnlinePlayer(Game assignedGame, String name, ObjectOutputStream out, Socket socket) {
        super(assignedGame);
        this.socket = socket;
        this.out = out;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    @Override
    public void sendEvent(Event event) {
        try {
            out.writeObject(event);
            out.flush();
        } catch (IOException e) {
            System.out.println("Couldn't send event to client " + this);
        }
    }
}
