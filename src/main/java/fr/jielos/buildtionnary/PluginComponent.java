package fr.jielos.buildtionnary;

public abstract class PluginComponent {

    protected final Buildtionnary instance;
    public PluginComponent(Buildtionnary instance) {
        this.instance = instance;
    }

}
