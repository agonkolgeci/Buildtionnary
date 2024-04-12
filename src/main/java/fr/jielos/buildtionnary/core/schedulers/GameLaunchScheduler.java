package fr.jielos.buildtionnary.core.schedulers;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.core.Game;
import fr.jielos.buildtionnary.core.GameScheduler;
import fr.jielos.buildtionnary.core.config.ConfigController;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class GameLaunchScheduler extends GameScheduler {

    private int seconds;
    public GameLaunchScheduler(Buildtionnary instance, Game game) {
        super(instance, game);

        seconds = game.getConfigController().getInt(ConfigController.Value.TIMER_LAUNCH);
    }

    @Override
    public void run() {
        if(game.canLaunch()) {
            if(seconds > 0) {
                if(seconds % 10 == 0 || seconds <= 5) {
                    instance.getServer().broadcastMessage(String.format("§eLa partie va démarrer dans §6%d secondes §e!", seconds));
                }

                for(Player player : instance.getServer().getOnlinePlayers()) {
                    player.setLevel(seconds);

                    if(seconds <= 5) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                        player.sendTitle(ChatColor.RED.toString() + seconds, null, 0, 40, 20);
                    }

                    game.getBoardController().updatePlayerBoard(player);
                }

                seconds--;
            } else {
                stop();

                game.startGame();
            }
        } else {
            game.cancelLaunch();
        }
    }

    public void stop() {
        cancel();

        game.setGameLaunchScheduler(null);
        for(Player player : instance.getServer().getOnlinePlayers()) {
            player.setLevel(0);
        }
    }

    public int getSeconds() {
        return seconds;
    }
}
