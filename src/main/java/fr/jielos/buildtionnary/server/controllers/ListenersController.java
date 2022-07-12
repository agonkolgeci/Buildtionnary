package fr.jielos.buildtionnary.server.controllers;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameComponent;
import fr.jielos.buildtionnary.server.listeners.CancelledEvents;
import fr.jielos.buildtionnary.server.listeners.PlayerConnection;
import org.bukkit.plugin.PluginManager;

public class ListenersController extends GameComponent implements ServerController {

    private final PluginManager pluginManager;
    public ListenersController(Buildtionnary instance, Game game) {
        super(instance, game);

        this.pluginManager = instance.getServer().getPluginManager();
    }

    @Override
    public void register() {
        pluginManager.registerEvents(new PlayerConnection(instance, game), instance);
        pluginManager.registerEvents(new CancelledEvents(instance, game), instance);
    }
}
