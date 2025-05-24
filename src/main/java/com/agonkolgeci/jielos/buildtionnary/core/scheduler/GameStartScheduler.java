package com.agonkolgeci.jielos.buildtionnary.core.scheduler;

import com.agonkolgeci.jielos.buildtionnary.core.GameManager;
import com.agonkolgeci.jielos.buildtionnary.utils.PlayerUtils;
import com.agonkolgeci.jielos.buildtionnary.utils.time.DateUtils;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class GameStartScheduler extends BukkitRunnable {

    private final GameManager gameManager;

    @Getter private int seconds;
    public GameStartScheduler(GameManager gameManager) {
        this.gameManager = gameManager;

        this.seconds = gameManager.getConfig().getTimerStart() + 1;
    }

    @Override
    public void run() {
        seconds--;

        if(seconds <= 0) {
            this.cancel();

            gameManager.play();

            return;
        }

        for(Player player : gameManager.getPlugin().getServer().getOnlinePlayers()) {
            player.setLevel(seconds);

            if(seconds <= 5) {
                player.playSound(PlayerUtils.SOUND_SUCCESS, Sound.Emitter.self());
                player.showTitle(Title.title(Component.text(seconds, NamedTextColor.RED), Component.empty(), Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)));
            }
        }

        if(seconds % 10 == 0 || seconds <= 5) {
            gameManager.getPlugin().getServer().playSound(PlayerUtils.SOUND_SUCCESS, Sound.Emitter.self());
            gameManager.getPlugin().getServer().broadcast(Component.text("La partie va démarrer dans").appendSpace().append(Component.text(DateUtils.formatSeconds(seconds), NamedTextColor.GOLD)).appendSpace().append(Component.text("!")).colorIfAbsent(NamedTextColor.YELLOW));
        }
    }
}
