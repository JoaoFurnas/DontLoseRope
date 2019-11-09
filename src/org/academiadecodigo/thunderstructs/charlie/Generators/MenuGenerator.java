package org.academiadecodigo.thunderstructs.charlie.Generators;

import org.academiadecodigo.bootcamp.Prompt;
import org.academiadecodigo.bootcamp.scanners.menu.MenuInputScanner;

import org.academiadecodigo.thunderstructs.charlie.Game;
import org.academiadecodigo.thunderstructs.charlie.GameType;
import org.academiadecodigo.thunderstructs.charlie.Server;
import org.academiadecodigo.thunderstructs.charlie.Team;
import org.academiadecodigo.thunderstructs.charlie.Utilities.Messages;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;


public class MenuGenerator {

    public static String askName(Socket playerSocket) throws IOException {

        Scanner input = new Scanner(playerSocket.getInputStream());
        PrintWriter printWriter = new PrintWriter(playerSocket.getOutputStream(), true);

        printWriter.println(GFXGenerator.clearScreen() + GFXGenerator.drawGameTitle() + Messages.WELCOME);

        return input.nextLine();

    }

    public static int mainMenu(Prompt prompt) {

        String[] menu = {"Join game", "Create game", "How to play", "Quit"};

        MenuInputScanner menuInputScanner = new MenuInputScanner(menu);
        menuInputScanner.setMessage(GFXGenerator.clearScreen() + GFXGenerator.drawGameTitle() + Messages.MAIN_MENU);

        int choice = prompt.getUserInput(menuInputScanner);

        if (choice <= menu.length - 1) {
            return choice;
        }

        return 0;

    }

    public static int joinGame(Prompt prompt) {

        String[] games = new String[Server.getGames().size() + 1];

        int counter = 1;
        for (Game s : Server.getGames().values()) {
            games[counter - 1] = s.getGameType().toString().substring(0, 1) +
                    s.getGameType().toString().substring(1).toLowerCase() + " game";
            counter++;
        }

        games[games.length - 1] = "Go back";

        MenuInputScanner menu = new MenuInputScanner(games);
        menu.setMessage(GFXGenerator.clearScreen() + GFXGenerator.drawGameTitle() + Messages.JOIN_GAME);

        int choice = prompt.getUserInput(menu);

        if (choice < games.length - 1) {
            return choice;
        }

        return 0;
    }

    public static int createGameMenu(Prompt prompt) {

        String[] menu = {"Set Max Numbers", "Set Team Colors", "Set Game Type", "Set Game Difficulty", "Create Game", "Go back"};

        MenuInputScanner menuInputScanner = new MenuInputScanner(menu);
        menuInputScanner.setMessage(GFXGenerator.clearScreen() + GFXGenerator.drawGameTitle() + Messages.CREATE_MENU);

        int choice = prompt.getUserInput(menuInputScanner);

        if (choice <= menu.length - 1) {
            return choice;
        }
        return 0;
    }

    public static int setMaxNumbers(Socket playerSocket) throws IOException {

        Scanner input = new Scanner(playerSocket.getInputStream());
        PrintWriter printWriter = new PrintWriter(playerSocket.getOutputStream(), true);

        printWriter.println("Set max player number: ");

        int maxNumber = Integer.parseInt(input.nextLine());

        if (maxNumber < 2) {
            setMaxNumbers(playerSocket);
        }

        return maxNumber;

    }

    public static Team setTeamsColor(Game game, Prompt prompt){

        Set<Team> teams = new HashSet<>(Arrays.asList(Team.values()));

        for (Team team : game.getTeams()) {
            teams.remove(team);
        }

        String[] choices = new String[teams.toArray().length + 1];

        for (int i = 1; i < choices.length; i++) {
            choices[i - 1] = teams.toArray()[i - 1].toString();
        }
        choices[choices.length - 1] = "Go back";

        MenuInputScanner menuInputScanner = new MenuInputScanner(choices);
        menuInputScanner.setMessage(GFXGenerator.clearScreen() + GFXGenerator.drawGameTitle() + Messages.CHOOSE_TEAM);

        int choice = prompt.getUserInput(menuInputScanner);

        Team[] teamTypes = new Team[teams.toArray().length];

        if (choice < teamTypes.length + 1) {
            System.out.println(teams.toArray()[choice - 1]);
            return (Team) teams.toArray()[choice -1];
        }
        System.out.println("null");
        return null;
    }

    public static Team chooseTeam(Prompt prompt, Game game) {

        String[] teams = new String[game.getTeams().length + 1];

        for (int i = 0; i < teams.length - 1; i++) {
            teams[i] = game.getTeams()[i].toString();
            System.out.println(teams[i]);
        }

        teams[teams.length - 1] = "Go back";

        MenuInputScanner menuInputScanner = new MenuInputScanner(teams);
        menuInputScanner.setMessage(GFXGenerator.clearScreen() + GFXGenerator.drawGameTitle() + Messages.CHOOSE_TEAM);

        int choice = prompt.getUserInput(menuInputScanner);

        Team[] teamTypes = game.getTeams();

        if (choice < teamTypes.length + 1) {
            System.out.println(teamTypes[choice - 1]);
            return teamTypes[choice - 1];
        }

        return null;

    }

    public static void main(String[] args) {
        Prompt prompt = new Prompt(System.in, System.out);
        Game game = new Game(2,GameType.CALC,1, Team.RED, Team.GREEN,false);
        MenuGenerator.setTeamsColor(game,prompt);
    }
}