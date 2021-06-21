package net.kunmc.lab.aging;

import net.kunmc.lab.command.CommandHandler;
import net.kunmc.lab.constants.CommandConst;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
import net.kunmc.lab.listener.PlayerEventHandler;
import net.kunmc.lab.task.AgingTask;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
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
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.text;

public final class Aging extends JavaPlugin {
    public static Aging plugin;
    private BukkitTask task;
    private PlayerEventHandler handler;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        plugin = this;

        CommandHandler commandHandler = new CommandHandler(this);
        getCommand(CommandConst.MAIN_COMMAND).setExecutor(commandHandler);
        //getCommand(CommandConstants.MAIN_COMMAND).setTabCompleter(commandHandler);
        config = getConfig();
    }

    @Override
    public void onDisable() {
        plugin = null;
        config = null;
    }

    /**
     * 老化クラフトの開始処理
     */
    public void startGame() {
        initGame();
        handler = new PlayerEventHandler(this);
        getServer().getPluginManager().registerEvents(handler, this);

        int period = config.getInt(ConfigConst.PERIOD);
        task = new AgingTask(this).runTaskTimer(this, period, period);
    }

    /**
     * 老化クラフトの終了処理
     */
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
        int age = new Random().nextInt(Generation.Type.ELDERLY.getMaxAge());
        setAge(player, age);
        setGeneration(player, Generation.getGeneration(age));
        setIsAging(player, true);
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
            if(false == getIsAging(player)) {
                return;
            }

            //　TODO: 死んでいる場合は老化させない

            int age = addAge(player);

            Generation.Type nowGeneration = getGeneration(player);
            Generation.Type nextGeneration = Generation.getGeneration(age);

            // 世代更新がない場合は次ユーザーへ
            if(nowGeneration.equals(nextGeneration) ) {
                return;
            }

            // 世代更新先がない場合は老衰とする
            if(false == nowGeneration.hasNext()) {
                player.damage(ConfigConst.DAMAGE);
                return;
            }

            addGeneration(player, nowGeneration.nextGeneration);
        });
    }

    /**
     * 年齢加算を行う
     * @param player 年齢加算するプレイヤー
     * @return 加算後の年齢
     */
    public int addAge(Player player) {
        int age = getAge(player) + 1;
        setAge(player, age);

        Component message = LinearComponents.linear(NamedTextColor.RED, text(player.getName() + " " + age + "歳 "));
        player.displayName(message);
        getServer().getLogger().info(player.getName() + " " + age + "歳 " + getGeneration(player) + "世代");

        return age;
    }

    /**
     * 世代更新を行う
     * @param player 世代更新するプレイヤー
     * @param generation 更新先の世代
     */
    public void addGeneration(Player player, Generation.Type generation) {
        try {
            // 世代更新メッセージ
            player.sendMessage(generation.getMessage());

            // 歩行速度
            float walkSpeed = (float) config.getDouble(generation.getPathName() + ConfigConst.WALK_SPEED);
            player.setWalkSpeed(walkSpeed);

            // HP
            double maxHp = config.getDouble(generation.getPathName() + ConfigConst.MAX_HP);
            player.setMaxHealth(maxHp);

            // 空腹
            int foodLevel = config.getInt(generation.getPathName() + ConfigConst.FOOD_LEVEL);
            player.setFoodLevel(foodLevel);

            setGeneration(player, generation);

        } catch(IllegalArgumentException ie) {
            getServer().getLogger().log(Level.WARNING, player + " :歩行速度の引数が範囲外の数値です[(float)-1~1]");
        } catch (Exception e){
            getServer().getLogger().log(Level.WARNING, e.getMessage());
        }
    }

    public void resetAge(Player player) {
        int init_age = config.getInt(ConfigConst.INIT_AGE);

        setAge(player, init_age);
        setGeneration(player, Generation.Type.BABY);
    }

    private void setMetaData(Player player, String key, Object value) {
        player.setMetadata(key, new FixedMetadataValue(this, value));
    }

    private Object getMetaData(Player player, String key) {
        List<MetadataValue> values = player.getMetadata(key);
        for(MetadataValue value : values) {
            // このプラグインで設定したデータのみ返却する
            if(Objects.requireNonNull(value.getOwningPlugin()).getDescription().getName().equals(this.getDescription().getName())) {
                return value.value();
            }
        }
        return null;
    }

    public int getAge(Player player) {
        return (int)getMetaData(player, ConfigConst.METAKEY_AGE);
    }

    public Generation.Type getGeneration(Player player) {
        return (Generation.Type) getMetaData(player, ConfigConst.METAKEY_GENERATION);
    }

    public boolean getIsAging(Player player) {
        return (boolean)getMetaData(player, ConfigConst.METAKEY_IS_AGING);
    }

    public void setAge(Player player, int age) {
        setMetaData(player, ConfigConst.METAKEY_AGE, age);
    }

    public void setGeneration(Player player, Generation.Type generation) {
        setMetaData(player, ConfigConst.METAKEY_GENERATION, generation);
    }

    public void setIsAging(Player player, boolean isAging) {
        setMetaData(player, ConfigConst.METAKEY_IS_AGING, isAging);
    }
}
