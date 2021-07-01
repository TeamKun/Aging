package net.kunmc.lab.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kunmc.lab.aging.Aging;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Consumer;

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

        // 特定のアイテムを食べた時は若返る
        if(plugin.canRejuvenateItems().equals(stack.getType()) ) {
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

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent e) {
        Player player = e.getPlayer();
        if(false == Generation.Type.ELDERLY.equals(plugin.getGeneration(player))) {
            return;
        }

        ItemStack stack  = e.getItem();
        Material material = stack.getType();

        // 食べられるアイテムの場合は何もせず終了
        for ( Material canEatMaterial : plugin.canEatItems(player)) {
            if(canEatMaterial.equals(material)) {
                return;
            }
        }

        player.sendMessage(stack.displayName() + "は固くて食べられない！");
        e.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        new BukkitRunnable() {

            @Override
            public void run() {
                String message = e.getMessage();
                // 漢字の入力制限
                if (plugin.isNotUseChineseCharacter(player) && hasChineseCharacter(message)) {
                    player.sendMessage("漢字は忘れてしまって発言できない！");
                    e.setCancelled(true);
                }
                // 使用できないひらがなを置換する
                if(plugin.isCheckHiragana(player)){
                    String replaceMessage = getReplaceHiragana(message);
                    Bukkit.broadcastMessage(ChatColor.WHITE + replaceMessage);
                }

                // 文末に言葉を追加
                if (plugin.hasEndWord(player)) {
                    Bukkit.broadcastMessage(ChatColor.WHITE + message + plugin.getEndWord(player));
                }
            }

            public boolean hasChineseCharacter(String text) {
                return text.matches(".*[一-龠].*");
            }

            public String getReplaceHiragana(String text) {
                // TODO: か行・は行...の置換
                return text;
            }
        }.runTask(plugin);
    }
}
