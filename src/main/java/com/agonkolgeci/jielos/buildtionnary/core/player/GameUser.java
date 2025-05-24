package com.agonkolgeci.jielos.buildtionnary.core.player;

import com.agonkolgeci.jielos.buildtionnary.core.board.GameBoard;
import org.bukkit.entity.Player;

public interface GameUser {

    Player getPlayer();
    GameBoard getBoard();

}
