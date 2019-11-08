package org.academiadecodigo.thunderstructs.charlie;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {

    private ServerSocket serverSocket;
    private static Map<String, PlayerHandler> players = new HashMap<>();
    private static Map<Integer, Game> games = new HashMap<>();

    private Server(ServerSocket serverSocket) {

        this.serverSocket = serverSocket;

    }

    public static void main(String[] args) {

        try {

            Server server = new Server(new ServerSocket(8080));

            Game g1 = new Game(2, GameType.CALC, 1, Team.BLUE, Team.RED);
            Game g2 = new Game(2, GameType.WORDS, 1, Team.RED, Team.BLUE);
            Game g3 = new Game(2, GameType.WORDS, 1, Team.RED, Team.BLUE);
            Game g4 = new Game(2, GameType.CALC, 1, Team.RED, Team.BLUE);

            games.put(3,g3);
            games.put(4,g4);
            games.put(1, g1);
            games.put(2, g2);

            server.init();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    private void init() {

        try {

            ExecutorService s1 = Executors.newCachedThreadPool();

            while (serverSocket.isBound()) {

                System.out.println("Server: Waiting for player to join...");
                Socket playerSocket = serverSocket.accept();
                System.out.println(playerSocket.getInetAddress().toString());
                PlayerHandler playerHandler = new PlayerHandler(playerSocket);
                s1.submit(playerHandler);
                System.out.println("Server: New player joined.");

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }



    public static Map<Integer, Game> getGames() {
        return games;
    }

    public static Map<String, PlayerHandler> getPlayers() {
        return players;
    }

}