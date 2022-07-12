package fr.jielos.buildtionnary.server.listeners;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.game.Game;
import fr.jielos.buildtionnary.game.GameComponent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class CancelledEvents extends GameComponent implements Listener {

    public CancelledEvents(Buildtionnary instance, Game game) {
        super(instance, game);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        if(entity instanceof Player) {
            event.setCancelled(true);
        }
    }

}
