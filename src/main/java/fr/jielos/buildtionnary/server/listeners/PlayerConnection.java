package fr.jielos.buildtionnary.server.listeners;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnection extends GameComponent implements Listener {

    public PlayerConnection(Buildtionnary instance, Game game) {
        super(instance, game);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        event.setJoinMessage(null);

        if(game.isWaiting()) {
            game.getGameController().addPlayer(player);
        } else {
            game.getGameController().addSpectator(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        event.setQuitMessage(null);

        game.getGameController().removePlayer(player, false);
    }

}
