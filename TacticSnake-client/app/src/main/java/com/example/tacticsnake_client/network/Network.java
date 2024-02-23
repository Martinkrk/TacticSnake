package com.example.tacticsnake_client.network;

import android.util.Log;
import com.example.tacticsnake_client.events.OnlineEventManager;
import com.shared.events.PlayerResponseEvent;
import com.shared.game.GameSettings;

import java.io.*;
import java.net.*;

public class Network {
    private final String serverAddress = "2.tcp.eu.ngrok.io";
    private final int serverPort = 16585;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private OnlineEventManager onlineEventManager;
    private PlayerResponseEvent playerResponseEvent;
    private Thread connectThread = new Thread(this::connect);
    private Thread receiveThread = new Thread(this::receiveObjects);
    private Thread responseThread = new Thread(this::sendResponse);
    private boolean sendResponse = false;
    private volatile boolean stopThreads = false;

    public Network(OnlineEventManager eventManager) {
        this.onlineEventManager = eventManager;
        connectThread.start();
    }

    private void connect() {
        while (!stopThreads) {
            try {
                socket = new Socket(serverAddress, serverPort);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                break;
            } catch (IOException e) {
                Log.d("IOEXCEPTION", String.valueOf(e));
            } catch (Exception e) {
                Log.d("EXCEPTION", String.valueOf(e));
            }
        }
        if (!stopThreads) {
            Log.d("DEBUG", "LEFT SERVER CONNECTION WHILE LOOP");
            sendPreferences(onlineEventManager.getPreferences());
            receiveThread.start();
            playerResponseEvent = new PlayerResponseEvent();
            responseThread.start();
        }
    }

    private void sendPreferences(GameSettings gameSettings) {
        try {
            out.writeObject(gameSettings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendObject(Object object) {
        try {
            out.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveObjects() {
        try {
            while (!stopThreads) {
                if (in != null) {
                    Object object = in.readObject();
                    onlineEventManager.handleObject(object);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            onlineEventManager.handleConnectionError();
        }
    }

    public void close() {
        stopThreads = true;
        connectThread.interrupt();
        receiveThread.interrupt();
        responseThread.interrupt();
        try {
            if (socket != null) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();

        } catch (IOException e) {}
    }

    private void sendResponse() {
        try {
            while (!stopThreads) {
                if (sendResponse) {
                    out.writeObject(playerResponseEvent);
                }
                Thread.sleep(10000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setSendResponse(boolean sendResponse) {
        this.sendResponse = sendResponse;
    }
}