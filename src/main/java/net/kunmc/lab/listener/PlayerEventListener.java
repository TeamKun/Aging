package net.kunmc.lab.listener;

import net.kunmc.lab.HiraganaConverter;
import net.kunmc.lab.main.Aging;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
    private HashMap<UUID, Double> fallDistanceMap;

    public PlayerEventListener(Aging plugin) {
        this.plugin = plugin;
        fallDistanceMap = new HashMap<>();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (GameMode.CREATIVE.equals(player.getGameMode())) {
            return;
        }
        if (!Generation.Type.ELDERLY.equals(plugin.getGeneration(player))) {
            return;
        }

        UUID uuid = player.getUniqueId();
        if(!player.isOnGround()) {
            return;
        }

        if (!fallDistanceMap.containsKey(uuid)) {
            fallDistanceMap.put(uuid, e.getTo().getY());
            return;
        }

        // 上下移動が一定数以上は高いところから落下したと判定
        if (2.0 <= fallDistanceMap.get(uuid) - e.getTo().getY()) {
            player.damage(ConfigConst.DAMAGE);
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, ConfigConst.DAMAGE));
        }
        fallDistanceMap.remove(uuid);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (EntityDamageEvent.DamageCause.CUSTOM != e.getEntity().getLastDamageCause().getCause()) {
            return;
        }
        if (!Generation.Type.ELDERLY.equals(plugin.getGeneration(player))) {
            return;
        }

        // 老衰
        if (Generation.Type.ELDERLY.max_age <= plugin.getAge(player)) {
            Component message = LinearComponents.linear(NamedTextColor.WHITE, text(player.getName() + " は老衰で死んでしまった"));
            e.deathMessage(message);
            return;
        }

        // 骨折
        Component message = LinearComponents.linear(NamedTextColor.WHITE, text(player.getName() + " は骨が折れて死んでしまった"));
        e.deathMessage(message);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        plugin.resetAge(player);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();
        if (GameMode.CREATIVE.equals(player.getGameMode())) {
            return;
        }
        int foodLevel = plugin.getPlayerFoodLevel(player);
        if (foodLevel < player.getFoodLevel() + e.getFoodLevel()) {
            e.setFoodLevel(foodLevel);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        Material material = e.getItem().getType();
        if (GameMode.CREATIVE.equals(player.getGameMode())) {
            return;
        }

        // 若返りアイテム
        for (Material rejuvenateItem : plugin.getRejuvenateItems()) {
            if (rejuvenateItem.equals(material)) {
                String message = plugin.rejuvenateAge(player);
                player.sendMessage(message);
                return;
            }
        }

        // 空腹値の上限
        int foodLevel = plugin.getPlayerFoodLevel(player);
        if (foodLevel <= player.getFoodLevel() && ConfigConst.DEF_FOOD_LEVEL != foodLevel) {
            e.setCancelled(true);
            player.sendMessage("老化で満腹まで食べられない！");
            return;
        }

        // 食べられるアイテムに制限がない
        if (plugin.isEatAllItem(player)) {
            return;
        }

        // 食事制限がある場合は食べられるものかチェックする
        for (Material canEatMaterial : plugin.canEatItems(player)) {
            if (canEatMaterial.equals(material)) {
                return;
            }
        }
        player.sendMessage("この食べ物は固くて食べられない！");
        e.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player player = e.getPlayer();
        String message = e.getMessage();

        if (GameMode.CREATIVE.equals(player.getGameMode())) {
            return;
        }

        // 漢字の入力制限
        if (plugin.isNotUseChineseCharacter(player) && hasChineseCharacter(message)) {
            player.sendMessage(ChatColor.RED + "漢字は忘れてしまって発言できない！");
            e.setCancelled(true);
            return;
        }

        // 使用できないひらがなを置換する
        if (plugin.isCheckHiragana(player)) {
            e.setMessage(HiraganaConverter.convertText(message));
            return;
        }

        // 特定の文字しか使用できない場合は文字数分置換する
        if (plugin.hasEndWord(player)) {
            StringBuilder replacedText = new StringBuilder();
            for (int i = 0; i < message.length(); i++) {
                replacedText.append(plugin.getEndWord(player));
            }
            e.setMessage(replacedText.toString());
        }
    }

    private boolean hasChineseCharacter(String text) {
        return text.matches(".*[一-龠].*");
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        plugin.initPlayer(e.getPlayer());
    }

}
