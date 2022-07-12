package fr.jielos.buildtionnary;

import fr.jielos.buildtionnary.components.display.Initializer;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.server.controllers.ListenersController;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Buildtionnary extends JavaPlugin {

    private static Buildtionnary instance;
    private Initializer initializer;

    private Game game;

    @Override
    public void onEnable() {
        instance = this;
        initializer = new Initializer();

        saveDefaultConfig();

        try {
            this.game = new Game(this);
            this.game.initGame();

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

    public Initializer getInitializer() {
        return initializer;
    }

    public Game getGame() {
        return game;
    }
}
