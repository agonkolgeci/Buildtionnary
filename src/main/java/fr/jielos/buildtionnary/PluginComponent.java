package fr.jielos.buildtionnary;

public abstract class PluginComponent {

    protected final Buildtionnary instance;
    protected PluginComponent(Buildtionnary instance) {
        this.instance = instance;
    }

}
