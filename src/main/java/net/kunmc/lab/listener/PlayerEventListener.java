package net.kunmc.lab.listener;

import net.kunmc.lab.HiraganaConverter;
import net.kunmc.lab.main.Aging;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import java.util.HashMap;
import java.util.UUID;
import static net.kyori.adventure.text.Component.text;

public class PlayerEventListener implements Listener {

    private Aging plugin;
    private HashMap<UUID, Float> fallDistanceMap;

    public PlayerEventListener(Aging plugin) {
        this.plugin = plugin;
        fallDistanceMap = new HashMap<UUID, Float>();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        Player player = e.getPlayer();
        if(false == Generation.Type.ELDERLY.equals(plugin.getGeneration(player))) {
            return;
        }

        UUID uuid = player.getUniqueId();
        float distance = player.getFallDistance();

        if(distance > 0) {
            if(false == fallDistanceMap.containsKey(uuid)) {
                fallDistanceMap.put(uuid, new Float(distance));
                return;
            }

            Float sumDistance = new Float(distance) + fallDistanceMap.get(uuid);
            fallDistanceMap.put(uuid, sumDistance);
            return;
        }

        if(false == fallDistanceMap.containsKey(uuid)) {
            return;
        }

        // 上下移動の累積が一定数以上は1マスより高いところから落下したと判定
        if(4.0 < fallDistanceMap.get(uuid)) {
            player.damage(ConfigConst.DAMAGE);
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, ConfigConst.DAMAGE));
        }
        fallDistanceMap.remove(uuid);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if(EntityDamageEvent.DamageCause.CUSTOM.equals(e.getEntity().getLastDamageCause())) {
            return;
        }

        // 老衰
        if(Generation.Type.ELDERLY.max_age <= plugin.getAge(player)) {
            Component message = LinearComponents.linear(NamedTextColor.WHITE, text(player.getName() + " は老衰で死んでしまった"));
            e.deathMessage(message);
            return;
        }

        // 骨折
        Component message = LinearComponents.linear(NamedTextColor.WHITE, text(player.getName() + " は骨が折れて死んでしまった"));
        e.deathMessage(message);
        return;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        plugin.resetAge(player);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();
        int foodLevel = plugin.getPlayerFoodLevel(player);
        if( foodLevel < player.getFoodLevel() + e.getFoodLevel()) {
            e.setFoodLevel(foodLevel);
            e.setCancelled(true);
            player.sendMessage("老化で満腹まで食べられない！");
        }
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
