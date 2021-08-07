package net.kunmc.lab.main;

import net.kunmc.lab.AgingScoreBoard;
import net.kunmc.lab.command.AgingCommandExecutor;
import net.kunmc.lab.constants.CommandConst;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
import net.kunmc.lab.listener.PlayerEventListener;
import net.kunmc.lab.task.ActionbarTask;
import net.kunmc.lab.task.AgingTask;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

public final class Aging extends JavaPlugin {
    public static Aging plugin;
    private BukkitTask task;
    private BukkitTask actionBarTask;
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
     *
     * @return boolean true: 開始成功, false: 開始失敗
     */
    public boolean start() {
        if (!resome()) {
            return false;
        }
        scoreboard = new AgingScoreBoard();
        initGame();
        listener = new PlayerEventListener(this);
        getServer().getPluginManager().registerEvents(listener, this);
        return true;
    }

    /**
     * 老化プラグインの終了処理
     *
     * @return boolean true: 終了成功, false: 終了失敗
     */
    public boolean stop() {
        if (!suspend()) {
            return false;
        }
        HandlerList.unregisterAll(listener);
        listener = null;
        scoreboard.remove();
        scoreboard = null;
        endGame();
        return true;
    }

    /**
     * 老化プラグインの年経過停止
     *
     * @return boolean
     */
    public boolean suspend() {
        if (task == null) {
            return false;
        }
        task.cancel();
        task = null;
        if (actionBarTask == null) {
            return false;
        }
        actionBarTask.cancel();
        actionBarTask = null;
        return true;
    }

    /**
     * 老化プラグインの年経過再開
     *
     * @return boolean
     */
    public boolean resome() {
        if (task != null) {
            return false;
        }
        int period = getConfig().getInt(ConfigConst.PERIOD);
        task = new AgingTask(this).runTaskTimer(this, period, period);
        actionBarTask = new ActionbarTask(this).runTaskTimer(this, 0, 10);
        return true;
    }

    public boolean restart() {
        suspend();
        resome();
        Bukkit.getOnlinePlayers().forEach(p->
        {
            removeEffects(p);
            addEffects(p);
        });
        return true;
    }

    /**
     * infoコマンド用の設定値を返す
     */
    public void info(CommandSender sender) {
        sender.sendMessage("1年経過にかかるTrik数: " + getConfig().getInt(ConfigConst.PERIOD));
        sender.sendMessage("リスポーン時の年齢: " + getConfig().getInt(ConfigConst.INIT_AGE));
        sender.sendMessage("若返る年齢: " + getConfig().getInt(ConfigConst.REJUVENATE_AGE));
        sender.sendMessage(setPlayerInfo());
    }

    /**
     * 世代固定プレイヤーの一覧を返す
     * @return 世代プレイヤー一覧
     */
    private BaseComponent[] setPlayerInfo() {
        // 世代固定プレイヤーを取得
        ComponentBuilder component = new ComponentBuilder("世代固定プレイヤー: ");
        if (!isStarted()) {
            component.append("なし").color(ChatColor.WHITE);
            return component.create();
        }

        boolean isSetGenerationPLayer = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Generation.Type generation = getGeneration(player);
            if(getIsAging(player)) {
                continue;
            }
            isSetGenerationPLayer = true;
            component.append(player.getName() + "(" + generation.dispName + ") ").color(generation.md_5Color);
        }
        // 世代固定プレイヤーがいない
        if(!isSetGenerationPLayer) {
            component.append("なし").color(ChatColor.WHITE);
        }
        return component.create();
    }

    /**
     * 老化プラグインが開始済みかチェックする
     *
     * @return boolean
     */
    public boolean isStarted() {
        return task != null;
    }

    /**
     * 老化プラグイン初期化処理
     */
    private void initGame() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            initPlayer(player);
        }
    }

    /**
     * 老化プラグイン終了処理
     */
    private void endGame() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setMaxHealth(ConfigConst.DEF_HP);
            player.setFoodLevel(ConfigConst.DEF_FOOD_LEVEL);
            player.setWalkSpeed(ConfigConst.DEF_WALKSPEED);
        }
    }

    /**
     * プレイヤー初期化処理
     *
     * @param player プレイヤー
     */
    public void initPlayer(Player player) {
        int age = new Random().nextInt(Generation.Type.ELDERLY.max_age);
        Generation.Type generation = Generation.getGeneration(age);
        setAge(player, age);
        setGeneration(player, generation);
        setIsAging(player, true);
        setPlayerGeneration(player, generation);

        scoreboard.setShowPlayer(player);
        scoreboard.addTeam(player, generation);
        scoreboard.setScore(player, age);
    }

    /**
     * オンラインの全プレイヤー老化処理
     */
    public void run() {
        Collection<? extends Player> allPlayer = Bukkit.getOnlinePlayers();
        if (1 > allPlayer.size()) {
            return;
        }

        for (Player o_player : allPlayer) {
            if (GameMode.CREATIVE == o_player.getGameMode()) {
                continue;
            }
            // 年齢固定の場合は老化させない
            if (!getIsAging(o_player)) {
                continue;
            }
            if (o_player.isDead()) {
                continue;
            }
            aging(o_player);
        }
    }

    /**
     * プレイヤーの老化処理を行う
     *
     * @param player 老化するプレイヤー
     */
    public void aging(Player player) {
        int age = getAge(player) + 1;
        if (Generation.Type.ELDERLY.max_age < age) {
            player.damage(ConfigConst.DAMAGE);
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, ConfigConst.DAMAGE));
            return;
        }
        setPlayerAge(player, age);
    }

    /**
     * プレイヤーの年齢と世代を設定する
     *
     * @param player プレイヤー
     * @param age 年齢
     */
    public void setPlayerAge(Player player, int age) {
        Generation.Type generation = Generation.getGeneration(getAge(player));

        setAge(player, age);
        scoreboard.setScore(player, age);

        // 世代更新がある場合
        Generation.Type nextGeneration = Generation.getGeneration(age);
        if (generation.equals(nextGeneration)) {
            return;
        }
        player.sendMessage(nextGeneration.getMessage());
        setPlayerGeneration(player, nextGeneration);
    }

    /**
     * プレイヤーの年齢と世代を強制的に上書きする
     *
     * @param player プレイヤー
     * @param age 年齢
     */
    public void setPlayerAgeForce(Player player, int age) {
        setAge(player, age);
        Generation.Type generation = Generation.getGeneration(age);

        // HP
        double maxHp = getConfig().getDouble(generation.getPathName() + ConfigConst.MAX_HP);
        player.setMaxHealth(maxHp);

        // 空腹
        new BukkitRunnable() {
            @Override
            public void run() {
                int foodLevel = getConfig().getInt(generation.getPathName() + ConfigConst.FOOD_LEVEL);
                player.setFoodLevel(foodLevel);
            }
        }.runTaskLater(this, 0);

        // リジェネ
        new BukkitRunnable() {
            @Override
            public void run() {
                addForceEffects(player);
            }
        }.runTaskLater(this, 1);
    }

    /**
     * 世代更新を行う
     *
     * @param player     世代更新するプレイヤー
     * @param generation 更新先の世代
     */
    public void setPlayerGeneration(Player player, Generation.Type generation) {
        try {
            // 世代更新メッセージ
            setGeneration(player, generation);

            // 歩行速度
            float walkSpeed = (float) getConfig().getDouble(generation.getPathName() + ConfigConst.WALK_SPEED);
            player.setWalkSpeed(walkSpeed);

            // HP
            double maxHp = getConfig().getDouble(generation.getPathName() + ConfigConst.MAX_HP);
            player.setMaxHealth(maxHp);

            // 空腹
            int foodLevel = getConfig().getInt(generation.getPathName() + ConfigConst.FOOD_LEVEL);
            if (foodLevel < player.getFoodLevel()) {
                player.setFoodLevel(foodLevel);
            }

            // リジェネ
            removeEffects(player);
            if (getIsAging(player)) {
                addEffects(player, generation);
            } else {
                addForceEffects(player);
            }
        } catch (IllegalArgumentException ie) {
            getServer().getLogger().log(Level.WARNING, player + " :歩行速度の引数が範囲外の数値です[(float)-1~1]");
        } catch (Exception e) {
            getServer().getLogger().log(Level.WARNING, e.getMessage());
        }
    }

    /**
     * 半永久的なリジェネ効果を取得する
     * @return リジェネ効果
     */
    private Collection<PotionEffect> getEffects() {
        Collection<PotionEffect> effects = new ArrayList<>();
        getEffectTypes().forEach(e -> effects.add(new PotionEffect(e, Integer.MAX_VALUE, ConfigConst.AMPLIFIER)));
        return effects;
    }

    /**
     * プレイヤーにリジェネを付与する(1年経過のtrik数変更用)
     * @param player プレイヤー
     */
    public void addEffects(Player player) {
        boolean isRegeneration = getConfig().getBoolean(getGeneration(player).getPathName() + ConfigConst.REGENERATION);
        if (!isRegeneration) {
            return;
        }
        Collection<PotionEffect> effects = getEffects();
        player.addPotionEffects(effects);
    }

    /**
     * プレイヤーにリジェネを付与する
     * @param player プレイヤー
     * @param generation 世代
     */
    private void addEffects(Player player, Generation.Type generation) {
        boolean isRegeneration = getConfig().getBoolean(generation.getPathName() + ConfigConst.REGENERATION);
        if (!isRegeneration) {
            return;
        }
        Collection<PotionEffect> effects = getEffects();
        player.addPotionEffects(effects);
    }

    /**
     * プレイヤーにリジェネを付与する(世代固定用)
     * @param player プレイヤー
     */
    private void addForceEffects(Player player) {
        boolean isRegeneration = getConfig().getBoolean(getGeneration(player).getPathName() + ConfigConst.REGENERATION);
        if (!isRegeneration) {
            return;
        }
        Collection<PotionEffect> effects = getEffects();
        player.addPotionEffects(effects);
    }

    /**
     * リジェネ効果一覧を取得する
     * @return 効果一覧
     */
    private Collection<PotionEffectType> getEffectTypes() {
        Collection<PotionEffectType> effects = new ArrayList<>();
        effects.add(PotionEffectType.CONDUIT_POWER);
        effects.add(PotionEffectType.DAMAGE_RESISTANCE);
        effects.add(PotionEffectType.CONDUIT_POWER);
        effects.add(PotionEffectType.FAST_DIGGING);
        effects.add(PotionEffectType.FIRE_RESISTANCE);
        effects.add(PotionEffectType.INCREASE_DAMAGE);
        effects.add(PotionEffectType.NIGHT_VISION);
        effects.add(PotionEffectType.SLOW_FALLING);
        effects.add(PotionEffectType.WATER_BREATHING);
        return effects;
    }

    /**
     * 指定プレイヤーの効果を削除する
     * @param player プレイヤー
     */
    public void removeEffects(Player player) {
        Collection<PotionEffectType> effects = getEffectTypes();
        effects.stream()
                .filter(player::hasPotionEffect)
                .forEach(player::removePotionEffect);
    }

    /**
     * リスポーン時の年齢再設定を行う
     *
     * @param player プレイヤー
     */
    public void resetAge(Player player) {
        int init_age = getIsAging(player) ? getConfig().getInt(ConfigConst.INIT_AGE) : getAge(player);
        if (getIsAging(player)) {
            setPlayerAge(player, init_age);
            return;
        }
        setPlayerAgeForce(player, init_age);
    }

    /**
     * 若返り時の年齢再設定を行う
     *
     * @param player プレイヤー
     * @return 若返りメッセージ
     */
    public String rejuvenateAge(Player player) {
        int rejuvenateAge = getConfig().getInt(ConfigConst.REJUVENATE_AGE);
        int age = Math.max(getAge(player) - rejuvenateAge, ConfigConst.AGE_0);
        setPlayerAge(player, age);

        return "昆布を食べたので " + rejuvenateAge + "歳若返った！";
    }

    /**
     * 若返りアイテムの一覧を取得する
     *
     * @return List<Material> 若返りアイテム一覧
     */
    public List<Material> getRejuvenateItems() {
        ArrayList<Material> list = new ArrayList<>();
        getConfig().getStringList(ConfigConst.REJUVENATE_ITEMS).forEach(name -> list.add(Material.getMaterial(name)));
        return list;
    }

    /**
     * 発言時にチャット末尾に追加する文言があるかチェックする
     *
     * @param player プレイヤー
     * @return boolean
     */
    public boolean hasEndWord(Player player) {
        return getConfig().contains(getGeneration(player).getPathName() + ConfigConst.ENDWORD);
    }

    /**
     * 発言時にチャット末尾に追加する文言を取得する
     *
     * @param player プレイヤー
     * @return String 付与文字
     */
    public String getEndWord(Player player) {
        if (hasEndWord(player)) {
            return getConfig().getString(getGeneration(player).getPathName() + ConfigConst.ENDWORD);
        }
        return "";
    }

    /**
     * 発言時に漢字を使えないかチェックする
     *
     * @param player プレイヤー
     * @return boolean true:漢字使用不可, false: 漢字使用可
     */
    public boolean isNotUseChineseCharacter(Player player) {
        return !getConfig().getBoolean(getGeneration(player).getPathName() + ConfigConst.USE_CHINESE_CHARACTER);
    }

    /**
     * 発言時にローマ字をつけるかチェックする
     *
     * @param player プレイヤー
     * @return boolean true:ローマ字使用可能, false:使用不可
     */
    public boolean isNotUseAlphabet(Player player) {
        return !getConfig().getBoolean(getGeneration(player).getPathName() + ConfigConst.USE_ALPHABET);
    }

    /**
     * 発言時にひらがなを「あはまわ行」へ変換するかチェックする
     *
     * @param player プレイヤー
     * @return boolean true:変換する, false:変換しない
     */
    public boolean isCheckHiragana(Player player) {
        return getConfig().getBoolean(getGeneration(player).getPathName() + ConfigConst.CHECK_HIRAGANA);
    }

    /**
     * 指定プレイヤーの世代が食べられるアイテムを取得する
     *
     * @param player プレイヤー
     * @return List<Material> 食事制限がある世代は食べられるアイテム一覧。食事制限がない場合は空配列を返す。
     */
    public List<Material> canEatItems(Player player) {
        Generation.Type generation = getGeneration(player);
        ArrayList<Material> list = new ArrayList<>();

        getConfig().getStringList(generation.getPathName() + ConfigConst.CANEAT).forEach(name -> {
            Material material = Material.getMaterial(name);
            if (null == material) {
                return;
            }
            list.add(material);
        });

        return list;
    }

    /**
     * 食事制限があるかチェックする
     *
     * @param player プレイヤー
     * @return boolean
     */
    public boolean isEatAllItem(Player player) {
        Generation.Type generation = getGeneration(player);
        return getConfig().getStringList(generation.getPathName() + ConfigConst.CANEAT).isEmpty();
    }

    /**
     * プレイヤーの空腹値の上限を取得する
     *
     * @param player プレイヤー
     * @return int 空腹値(0~20)
     */
    public int getPlayerFoodLevel(Player player) {
        Generation.Type generation = getGeneration(player);
        return getConfig().getInt(generation.getPathName() + ConfigConst.FOOD_LEVEL);
    }

    private void setMetaData(Player player, String key, Object value) {
        player.setMetadata(key, new FixedMetadataValue(this, value));
    }

    private Object getMetaData(Player player, String key) {
        List<MetadataValue> values = player.getMetadata(key);
        for (MetadataValue value : values) {
            // このプラグインで設定したデータのみ返却する
            if (Objects.requireNonNull(value.getOwningPlugin()).getDescription().getName().equals(this.getDescription().getName())) {
                return value.value();
            }
        }
        return null;
    }

    public int getAge(Player player) {
        return (int) getMetaData(player, ConfigConst.METAKEY_AGE);
    }

    public Generation.Type getGeneration(Player player) {
        return (Generation.Type) getMetaData(player, ConfigConst.METAKEY_GENERATION);
    }

    public boolean getIsAging(Player player) {
        return (boolean) getMetaData(player, ConfigConst.METAKEY_IS_AGING);
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
