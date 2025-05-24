package com.agonkolgeci.jielos.buildtionnary;

import com.agonkolgeci.jielos.buildtionnary.core.GameManager;
import com.agonkolgeci.jielos.buildtionnary.plugin.PluginAdapter;
import com.agonkolgeci.jielos.buildtionnary.plugin.events.EventsManager;
import com.google.gson.Gson;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.SplittableRandom;
import java.util.logging.Logger;

public class Buildtionnary implements PluginAdapter {

    public static final SplittableRandom SPLITTABLE_RANDOM = new SplittableRandom();
    public static final Gson GSON = new Gson();
    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Getter private final JavaPlugin plugin;
    @Getter private final Logger logger;

    @Getter private final GameManager gameManager;

    @Getter private final EventsManager eventsManager;

    public Buildtionnary(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();

        this.gameManager = new GameManager(this);

        this.eventsManager = new EventsManager(this);
    }

    @Override
    public void load() throws RuntimeException {
        gameManager.load();
    }

    @Override
    public void unload() {
        gameManager.unload();
    }
}
