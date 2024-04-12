package fr.jielos.buildtionnary.core.board;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.core.Game;
import fr.jielos.buildtionnary.core.GameComponent;
import fr.jielos.buildtionnary.core.cache.player.PlayerBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class BoardController extends GameComponent implements Listener {

    private final Map<Player, PlayerBoard> boards;
    public BoardController(Buildtionnary instance, Game game) {
        super(instance, game);

        this.boards = new HashMap<>();

        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    public void updatePlayerBoard(Player player) {
        getPlayerBoard(player).update();
    }

    public boolean hasPlayerBoard(Player player) {
        return boards.containsKey(player);
    }

    public PlayerBoard getPlayerBoard(Player player) {
        if(hasPlayerBoard(player)) {
            return boards.get(player);
        }

        return new PlayerBoard(instance, game, player);
    }

    public void destroyPlayerBoard(Player player) {
        if(hasPlayerBoard(player)) {
            getPlayerBoard(player).destroy();
        }
    }

    public void removePlayerBoard(Player player) {
        if(hasPlayerBoard(player)) {
            boards.remove(player);
        }
    }

    public Map<Player, PlayerBoard> getBoards() {
        return boards;
    }
}
