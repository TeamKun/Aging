package net.kunmc.lab.main;

import net.kunmc.lab.AgingScoreBoard;
import net.kunmc.lab.command.AgingCommandExecutor;
import net.kunmc.lab.constants.CommandConst;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
import net.kunmc.lab.listener.PlayerEventListener;
import net.kunmc.lab.task.AgingTask;
import net.kyori.adventure.text.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;
import java.util.logging.Level;
import static net.kyori.adventure.text.Component.text;

public final class Aging extends JavaPlugin {
    public static Aging plugin;
    private BukkitTask task;
    private PlayerEventListener listener;
    private AgingScoreBoard scoreboard;

    @Override
    public void onEnable() {
        plugin = this;

        AgingCommandExecutor commandHandler = new AgingCommandExecutor(this);
        getCommand(CommandConst.MAIN_COMMAND).setExecutor(commandHandler);
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    /**
     * 老化プラグインの開始処理
     * @return boolean true: 開始成功, false: 開始失敗
     */
    public boolean start() {
        if(false == resome()) {
            return false;
        }
        initGame();
        listener = new PlayerEventListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        return true;
    }

    /**
     * 老化プラグインの終了処理
     * @return boolean true: 終了成功, false: 終了失敗
     */
    public boolean stop() {
        if(false == suspend()) {
            return false;
        }
        HandlerList.unregisterAll(listener);
        listener = null;
        scoreboard.remove();
        scoreboard = null;
        return true;
    }

    /**
     * 老化プラグインの年経過停止
     * @return boolean
     */
    public boolean suspend() {
        if(task == null) {
            return false;
        }
        task.cancel();
        task = null;
        return true;
    }

    /**
     * 老化プラグインの年経過再開
     * @return boolean
     */
    public boolean resome() {
        if(task != null) {
            return false;
        }
        int period = getConfig().getInt(ConfigConst.PERIOD);
        task = new AgingTask(this).runTaskTimer(this, period, period);
        return true;
    }

    public boolean restart() {
        return suspend() && resome();
    }

    /**
     * 老化プラグイン初期化処理
     */
    private void initGame() {
        scoreboard = new AgingScoreBoard();
        for(Player player : Bukkit.getOnlinePlayers()) {
            initPlayer(player);
            scoreboard.setShowPlayer(player);
        }
    }

    /**
     * プレイヤー初期化処理
     * @param player
     */
    public void initPlayer(Player player) {
        int age = new Random().nextInt(Generation.Type.ELDERLY.max_age);
        setAge(player, age);
        setGeneration(player, Generation.getGeneration(age));
        setIsAging(player, true);

        scoreboard.setScore(player.getName(), age);
    }

    /**
     * オンラインの全プレイヤー老化処理
     */
    public void run() {
        Collection allPlayer = Bukkit.getOnlinePlayers();
        if(1 > allPlayer.size()) {
            return;
        }

        allPlayer.forEach((o_player) ->{
            Player player = (Player) o_player;

            if(GameMode.CREATIVE == player.getGameMode()) {
                return;
            }

            // 年齢固定の場合は老化させない
            if(false == getIsAging(player)) {
                return;
            }

            if(player.isDead()) {
                return;
            }

            aging(player);
        });
    }

    /**
     * プレイヤーの老化処理を行う
     * @param player 老化するプレイヤー
     * @return
     */
    public void aging(Player player) {
        int age = getAge(player) + 1;
        if(Generation.Type.ELDERLY.max_age < age) {
            player.damage(ConfigConst.DAMAGE);
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, ConfigConst.DAMAGE));
            return;
        }
        setPlayerAge(player, age);

        // 各種表示
        Generation.Type generation = getGeneration(player);
        Component message = LinearComponents.linear(generation.color, text(player.getName() + " " + age + "歳 "));
        player.displayName(message);
        getServer().getLogger().info(player.getName() + " " + age + "歳(" + generation + ")");

    }

    /**
     * プレイヤーの年齢と世代を設定する
     * @param player
     * @param age
     */
    public void setPlayerAge(Player player, int age) {
        Generation.Type generation = Generation.getGeneration(getAge(player));

        setAge(player, age);
        scoreboard.setScore(player.getName(), age);

        // 世代更新がある場合
        Generation.Type nextGeneration = Generation.getGeneration(age);
        if(generation.equals(nextGeneration) ) {
            return;
        }
        setPlayerGeneration(player, nextGeneration);
    }

    /**
     * 世代更新を行う
     * @param player 世代更新するプレイヤー
     * @param generation 更新先の世代
     */
    public void setPlayerGeneration(Player player, Generation.Type generation) {
        try {
            // 世代更新メッセージ
            player.sendMessage(generation.getMessage());
            setGeneration(player, generation);

            // 歩行速度
            float walkSpeed = (float) getConfig().getDouble(generation.getPathName() + ConfigConst.WALK_SPEED);
            player.setWalkSpeed(walkSpeed);

            // HP
            double maxHp = getConfig().getDouble(generation.getPathName() + ConfigConst.MAX_HP);
            player.setMaxHealth(maxHp);

            // 空腹
            int foodLevel = getConfig().getInt(generation.getPathName() + ConfigConst.FOOD_LEVEL);
            player.setFoodLevel(foodLevel);

        } catch(IllegalArgumentException ie) {
            getServer().getLogger().log(Level.WARNING, player + " :歩行速度の引数が範囲外の数値です[(float)-1~1]");
        } catch (Exception e){
            getServer().getLogger().log(Level.WARNING, e.getMessage());
        }
    }

    public void resetAge(Player player) {
        int init_age = getIsAging(player) ? getConfig().getInt(ConfigConst.INIT_AGE) : getAge(player);
        setPlayerAge(player, init_age);
    }

    public String rejuvenateAge(Player player) {
        int rejuvenateAge = getConfig().getInt(ConfigConst.REJUVENATE_AGE);
        int age = getAge(player) - rejuvenateAge >= ConfigConst.AGE_0 ? getAge(player) - rejuvenateAge : ConfigConst.AGE_0 ;
        setPlayerAge(player, age);

        return "昆布を食べたので " + rejuvenateAge + "歳若返った！[現在の年齢: "+ age + "歳]";
    }

    public List<Material> getRejuvenateItems() {
        ArrayList<Material> list = new ArrayList<Material>();

        getConfig().getStringList(ConfigConst.REJUVENATE_ITEMS).forEach(name -> {
            list.add(Material.getMaterial(name));
        });

        return list;
    }

    public boolean hasEndWord(Player player){
        return getConfig().contains(getGeneration(player).getPathName() + ConfigConst.ENDWORD);
    }

    public String getEndWord(Player player) {
        if(hasEndWord(player)) {
            return getConfig().getString(getGeneration(player).getPathName() + ConfigConst.ENDWORD);
        }
        return "";
    }

    public boolean isNotUseChineseCharacter(Player player) {
        return !getConfig().getBoolean(getGeneration(player).getPathName() + ConfigConst.USE_CHINESE_CHARACTER);
    }

    public boolean isCheckHiragana(Player player) {
        return getConfig().getBoolean(getGeneration(player).getPathName() + ConfigConst.CHECK_HIRAGANA);
    }

    public List<Material> canEatItems(Player player) {
        Generation.Type generation = getGeneration(player);
        ArrayList<Material> list = new ArrayList<Material>();

        getConfig().getStringList(generation.getPathName() + ConfigConst.CANEAT).forEach(name -> {
            Material material = Material.getMaterial(name);
            if(null == material) {
                return;
            }
            list.add(material);
        });

        return list;
    }

    public boolean isEatAllItem(Player player) {
        Generation.Type generation = getGeneration(player);
        return getConfig().getStringList(generation.getPathName() + ConfigConst.CANEAT).isEmpty();
    }

    public int getPlayerFoodLevel(Player player) {
        Generation.Type generation = getGeneration(player);
        return getConfig().getInt(generation.getPathName() + ConfigConst.FOOD_LEVEL);
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

    public void setConfig(String key, Object value) {
        getConfig().set(key, value);
        saveConfig();
    }
}
