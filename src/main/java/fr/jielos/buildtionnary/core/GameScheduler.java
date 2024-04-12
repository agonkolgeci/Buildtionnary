package fr.jielos.buildtionnary.core;

import fr.jielos.buildtionnary.Buildtionnary;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class GameScheduler extends BukkitRunnable {

    protected final Buildtionnary instance;
    protected final Game game;
    public GameScheduler(Buildtionnary instance, Game game) {
        this.instance = instance;
        this.game = game;
    }

}
