package com.agonkolgeci.jielos.buildtionnary.core.board;

import com.agonkolgeci.jielos.buildtionnary.core.GameManager;
import com.agonkolgeci.jielos.buildtionnary.core.GameStatus;
import com.agonkolgeci.jielos.buildtionnary.core.builder.GameBuilder;
import com.agonkolgeci.jielos.buildtionnary.core.player.GamePlayer;
import com.agonkolgeci.jielos.buildtionnary.utils.time.DateUtils;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class GameBoard extends FastBoard {

    public static final int LEADERBOARD_MAX_PLAYERS = 5;

    private final GameManager gameManager;

    public GameBoard(GameManager gameManager, Player player) {
        super(player);

        this.gameManager = gameManager;

        this.updateTitle(gameManager.getConfig().getScoreboardName());
    }

    public void update() {
        @NotNull final List<Component> lines = new ArrayList<>(Collections.singleton(Component.empty()));

        @Nullable final GamePlayer gamePlayer = gameManager.getPlayersManager().getPlayer(this.getPlayer());

        switch (gameManager.getStatus()) {
            case WAITING_FOR_PLAYERS, STARTING -> {
                lines.add(Component.text("Statut", NamedTextColor.GREEN, TextDecoration.BOLD));
                lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("Joueurs:")).appendSpace().append(Component.text(gameManager.getPlayersManager().getPlayers().size(), NamedTextColor.YELLOW)).colorIfAbsent(NamedTextColor.GRAY).build());

                if(gameManager.getStatus() == GameStatus.WAITING_FOR_PLAYERS) {
                    lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("En attente de")).appendSpace().append(Component.text(gameManager.getMissingPlayers() + " joueur(s)", NamedTextColor.YELLOW)).colorIfAbsent(NamedTextColor.GRAY).build());
                } else if(gameManager.getStatus() == GameStatus.STARTING && gameManager.getStartScheduler() != null) {
                    lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("Démarrage dans")).appendSpace().append(Component.text(DateUtils.formatSeconds(gameManager.getStartScheduler().getSeconds()), NamedTextColor.YELLOW)).colorIfAbsent(NamedTextColor.GRAY).build());
                }
            }

            case PLAYING -> {
                lines.add(Component.text("Partie", NamedTextColor.AQUA, TextDecoration.BOLD));

                final GameBuilder builder = gameManager.getBuildersManager().getBuilder();
                if(builder != null) {
                    lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("Constructeur:")).appendSpace().append(Component.text(builder.getGamePlayer().getPlayer().getName(), NamedTextColor.YELLOW)).colorIfAbsent(NamedTextColor.GRAY).build());
                    lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("Temps restant:")).appendSpace().append(Component.text(DateUtils.formatSeconds(builder.getSeconds()), NamedTextColor.YELLOW)).colorIfAbsent(NamedTextColor.GRAY).build());

                    if(gamePlayer == builder.getGamePlayer() || (gamePlayer != null && gamePlayer.hasFound(builder))) {
                        lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("Mot:")).appendSpace().append(Component.text(builder.getWord(), NamedTextColor.GREEN)).colorIfAbsent(NamedTextColor.GRAY).build());
                    } else {
                        lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("Indice:")).appendSpace().append(Component.text(builder.getHint(), NamedTextColor.GREEN)).colorIfAbsent(NamedTextColor.GRAY).build());
                    }
                }

                final List<GamePlayer> players = gameManager.getPlayersManager().getPlayers();
                if(!players.isEmpty()) {
                    lines.add(Component.empty());
                    lines.add(Component.text("Classement", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));

                    final AtomicInteger position = new AtomicInteger(1); // instead of indexOf
                    Stream.concat(players.stream(), Stream.of(gamePlayer)).limit(LEADERBOARD_MAX_PLAYERS).distinct().forEachOrdered(leaderboardPlayer -> {
                        lines.add(Component.text()
                                .append(Component.text("#" + position, NamedTextColor.DARK_GRAY))
                                .appendSpace()
                                .append(Component.text(leaderboardPlayer.getPlayer().getName(), NamedTextColor.WHITE).decoration(TextDecoration.BOLD, leaderboardPlayer == gamePlayer))
                                .append(Component.text(":"))
                                .appendSpace()
                                .append(Component.text(String.format("%d pts", leaderboardPlayer.getPoints()), NamedTextColor.AQUA))
                                .colorIfAbsent(NamedTextColor.GRAY)
                                .build()
                        );

                        position.getAndIncrement();
                    });

                    if(players.size() > LEADERBOARD_MAX_PLAYERS) {
                        lines.add(Component.text("...", NamedTextColor.GRAY, TextDecoration.ITALIC));
                    }
                }
            }

            case FINISH -> {
                lines.add(Component.text("Gagnant(s)", NamedTextColor.AQUA, TextDecoration.BOLD));

                final List<GamePlayer> winners = gameManager.getPlayersManager().getWinners();
                if(!winners.isEmpty()) {
                    winners.forEach(winner -> {
                        lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(winner.getPlayer().displayName()).colorIfAbsent(NamedTextColor.GREEN).build());
                    });
                }

                if(gamePlayer != null) {
                    lines.add(Component.empty());
                    lines.add(Component.text("Vos statistiques", NamedTextColor.YELLOW, TextDecoration.BOLD));
                    lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("Points:")).appendSpace().append(Component.text(gamePlayer.getPoints(), NamedTextColor.YELLOW)).colorIfAbsent(NamedTextColor.GRAY).build());
                    lines.add(Component.text().append(Component.text("•", NamedTextColor.DARK_GRAY)).appendSpace().append(Component.text("Mots trouvés:")).appendSpace().append(Component.text(gamePlayer.getFoundedBuilds().size(), NamedTextColor.GREEN)).colorIfAbsent(NamedTextColor.GRAY).build());
                }
            }
        }

        lines.add(Component.empty());
        lines.add(gameManager.getConfig().getScoreboardFooter());

        this.updateLines(lines);
    }

}
