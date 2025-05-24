package com.agonkolgeci.jielos.buildtionnary.plugin;

public interface PluginAdapter {

    void load() throws RuntimeException;
    void unload();

}
