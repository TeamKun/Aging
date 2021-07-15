package net.kunmc.lab.listener;

import net.kunmc.lab.aging.Aging;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
import net.kunmc.lab.constants.HiraganaConverter;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import static net.kyori.adventure.text.Component.text;

public class PlayerEventListener implements Listener {

    private Aging plugin;
    public PlayerEventListener(Aging plugin) {
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
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        // TODO: 老化の場合だけカスタムメッセージを表示するように変更

        Component message = LinearComponents.linear(NamedTextColor.WHITE, text(player.getName() + " は老衰で死亡した"));
        e.deathMessage(message);
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        plugin.resetAge(player);
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        Material material = e.getItem().getType();

        // 若返りアイテム
        for(Material rejuvenateItem : plugin.getRejuvenateItems()) {
            if(rejuvenateItem.equals(material)) {
                String message = plugin.rejuvenateAge(player);
                player.sendMessage(message);
                return;
            }
        }

        // 食べられるアイテムに制限がない
        if(plugin.isEatAllItem(player)) {
            return;
        }

        // 食事制限がある場合は食べられるものかチェックする
        for ( Material canEatMaterial : plugin.canEatItems(player)) {
            if(canEatMaterial.equals(material)) {
                return;
            }
        }

        player.sendMessage("この食べ物は固くて食べられない！");
        e.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if(e.isCancelled()) {
            return;
        }
        Player player = e.getPlayer();
        String message = e.getMessage();

        // 漢字の入力制限
        if (plugin.isNotUseChineseCharacter(player) && hasChineseCharacter(message)) {
            player.sendMessage(ChatColor.RED + "漢字は忘れてしまって発言できない！");
            e.setCancelled(true);
            return;
        }

        // 使用できないひらがなを置換する
        if(plugin.isCheckHiragana(player)){
            e.setMessage(HiraganaConverter.convertText(message));
            return;
        }

        // 特定の文字しか使用できない場合は文字数分置換する
        if (plugin.hasEndWord(player)) {
            String replacedText = "";
            for(int i=0; i<message.length(); i++) {
                replacedText = replacedText + plugin.getEndWord(player);
            }
            e.setMessage(replacedText);
            return;
        }
    }

    private boolean hasChineseCharacter(String text) {
        return text.matches(".*[一-龠].*");
    }

}
