package com.agonkolgeci.jielos.buildtionnary.plugin;

import com.agonkolgeci.jielos.buildtionnary.Buildtionnary;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

@Getter
public abstract class PluginManager {

    protected final Buildtionnary instance;
    protected final JavaPlugin plugin;

    public PluginManager(Buildtionnary instance) {
        this.instance = instance;
        this.plugin = instance.getPlugin();
    }

}
