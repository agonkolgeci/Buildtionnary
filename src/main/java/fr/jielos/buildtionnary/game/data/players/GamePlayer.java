package fr.jielos.buildtionnary.game.data.players;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GamePlayer extends GameComponent {

    private final Player player;

    private int points;
    private List<GamePlayer> foundedWords;
    public GamePlayer(Buildtionnary instance, Game game, Player player) {
        super(instance, game);

        this.player = player;

        this.points = 0;
        this.foundedWords = new ArrayList<>();
    }

    public int getPoints() {
        return points;
    }
    public void addPoints(int value) {
        points += value;

        instance.getInitializer().sendActionBar(player, String.format("§7Vous venez de remportez §e%d points§7.", value));

        for(Player player : game.getGameData().getPlayers()) {
            game.getBoardController().updatePlayerBoard(player);
        }
    }

    public List<GamePlayer> getFoundedWords() {
        return foundedWords;
    }
    public boolean hasFound(GamePlayer gamePlayer) {
        return foundedWords.contains(gamePlayer);
    }

    public boolean isPlaying() {
        return game.getGameData().getPlayers().contains(player);
    }

    public Player getPlayer() {
        return player;
    }
}
