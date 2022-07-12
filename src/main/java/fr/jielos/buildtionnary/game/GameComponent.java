package fr.jielos.buildtionnary.game;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.PluginComponent;
import fr.jielos.buildtionnary.game.Game;

public abstract class GameComponent extends PluginComponent {

    protected final Game game;
    protected GameComponent(Buildtionnary instance, Game game) {
        super(instance);

        this.game = game;
    }
}
