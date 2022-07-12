package fr.jielos.buildtionnary.game;

import fr.jielos.buildtionnary.Buildtionnary;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class GameScheduler extends BukkitRunnable {

    protected final Buildtionnary instance;
    protected final Game game;
    protected GameScheduler(Buildtionnary instance, Game game) {
        this.instance = instance;
        this.game = game;
    }
}
