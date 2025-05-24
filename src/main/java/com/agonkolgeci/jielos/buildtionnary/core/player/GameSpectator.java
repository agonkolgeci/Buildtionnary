package com.agonkolgeci.jielos.buildtionnary.core.player;

import com.agonkolgeci.jielos.buildtionnary.core.GameManager;
import com.agonkolgeci.jielos.buildtionnary.core.board.GameBoard;
import lombok.Getter;
import org.bukkit.entity.Player;

public class GameSpectator implements GameUser {

    private final GameManager gameManager;

    @Getter private final Player player;
    @Getter private final GameBoard board;

    public GameSpectator(GameManager gameManager, Player player) {
        this.gameManager = gameManager;
        this.player = player;

        this.board = new GameBoard(gameManager, player);
    }
}
