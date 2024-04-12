package fr.jielos.buildtionnary.core.cache.player;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.utils.time.DateUtils;
import fr.jielos.buildtionnary.utils.text.WordReference;
import fr.jielos.buildtionnary.core.Game;
import fr.jielos.buildtionnary.core.GameScheduler;
import fr.jielos.buildtionnary.core.config.ConfigController;
import fr.jielos.buildtionnary.core.cache.GameBuilders;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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
                gamePlayer.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(String.format("§7Il vous reste §r%s §7...", DateUtils.formatSeconds(seconds, ChatColor.YELLOW, true))));

                for(Player player : game.getGameData().getPlayers()) {
                    if(seconds <= 5) {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                        player.sendTitle(ChatColor.RED.toString() + seconds, null, 0, 40, 20);
                    }

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
