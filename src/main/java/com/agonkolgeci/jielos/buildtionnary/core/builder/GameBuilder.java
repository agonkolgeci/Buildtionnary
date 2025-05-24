package com.agonkolgeci.jielos.buildtionnary.core.builder;

import com.agonkolgeci.jielos.buildtionnary.core.GameManager;
import com.agonkolgeci.jielos.buildtionnary.core.player.GamePlayer;
import com.agonkolgeci.jielos.buildtionnary.utils.PlayerUtils;
import com.agonkolgeci.jielos.buildtionnary.utils.time.DateUtils;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class GameBuilder extends BukkitRunnable {

    private final GameManager gameManager;
    @Getter private final GamePlayer gamePlayer;
    @Getter private final String word;

    @Getter private int seconds;

    public GameBuilder(GameManager gameManager, GamePlayer gamePlayer, String word) {
        this.gameManager = gameManager;
        this.gamePlayer = gamePlayer;
        this.word = word;

        this.seconds = gameManager.getConfig().getTimerBuild();
    }

    @Override
    public void run() {
        seconds--;

        if(seconds <= 0) {
            this.cancel();

            gameManager.getBuildersManager().next();

            return;
        }

        gamePlayer.getPlayer().sendActionBar(
                Component.text()
                        .append(Component.text("Il vous reste ", NamedTextColor.GRAY))
                        .append(Component.text(DateUtils.formatSeconds(seconds), NamedTextColor.YELLOW))
                        .append(Component.text("...", NamedTextColor.GRAY))
                        .build()
        );

        for(GamePlayer gamePlayer : gameManager.getPlayersManager().getPlayers()) {
            if(seconds <= 5) {
                gamePlayer.getPlayer().playSound(PlayerUtils.SOUND_SUCCESS, Sound.Emitter.self());
                gamePlayer.getPlayer().showTitle(Title.title(Component.text(seconds, NamedTextColor.RED), Component.empty(), Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ZERO)));
            }
        }
    }

    public String getHint() {
        // La progression commencent uniquement à la moitié du temps
        double rawProgress = 1 - (seconds / (double) gameManager.getConfig().getTimerBuild());
        double progress = Math.clamp((rawProgress - 0.5) * 2, 0, 0.5);
        final int letters = (int) Math.ceil(progress * this.getTrueLetters());

        final StringBuilder hint = new StringBuilder();

        for (int i = 0, revealed = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            if (!Character.isLetter(c)) {
                hint.append(c); // ignorer espaces, ...
            } else if (revealed < letters) {
                hint.append(c);
                revealed++;
            } else {
                // cacher la lettre
                hint.append("_");
            }
        }

        return hint.toString();
    }

    private int getTrueLetters() {
        int count = 0;
        for (char c : word.toCharArray()) {
            if (Character.isLetter(c)) {
                count++;
            }
        }
        return count;
    }
}
