package com.agonkolgeci.jielos.buildtionnary;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildtionnaryBootstrap extends JavaPlugin {

    @Getter private static Buildtionnary instance;

    @Override
    public void onEnable() {
        if(instance != null) throw new IllegalStateException("Unable to load the plugin more than once!");

        try {
            instance = new Buildtionnary(this);
            instance.load();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Override
    public void onDisable() {
        if(instance != null) {
            instance.unload();
        }
    }
}
