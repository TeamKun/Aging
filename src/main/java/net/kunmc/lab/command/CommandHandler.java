package net.kunmc.lab.command;

import net.kunmc.lab.aging.Aging;
import net.kunmc.lab.constants.CommandConst;
import net.kunmc.lab.constants.ConfigConst;
import net.kunmc.lab.constants.Generation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private Aging plugin;
    public CommandHandler(Aging plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            return true;
        }

        if(!CommandConst.MAIN_COMMAND.equals(command.getName())) {
            return true;
        }

        if(1 != args.length) {
            sender.sendMessage(ChatColor.RED + "usage: ¥naging <" + CommandConst.COMMAND_START + "|" + CommandConst.COMMAND_STOP + ">" );
        }

        if(null == this.plugin) {
            return true;
        }

        switch (args[0]) {
            case CommandConst.COMMAND_START:
                this.plugin.startGame();
                sender.sendMessage(ChatColor.GREEN + "info: 老化プラグイン が有効化されました");
                break;
            case CommandConst.COMMAND_STOP:
                this.plugin.stopGame();
                sender.sendMessage(ChatColor.GREEN + "info: 老化プラグイン が無効化されました");
                break;
            case CommandConst.COMMAND_SUSPEND:
                this.plugin.suspend();
                sender.sendMessage(ChatColor.GREEN + "info: 老化プラグイン が一時停止されました");
                break;
            case CommandConst.COMMAND_RESTART:
                this.plugin.restart();
                sender.sendMessage(ChatColor.GREEN + "info: 老化プラグイン が再開されました");
                break;
            case CommandConst.COMMAND_CONF:
                onConf(args);
                break;
            case CommandConst.COMMAND_SET:
                break;
            case CommandConst.COMMAND_UNSET:
                break;
            default:
                sender.sendMessage(ChatColor.RED + "usage: \n/aging <" + CommandConst.COMMAND_START + " | " + CommandConst.COMMAND_STOP + ">");
                break;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(!CommandConst.MAIN_COMMAND.equals(command.getName())) {
            return new ArrayList<>();
        }

        if(1 == args.length) {
            return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                ? Stream.of(CommandConst.COMMAND_START, CommandConst.COMMAND_STOP, CommandConst.COMMAND_CONF, CommandConst.COMMAND_SET, CommandConst.COMMAND_UNSET)
                : Stream.of(CommandConst.COMMAND_START, CommandConst.COMMAND_STOP, CommandConst.COMMAND_CONF, CommandConst.COMMAND_SET, CommandConst.COMMAND_UNSET)
            ).filter(e -> e.startsWith(args[0])).collect(Collectors.toList());
        }
        if(2 == args.length) {
            switch(args[0]){
                case CommandConst.COMMAND_CONF:
                    return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                            ? Stream.of(CommandConst.ARGS1_PERIOD, Generation.Type.BABY.name, Generation.Type.KIDS.name, Generation.Type.YOUNG.name, Generation.Type.ADULT.name, Generation.Type.ELDERLY.name)
                            : Stream.of(CommandConst.ARGS1_PERIOD, Generation.Type.BABY.name, Generation.Type.KIDS.name, Generation.Type.YOUNG.name, Generation.Type.ADULT.name, Generation.Type.ELDERLY.name)
                    ).filter(e -> e.startsWith(args[1])).collect(Collectors.toList());

                case CommandConst.COMMAND_SET:

                case CommandConst.COMMAND_UNSET:
                    return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                            ? getPlayerName()
                            : getPlayerName()
                    ).filter(e -> e.startsWith(args[1])).collect(Collectors.toList());
                default:
                    return new ArrayList<>();
            }
        }

        if(3 == args.length) {
            if(isGenerationName(args[1])) {
                return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                        ? Stream.of(CommandConst.ARGS2_WALKSPEED, CommandConst.ARGS2_MAXHP, CommandConst.ARGS2_FOODLEVEL)
                        : Stream.of(CommandConst.ARGS2_WALKSPEED, CommandConst.ARGS2_MAXHP, CommandConst.ARGS2_FOODLEVEL)
                ).filter(e -> e.startsWith(args[2])).collect(Collectors.toList());
            }

            if(isPlayerName(args[1])) {
                if(CommandConst.COMMAND_UNSET.equals(args[1])) {
                    return new ArrayList<>();
                }

                return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                        ? Stream.of(Generation.Type.BABY.name, Generation.Type.KIDS.name, Generation.Type.YOUNG.name, Generation.Type.ADULT.name, Generation.Type.ELDERLY.name)
                        : Stream.of(Generation.Type.BABY.name, Generation.Type.KIDS.name, Generation.Type.YOUNG.name, Generation.Type.ADULT.name, Generation.Type.ELDERLY.name)
                ).filter(e -> e.startsWith(args[2])).collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    private Stream<String> getPlayerName() {
        Collection<Player> list = (Collection<Player>) Bukkit.getOnlinePlayers();
        ArrayList<String> names = new ArrayList<>();

        for(Player player : list) {
            names.add(player.getName());
        }
        return names.stream();
    }

    private boolean isGenerationName(String args){
        return Arrays.stream(Generation.Type.values()).anyMatch(e->e.name.equals(args));
    }

    private boolean isPlayerName(String args) {
        return getPlayerName().anyMatch(e -> e.equals(args));
    }

    public String onConf(String[] args) {
        // 1年経過するのに必要なtrik数を変更する
        if(CommandConst.ARGS1_PERIOD.equals(args[1])) {
            if( !(4 == args.length) ) {
                return "引数が足りません";
            }

            int trik = Integer.parseInt(args[3]);
            if(trik < CommandConst.MIN_WALKSPEED || CommandConst.MAX_WALKSPEED < trik) {
                return "trikは" + CommandConst.MIN_WALKSPEED + "~" + CommandConst.MAX_WALKSPEED + "の間で設定してください";
            }

            plugin.setConfig(ConfigConst.PERIOD, trik);
            return "1年経過するのに必要なtrik数を" + trik + "に変更しました";
        }

        if( !Generation.Type.BABY.name.equals(args[1])
        && !Generation.Type.KIDS.name.equals(args[1])
        && !Generation.Type.YOUNG.name.equals(args[1])
        && !Generation.Type.ADULT.name.equals(args[1])
        && !Generation.Type.ELDERLY.name.equals(args[1]) ) {
            return "世代指定が間違っています";
        }

        // 歩行速度、最大HP、空腹値の最大値を変更する
        switch (args[2]) {
            case CommandConst.ARGS2_WALKSPEED:
                // TODO: 例外処理の追加
                int walkSpeed = Integer.parseInt(args[3]);
                if(walkSpeed < -1 || 1 < walkSpeed) {
                    return "歩行速度は-1.0~1.0の間で設定してください";
                }
                plugin.setConfig(args[1] + "." + ConfigConst.WALK_SPEED, walkSpeed);
                break;
            case CommandConst.ARGS2_MAXHP:
                int maxHp = Integer.parseInt(args[3]);
                if(maxHp < 1 || 10 < maxHp) {
                    return "最大HPは1~10の間で設定してください";
                }
                plugin.setConfig(args[1] + "." + ConfigConst.MAX_HP, maxHp);
                break;
            case CommandConst.ARGS2_FOODLEVEL:
                int foodLevel = Integer.parseInt(args[3]);
                if(foodLevel < 1 || 10 < foodLevel) {
                    return "空腹度は1~10の間で設定してください";
                }
                plugin.setConfig(args[1] + "." + ConfigConst.FOOD_LEVEL, foodLevel);
                break;
        }
        return "";
    }

}
