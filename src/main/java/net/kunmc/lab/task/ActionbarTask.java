package net.kunmc.lab.task;

import net.kunmc.lab.constants.Generation;
import net.kunmc.lab.main.Aging;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ActionbarTask extends BukkitRunnable {
    private Aging plugin;

    public ActionbarTask(Aging plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Generation.Type generation = plugin.getGeneration(player);
                BaseComponent[] component = new ComponentBuilder(generation.dispName).color(generation.md_5Color)
                        .append(" " + plugin.getAge(player) + "歳").color(ChatColor.WHITE).create();
                player.sendActionBar(component);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
