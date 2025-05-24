package com.agonkolgeci.jielos.buildtionnary.plugin.events;

import com.agonkolgeci.jielos.buildtionnary.Buildtionnary;
import com.agonkolgeci.jielos.buildtionnary.plugin.PluginManager;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class EventsManager extends PluginManager {

    @NotNull private final org.bukkit.plugin.PluginManager eventManager;

    public EventsManager(@NotNull Buildtionnary instance) {
        super(instance);

        this.eventManager = instance.getPlugin().getServer().getPluginManager();
    }

    public void registerAdapter(@NotNull ListenerAdapter listener) {
        eventManager.registerEvents(listener, instance.getPlugin());
    }

    public void unregisterAdapter(@NotNull ListenerAdapter listener) {
        HandlerList.unregisterAll(listener);
    }

    @NotNull
    public <E extends Event> E callEvent(@NotNull E event) {
        eventManager.callEvent(event);

        return event;
    }

}
