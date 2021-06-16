package net.kunmc.lab.aging;

import net.kunmc.lab.constants.ConfigConstants;
import net.kunmc.lab.listener.PlayerEventHandler;
import net.kunmc.lab.task.AgingTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Aging extends JavaPlugin {
    private BukkitTask task;
    private PlayerEventHandler handler;

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void startGame() {
        initGame();
        handler = new PlayerEventHandler();
        getServer().getPluginManager().registerEvents(handler, this);
        task = new AgingTask(this).runTaskTimer(this, 20, 20);
    }

    public void stopGame() {
        task.cancel();
        HandlerList.unregisterAll(handler);
        handler = null;
    }

    private void initGame() {
        Collection allPlayer = Bukkit.getOnlinePlayers();
        allPlayer.forEach((o_player) ->{
            Player player = (Player)o_player;
            initPlayer(player);
        });
    }

    public void initPlayer(Player player) {
        int age = new Random().nextInt(ConfigConstants.MAX_ELDERLY_AGE);
        setMetaData(player, ConfigConstants.METAKEY_AGE, age);
        setMetaData(player, ConfigConstants.METAKEY_GENERATION, getGeneration(age));
        setMetaData(player, ConfigConstants.METAKEY_IS_AGING, true);
    }

    /**
     * ログインユーザーの老化処理を行う
     */
    public void aging() {
        Collection allPlayer = Bukkit.getOnlinePlayers();
        if(1 > allPlayer.size()) {
            return;
        }

        allPlayer.forEach((o_player) ->{
            Player player = (Player) o_player;

            // 年齢固定の場合は老化させない
            if((boolean)getMetaData(player, ConfigConstants.METAKEY_IS_AGING)) {
                return;
            }

            int age = addAge(player);
            updPlayerName(player);

            int generation = getGeneration(age);

            // 老衰
            if(0 > generation) {
                player.damage(ConfigConstants.DAMAGE);
                return;
            }

            // 世代更新がない場合は次ユーザーへ
            if(generation == (int)getMetaData(player, ConfigConstants.METAKEY_GENERATION)) {
                return;
            }

            updGeneration(player, age);
        });
    }

    public int addAge(Player player) {
        int age = (int)getMetaData(player, ConfigConstants.METAKEY_AGE);
        setMetaData(player, ConfigConstants.METAKEY_AGE, age++);

        return age;
    }

    public void setMetaData(Player player, String key, Object value) {
        player.setMetadata(key, new FixedMetadataValue(this, value));
    }

    public Object getMetaData(Player player, String key) {
        List<MetadataValue> values = player.getMetadata(key);
        for(MetadataValue value : values) {
            // このプラグインで設定したデータのみ返却する
            if(Objects.requireNonNull(value.getOwningPlugin()).getDescription().getName().equals(this.getDescription().getName())) {
                return value.value();
            }
        }
        return null;
    }

    /**
     * 現在の年齢がどの世代に該当するか判定する
     * @return int 世代(0-4), 老人の最大歳を超えた場合は-1を返す
     */
    public int getGeneration(int age) {
        if(ConfigConstants.MAX_BABY_AGE >= age) {
            return 0;
        }
        if(ConfigConstants.MAX_KIDS_AGE >= age) {
            return 1;
        }
        if(ConfigConstants.MAX_YOUNG_AGE >= age) {
            return 2;
        }
        if(ConfigConstants.MAX_ADULT_AGE >= age) {
            return 3;
        }
        if(ConfigConstants.MAX_ELDERLY_AGE >= age) {
            return 4;
        }

        return -1;
    }

    public void updGeneration(Player player, int age){
        setMetaData(player, ConfigConstants.METAKEY_GENERATION, getGeneration(age));

        // TODO: 世代別に異なる設定を追加
    }

    public void updPlayerName(Player player) {
        // TODO: 表示名の変更処理
    }
}
