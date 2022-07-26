package fr.jielos.buildtionnary.game.data;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameComponent;
import fr.jielos.buildtionnary.game.data.players.GamePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GameData extends GameComponent {

    private final List<Player> players;
    private final List<Player> spectators;

    private final Map<Player, GamePlayer> gamePlayers;
    private List<GamePlayer> gameWinners;

    public GameData(Buildtionnary instance, Game game) {
        super(instance, game);

        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();

        this.gamePlayers = new HashMap<>();
        this.gameWinners = new ArrayList<>();
    }

    public List<Player> getPlayers() {
        return players;
    }
    public List<Player> getSpectators() {
        return spectators;
    }

    public GamePlayer addGamePlayer(Player player) {
        final GamePlayer gamePlayer = new GamePlayer(instance, game, player);
        gamePlayers.put(player, gamePlayer);

        return gamePlayer;
    }
    public void removeGamePlayer(Player player) {
        if(hasGamePlayer(player)) {
            gamePlayers.remove(player);
        }
    }
    public boolean hasGamePlayer(Player player) {
        return gamePlayers.containsKey(player);
    }
    public GamePlayer getGamePlayer(Player player) {
        return gamePlayers.get(player);
    }

    public Map<Player, GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public List<GamePlayer> getSortedGamePlayers() {
        return gamePlayers.values().stream().sorted(Comparator.comparingInt(GamePlayer::getPoints).reversed()).collect(Collectors.toList());
    }

    public List<GamePlayer> getGameWinners() {
        return gameWinners;
    }
    public List<GamePlayer> setGameWinners(List<GamePlayer> gameWinners) {
        return this.gameWinners = gameWinners;
    }
}
