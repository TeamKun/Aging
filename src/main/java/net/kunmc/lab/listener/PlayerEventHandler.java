package net.kunmc.lab.listener;

import net.kunmc.lab.aging.Aging;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;

public class PlayerEventHandler implements Listener {

    private Aging plugin;
    public PlayerEventHandler(Aging plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        Player p = e.getPlayer();
        if(false == Generation.Type.ELDERLY.equals(plugin.getGeneration(p))){
            return;
        }
        if(false == p.isOnline()) {
            return;
        }
        if( !(e.getFrom().getY() - e.getTo().getY() > 1) ) {
            return;
        }

        // 2ブロック以上の段差から落ちたら死亡
        EntityDamageEvent ede = new EntityDamageEvent(p, EntityDamageEvent.DamageCause.FALL, ConfigConst.DAMAGE);
        Bukkit.getPluginManager().callEvent(ede);
        if(ede.isCancelled()) {
            return;
        }

        ede.getEntity().setLastDamageCause(ede);
        p.damage(ConfigConst.DAMAGE);
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
        ItemStack stack = e.getItem();

        // 乾いた昆布を食べた時は若返る
        if(Material.DRIED_KELP.equals(stack.getType()) ) {
            plugin.rejuvenateAge(e.getPlayer());
        }
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
