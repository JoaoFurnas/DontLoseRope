package org.academiadecodigo.thunderstructs.charlie;

import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.string.StringInputScanner;
import org.academiadecodigo.thunderstructs.charlie.Generators.ChallengeGenerator;
import org.academiadecodigo.thunderstructs.charlie.Generators.Count;
import org.academiadecodigo.thunderstructs.charlie.Generators.GFXGenerator;
import org.academiadecodigo.thunderstructs.charlie.Generators.MenuGenerator;
import org.academiadecodigo.thunderstructs.charlie.Utilities.Color;
import org.academiadecodigo.thunderstructs.charlie.Utilities.Messages;
import org.w3c.dom.ls.LSOutput;

import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerHandler implements Runnable {

    private Socket playerSocket;
    private PrintWriter printToPlayer;
    private String name;
    private Team team;
    private Prompt prompt;
    private Game game;
    private int gameRoom;
    private boolean quit;
    private String lastAnswer;

    public PlayerHandler(Socket playerSocket) {

        this.playerSocket = playerSocket;

        try {
            this.prompt = new Prompt(playerSocket.getInputStream(), new PrintStream(playerSocket.getOutputStream()));
            this.printToPlayer = new PrintWriter(playerSocket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        try {
            this.name = MenuGenerator.askName(playerSocket);
            joinPlayerMap();

            while (!quit) {
                playerRun();
            }
            exit();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Add this player to the server players list.
     */
    public void joinPlayerMap() {
        Server.getPlayers().put(name, this);
    }

    /**
     * Main menu screen is shown to the player allowing them to choose from the menu options.
     */
    public void playerRun() {

        int menuOption = MenuGenerator.mainMenu(prompt);
        System.out.println("menu sent");

        try {

            switch (menuOption) {
                case 0:
                    quit = true;
                    break;
                case 1:
                    chooseGameRoom();
                    break;
                case 2:
                    createNewGame();
                    break;
                case 3:
                    printToPlayer.println(GFXGenerator.showGameInstructions());
                    break;
            }

            if (game != null) {
                joinGame();
            }

        } catch (InterruptedException ie){
            System.err.println("Something went wrong with Count Down");
        }
    }

    /**
     *
     */
    public void chooseGameRoom() {

        while (team == null) {

            if ((game = gameToEnter()) == null) {
                return;
            }

            if (!game.hasEmptySlots()) {
                System.err.println("\n Game is full" + game.getPlayers().length);
                printToPlayer.println(Messages.GAME_FULL);
                continue;
            }

            System.out.println(gameRoom + " had space in game " + game.toString());
            team = chooseTeam();
        }
    }

    public Game gameToEnter() {

        gameRoom = MenuGenerator.joinGame(prompt);
        if (gameRoom == 0) {
            return null;
        }

        return Server.getGames().get(gameRoom);
    }


    public void joinGame() throws InterruptedException{

        printToPlayer.println(name + " has joined " + team + " team in " + game.toString() + " game.\n");
        game.addPlayer(this);

        while (game.getActivePlayers() != game.getNumMaxPlayers()) {
        }

        startCountDown(printToPlayer);

        while (game.getScore() > 0 && game.getScore() < 100) {
            // TODO: 08/11/2019 HARD: make it leave input when game over
            if (game.getPlayers()[0] == null) {
                reset();
                return;
            }
            sendChallenge(this);
        }

        winGame();
    }

    public Team chooseTeam() {
        return MenuGenerator.chooseTeam(prompt, game);
    }

    public void createNewGame(){

        boolean createGame = false;
        Game creatingGame = new Game(null,0, null, 0, null,null,false);
        String currentGameInfo = "Insert game info here (name, maxplayers, type, difficulty, team1color, team2color)";
        // TODO: 09/11/2019 add current game info as part of Create game menu so it appears after DONT LOSE ROPE, instead of after input

        while (!createGame) {

            int menuChoice = MenuGenerator.createGameMenu(prompt);

            printToPlayer.println(currentGameInfo);

            switch (menuChoice) {

                case -1:
                    createGame = true;
                    break;

                case 0:
                    return;

                case 1:
                    creatingGame.setName(setGameName());
                    printToPlayer.println("Game name set to: " + creatingGame.getName());
                    break;

                case 2:
                    creatingGame.setNumMaxPlayers(setPlayerAmount());
                    printToPlayer.println("Max players set to: " + creatingGame.getNumMaxPlayers());
                    break;

                case 3:
                    selectTeam(creatingGame);
                    break;

                case 4:
                    creatingGame.setGameType(setGameType());
                    printToPlayer.println("Game type set to: " + creatingGame.getGameType().toString());
                    break;

                case 5:
                    creatingGame.setDifficulty(setGameDifficulty());
                    printToPlayer.println("Game difficulty set to: " + creatingGame.getDifficulty());
                    break;
            }
        }

        //Game creatingGame = new Game(gameName, playerAmount, gameType, gameDifficulty, team1Color, team2Color, false);
        Server.getGames().put(Server.getGames().size() + 1, creatingGame);

    }

    private String setGameName() {

        try {
            return MenuGenerator.setGameName(playerSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int setPlayerAmount() {

        try {
            return MenuGenerator.setMaxNumbers(playerSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void selectTeam(Game creatingGame) {

        while (true) {

            int selectedTeam = MenuGenerator.selectTeam(prompt);

            switch (selectedTeam) {
                case 0:
                    return;

                case 1:
                    creatingGame.setTeam1(MenuGenerator.setTeamsColor(creatingGame, prompt));
                    break;

                case 2:
                    creatingGame.setTeam2(MenuGenerator.setTeamsColor(creatingGame, prompt));
                    break;
            }
        }
    }

    private GameType setGameType() {
        return MenuGenerator.setGameType(prompt);
    }

    private int setGameDifficulty() {
        return MenuGenerator.setGameDifficulty(prompt);
    }

    private void sendChallenge(PlayerHandler player) {

        switch (game.getGameType()) {
            case CALC:
                game.checkEquation(ChallengeGenerator.generateEquation(game.getDifficulty()), player);
                break;

            case WORDS:
                game.checkWord(ChallengeGenerator.generateWord(game.getDifficulty()), player);
                break;
        }
    }

    public void winGame() {
        // TODO: this assumes that first player belongs to one team and that de following player will always be from the other team
        printToPlayer.println(GFXGenerator.drawRope(game.getScore(), game.getPlayers()[0].getTeam(), game.getPlayers()[1].getTeam()));
        game.gameOver(this);
        reset();
    }

    public void reset() {
        team = null;
        game = null;
    }

    public void exit() throws IOException {

        printToPlayer.println(Messages.QUIT);
        Server.getPlayers().remove(name);
        playerSocket.close();
        Thread.currentThread().interrupt();
    }

    private void startCountDown(PrintWriter printToPlayer) throws InterruptedException {

        for (int i = 0; i < 4; i++){

            switch(i){
                case 0:
                    printToPlayer.println(GFXGenerator.drawCountDown(Count.READY));
                    Thread.sleep(1000);
                    break;
                case 1:
                    printToPlayer.println(GFXGenerator.drawCountDown(Count.TREE));
                    Thread.sleep(1000);
                    break;
                case 2:
                    printToPlayer.println(GFXGenerator.drawCountDown(Count.TWO));
                    Thread.sleep(1000);
                    break;
                case 3:
                    printToPlayer.println(GFXGenerator.drawCountDown(Count.ONE));
                    Thread.sleep(1000);
                    break;
            }

        }

    }

    public PrintWriter getOutputStream() {
        return printToPlayer;
    }

    public String getName() {
        return name;
    }

    public Team getTeam() {
        return team;
    }

    public int getGameRoom() {
        return gameRoom;
    }

    public Prompt getPrompt() {
        return prompt;
    }
}
