package net.kunmc.lab.aging;

import net.kunmc.lab.command.CommandHandler;
import net.kunmc.lab.constants.CommandConst;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
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
        getCommand(CommandConst.MAIN_COMMAND).setExecutor(commandHandler);
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

            setGeneration(player, nowGeneration.nextGeneration);
        });
    }

    public int addAge(Player player) {
        int age = getAge(player) + 1;
        setAge(player, age);

        Component message = LinearComponents.linear(NamedTextColor.RED, text(player.getName() + " " + age + "歳 "));
        player.displayName(message);
        getServer().getLogger().info(player.getName() + " " + age + "歳 " + getGeneration(player) + "世代");

        return age;
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

    public void resetAge(Player player) {
        setAge(player, ConfigConst.INIT_AGE);
        setGeneration(player, Generation.Type.BABY);
    }

}
