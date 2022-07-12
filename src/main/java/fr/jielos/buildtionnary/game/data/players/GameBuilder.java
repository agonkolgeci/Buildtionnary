package fr.jielos.buildtionnary.game.data.players;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.components.display.Time;
import fr.jielos.buildtionnary.components.game.WordReference;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameScheduler;
import fr.jielos.buildtionnary.game.controllers.ConfigController;
import fr.jielos.buildtionnary.game.data.GameBuilders;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GameBuilder extends GameScheduler {

    private final GameBuilders gameBuilders;
    private final GamePlayer gamePlayer;
    private final String word;

    private int seconds;
    public GameBuilder(Buildtionnary instance, Game game, GamePlayer gamePlayer) {
        super(instance, game);

        this.gameBuilders = game.getGameBuilders();
        this.gamePlayer = gamePlayer;
        this.word = WordReference.getRandomWord();

        this.seconds = game.getConfigController().getInt(ConfigController.Value.TIMER_BUILD);

        gamePlayer.setHasBuild(true);

        runTaskTimer(instance, 0, 20);
    }

    @Override
    public void run() {
        if(gamePlayer.isPlaying()) {
            if(seconds > 0) {
                instance.getInitializer().sendActionBar(gamePlayer.getPlayer(), String.format("§7Il vous reste §r%s §7...", Time.formatSeconds(seconds, ChatColor.YELLOW, true)));

                for(Player player : game.getGameData().getPlayers()) {
                    game.getBoardController().updatePlayerBoard(player);
                }

                seconds--;

                return;
            }
        }

        stop();
    }

    public void stop() {
        cancel();

        gameBuilders.broadcastStats(this);
        gameBuilders.nextBuilder();
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
    public Player getPlayer() {
        return gamePlayer.getPlayer();
    }

    public String getWord() {
        return word;
    }

    public int getSeconds() {
        return seconds;
    }

}
