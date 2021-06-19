package net.kunmc.lab.aging;

import net.kunmc.lab.command.CommandHandler;
import net.kunmc.lab.constants.CommandConstants;
import net.kunmc.lab.constants.ConfigConstants;
import net.kunmc.lab.listener.PlayerEventHandler;
import net.kunmc.lab.task.AgingTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
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

import static net.kyori.adventure.text.Component.text;

public final class Aging extends JavaPlugin {
    public static Aging plugin;
    private BukkitTask task;
    private PlayerEventHandler handler;

    @Override
    public void onEnable() {
        plugin = this;

        CommandHandler commandHandler = new CommandHandler(this);
        getCommand(CommandConstants.MAIN_COMMAND).setExecutor(commandHandler);
        //getCommand(CommandConstants.MAIN_COMMAND).setTabCompleter(commandHandler);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        plugin = null;
    }

    public void startGame() {
        initGame();
        handler = new PlayerEventHandler(this);
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
            if(!(boolean)getMetaData(player, ConfigConstants.METAKEY_IS_AGING)) {
                return;
            }

            //　TODO: 死んでいる場合は老化させない

            int age = addAge(player);
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
        setMetaData(player, ConfigConstants.METAKEY_AGE, ++age);

        Component message = LinearComponents.linear(NamedTextColor.RED, text(player.getName() + " " + age + "歳 "));
        player.displayName(message);
        getServer().getLogger().info(player.getName() + " " + age + "歳 " + getMetaData(player, ConfigConstants.METAKEY_GENERATION) + "世代");

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
     * @return int 世代, 老人の最大歳を超えた場合はマイナスの値を返す
     */
    public int getGeneration(int age) {
        if(ConfigConstants.MAX_BABY_AGE >= age) {
            return ConfigConstants.BABY;
        }
        if(ConfigConstants.MAX_KIDS_AGE >= age) {
            return ConfigConstants.KIDS;
        }
        if(ConfigConstants.MAX_YOUNG_AGE >= age) {
            return ConfigConstants.YOUNG;
        }
        if(ConfigConstants.MAX_ADULT_AGE >= age) {
            return ConfigConstants.ADULT;
        }
        if(ConfigConstants.MAX_ELDERLY_AGE >= age) {
            return ConfigConstants.ELDERLY;
        }

        return ConfigConstants.DEATH;
    }

    public void updGeneration(Player player, int age){
        setMetaData(player, ConfigConstants.METAKEY_GENERATION, getGeneration(age));

        // TODO: 世代別に異なる設定を追加
    }

    public void resetAge(Player player) {
        setMetaData(player, ConfigConstants.METAKEY_AGE, ConfigConstants.INIT_AGE);
        updGeneration(player, ConfigConstants.INIT_AGE);
    }

}
