package fr.jielos.buildtionnary.core;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.core.config.ConfigController;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;

public class GameController extends GameComponent implements Listener {

    public GameController(Buildtionnary instance, Game game) {
        super(instance, game);

        instance.getServer().getPluginManager().registerEvents(this, instance);
    }

    public void loadOnlinePlayers() {
        for(Player player : instance.getServer().getOnlinePlayers()) {
            addPlayer(player);
        }
    }

    public void addPlayer(Player player) {
        game.getGameData().getPlayers().add(player);

        player.teleport(game.getConfigController().getLocation(ConfigController.Value.WAITING_ROOM));
        player.setGameMode(GameMode.ADVENTURE);
        clearContents(player);

        if(game.isWaiting()) {
            final int playersSize = game.getGameData().getPlayers().size();
            final int maxPlayers = game.getConfigController().getInt(ConfigController.Value.MAX_PLAYERS);
            final String joinMessage = String.format("§7%s §ea rejoint la partie ! §a(%d/%d)", player.getName(), playersSize, maxPlayers);

            final PluginDescriptionFile pluginDescriptionFile = instance.getDescription();
            player.sendMessage(String.format("\n§6§l%s\n§7%s", pluginDescriptionFile.getName(), pluginDescriptionFile.getDescription()));

            instance.getServer().broadcastMessage(joinMessage);
            instance.getServer().getOnlinePlayers().forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(joinMessage)));

            game.checkLaunch();
        }

        game.getBoardController().updatePlayerBoard(player);
    }

    public void removePlayer(Player player, boolean spectate) {
        game.getGameData().getPlayers().remove(player);

        if(game.isWaiting()) {
            final int playersSize = game.getGameData().getPlayers().size();
            final int maxPlayers = game.getConfigController().getInt(ConfigController.Value.MAX_PLAYERS);
            final String quitMessage = String.format("§7%s §ea quitté la partie ! §c(%d/%d)", player.getName(), playersSize, maxPlayers);

            instance.getServer().broadcastMessage(quitMessage);
            instance.getServer().getOnlinePlayers().forEach(p -> p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(quitMessage)));
        } else if(game.isPlaying()) {
            game.getGameData().removeGamePlayer(player);

            instance.getServer().broadcastMessage(String.format("§7§l%s§r §7s'est déconnécté en plein jeu.", player.getName()));

            game.checkFinish();

            if(spectate) addSpectator(player);
        }

        game.getBoardController().updatePlayerBoard(player);
    }

    private void addSpectator(Player player) {
        player.teleport(game.getConfigController().getLocation(ConfigController.Value.WAITING_ROOM));
        player.setGameMode(GameMode.SPECTATOR);

        player.sendMessage("§7§oVous êtes désormais §8§oSpectateur§7§o, vous rejoindrez la prochaine partie automatiquement.");
        player.sendTitle("§8§lMODE SPECTATEUR", "", 10, 70, 20);

        game.getGameData().getSpectators().add(player);
        game.getBoardController().updatePlayerBoard(player);
    }

    public boolean isPlayer(Player player) {
        return game.getGameData().getPlayers().contains(player) && !game.getGameData().getSpectators().contains(player);
    }
    public boolean isSpectator(Player player) {
        return !isPlayer(player);
    }

    public void clearContents(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(20); player.setFoodLevel(20);
        player.setLevel(0); player.setExp(0);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        event.setJoinMessage(null);

        if(game.isWaiting()) {
            addPlayer(player);
        } else {
            addSpectator(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        event.setQuitMessage(null);

        removePlayer(player, false);
    }
}
