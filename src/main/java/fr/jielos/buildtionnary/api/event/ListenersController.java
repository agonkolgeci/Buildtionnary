package fr.jielos.buildtionnary.api.event;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.core.Game;
import fr.jielos.buildtionnary.core.GameComponent;
import fr.jielos.buildtionnary.core.events.CancelledEvents;
import org.bukkit.plugin.PluginManager;

public class ListenersController extends GameComponent implements ServerController {

    private final PluginManager pluginManager;
    public ListenersController(Buildtionnary instance, Game game) {
        super(instance, game);

        this.pluginManager = instance.getServer().getPluginManager();
    }

    @Override
    public void register() {
        pluginManager.registerEvents(new CancelledEvents(instance, game), instance);
    }
}
