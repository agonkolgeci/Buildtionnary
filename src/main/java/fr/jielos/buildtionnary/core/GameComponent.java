package fr.jielos.buildtionnary.core;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.PluginComponent;

public abstract class GameComponent extends PluginComponent {

    protected final Game game;
    protected GameComponent(Buildtionnary instance, Game game) {
        super(instance);

        this.game = game;
    }
}
