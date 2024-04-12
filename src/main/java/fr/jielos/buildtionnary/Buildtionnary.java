package fr.jielos.buildtionnary;

import fr.jielos.buildtionnary.core.Game;
import fr.jielos.buildtionnary.api.event.ListenersController;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.SplittableRandom;

public final class Buildtionnary extends JavaPlugin {

    private static Buildtionnary instance;
    private SplittableRandom splittableRandom;

    private Game game;

    @Override
    public void onEnable() {
        instance = this;
        splittableRandom = new SplittableRandom();

        try {
            game = new Game(this);
            game.initGame();

            new ListenersController(instance, game).register();
        } catch (Exception exception) {
            exception.printStackTrace();
            disable();
        }
    }

    public void disable() {
        getLogger().severe("An error occurred while starting the plugin, impossible to continue.");
        getPluginLoader().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        for(Player player : getServer().getOnlinePlayers()) {
            game.getBoardController().destroyPlayerBoard(player);
        }
    }

    public static Buildtionnary getInstance() {
        return instance;
    }

    public SplittableRandom getSplittableRandom() {
        return splittableRandom;
    }

    public Game getGame() {
        return game;
    }
}
