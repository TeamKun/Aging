package net.kunmc.lab.listener;

import net.kunmc.lab.aging.Aging;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import static net.kyori.adventure.text.Component.text;

public class PlayerEventHandler implements Listener {

    private Aging plugin;
    public PlayerEventHandler(Aging plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        Player p = e.getPlayer();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
    }
    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Component message = LinearComponents.linear(NamedTextColor.WHITE, text(player.getName() + "は老衰で死亡した"));
        e.deathMessage(message);
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        plugin.resetAge(player);
    }
}
