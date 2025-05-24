package com.agonkolgeci.jielos.buildtionnary.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

public class PlayerUtils {

    public static final Sound SOUND_SUCCESS = Sound.sound(Key.key("minecraft:block.note_block.pling"), Sound.Source.MASTER, 1F, 2F);
    public static final Sound SOUND_ERROR = Sound.sound(Key.key("minecraft:entity.villager.no"), Sound.Source.MASTER, 1F, 1F);

    public static void clearContents(Player player) {
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0);
    }

}
