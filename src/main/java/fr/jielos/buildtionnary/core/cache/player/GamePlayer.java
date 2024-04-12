package fr.jielos.buildtionnary.core.cache.player;

import fr.jielos.buildtionnary.Buildtionnary;
import fr.jielos.buildtionnary.core.Game;
import fr.jielos.buildtionnary.core.GameComponent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GamePlayer extends GameComponent {

    private final Player player;

    private final List<GamePlayer> foundedWords;
    private boolean hasBuild;
    private int points;
    public GamePlayer(Buildtionnary instance, Game game, Player player) {
        super(instance, game);

        this.player = player;

        this.foundedWords = new ArrayList<>();
        this.hasBuild = false;
        this.points = 0;
    }

    public int getPoints() {
        return points;
    }
    public void addPoints(int value) {
        points += value;

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent( String.format("§7Vous venez de remportez §e%d points§7.", value)));

        for(Player player : game.getGameData().getPlayers()) {
            game.getBoardController().updatePlayerBoard(player);
        }
    }

    public List<GamePlayer> getFoundedWords() {
        return foundedWords;
    }

    public boolean hasBuild() {
        return hasBuild;
    }
    public void setHasBuild(boolean hasBuild) {
        this.hasBuild = hasBuild;
    }

    public boolean hasFound(GamePlayer gamePlayer) {
        return foundedWords.contains(gamePlayer);
    }

    public boolean isPlaying() {
        return game.getGameController().isPlayer(player);
    }

    public Player getPlayer() {
        return player;
    }
}
