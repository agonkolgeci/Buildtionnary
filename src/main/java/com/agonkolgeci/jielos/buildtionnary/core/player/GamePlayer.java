package com.agonkolgeci.jielos.buildtionnary.core.player;

import com.agonkolgeci.jielos.buildtionnary.core.GameManager;
import com.agonkolgeci.jielos.buildtionnary.core.board.GameBoard;
import com.agonkolgeci.jielos.buildtionnary.core.builder.GameBuilder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GamePlayer implements GameUser {

    private final GameManager gameManager;

    @Getter private final Player player;
    @Getter private final GameBoard board;

    @Getter private final List<GameBuilder> foundedBuilds;
    @Getter private boolean builded;

    @Getter private int points;

    public GamePlayer(GameManager gameManager, Player player) {
        this.gameManager = gameManager;

        this.player = player;
        this.board = new GameBoard(gameManager, player);

        this.foundedBuilds = new ArrayList<>();
        this.builded = false;
        this.points = 0;
    }

    public void addPoints(int value) {
        points += value;

        player.sendActionBar(Component.text("Vous venez de remportez").appendSpace().append(Component.text(value + " points", NamedTextColor.YELLOW)).append(Component.text(".")).colorIfAbsent(NamedTextColor.GRAY));
    }

    public boolean hasFound(GameBuilder builder) {
        return foundedBuilds.contains(builder);
    }

}