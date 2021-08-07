package net.kunmc.lab.command;

import net.kunmc.lab.main.Aging;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AgingCommandExecutor implements CommandExecutor, TabCompleter {

    private Aging plugin;

    public AgingCommandExecutor(Aging plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (!CommandConst.MAIN_COMMAND.equals(command.getName())) {
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "error: コマンドが間違っています \nusage: /aging <" + CommandConst.COMMAND_START + " | " + CommandConst.COMMAND_STOP + " | " + CommandConst.COMMAND_CONF + " | " + CommandConst.COMMAND_RESTART + " | " + CommandConst.COMMAND_SET + " | " + CommandConst.COMMAND_UNSET + " | " + CommandConst.COMMAND_INFO +">");
            return true;
        }

        String message = "";
        switch (args[0]) {
            case CommandConst.COMMAND_START:
                if (!(args.length == 1)) {
                    sender.sendMessage(ChatColor.RED + "error: 引数が間違っています \nusage: /aging " + CommandConst.COMMAND_START);
                    return true;
                }
                if (!this.plugin.start()) {
                    sender.sendMessage(ChatColor.RED + "error: 老化プラグインはすでに起動されています");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "info: 老化プラグインが起動しました");
                break;
            case CommandConst.COMMAND_STOP:
                if (!(args.length == 1)) {
                    sender.sendMessage(ChatColor.RED + "error: 引数が間違っています \nusage: /aging " + CommandConst.COMMAND_STOP);
                    return true;
                }
                if (!this.plugin.stop()) {
                    sender.sendMessage(ChatColor.RED + "error: 老化プラグインはまだ起動していません");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "info: 老化プラグインが停止しました");
                break;
            case CommandConst.COMMAND_SUSPEND:
                if (!(args.length == 1)) {
                    sender.sendMessage(ChatColor.RED + "error: 引数が間違っています \nusage: /aging " + CommandConst.COMMAND_SUSPEND);
                    return true;
                }
                if (!this.plugin.suspend()) {
                    sender.sendMessage(ChatColor.RED + "error: 老化プラグインはまだ起動していません");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "info: 老化プラグイン が一時停止されました");
                break;
            case CommandConst.COMMAND_RESOME:
                if (!(args.length == 1)) {
                    sender.sendMessage(ChatColor.RED + "error: 引数が間違っています \nusage: /aging " + CommandConst.COMMAND_RESOME);
                    return true;
                }
                if (!this.plugin.resome()) {
                    sender.sendMessage(ChatColor.RED + "error: 老化プラグインはすでに起動しています");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "info: 老化プラグインが再開されました");
                break;
            case CommandConst.COMMAND_RESTART:
                if (!(args.length == 1)) {
                    sender.sendMessage(ChatColor.RED + "error: 引数が間違っています \nusage: /aging " + CommandConst.COMMAND_RESTART);
                    return true;
                }
                if (!this.plugin.restart()) {
                    sender.sendMessage(ChatColor.RED + "error: 老化プラグインの再起動に失敗しました \nstop->startを手動で実行してください");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "info: 老化プラグインが再起動しました");
                break;
            case CommandConst.COMMAND_CONF:
                try {
                    message = checkConfArgs(args);
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "error: 設定値の数値変換処理で例外が発生しました");
                }

                if (!message.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + message);
                    break;
                }
                message = setConf(args);
                if (!message.isEmpty()) {
                    sender.sendMessage(ChatColor.GREEN + message);
                }
                break;
            case CommandConst.COMMAND_SET:
                if (!plugin.isStarted()) {
                    sender.sendMessage(ChatColor.RED + "error: 老化プラグインを開始させてから設定してください");
                    return true;
                }
                message = checkSetArgs(args);
                if (!message.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + message);
                    break;
                }
                message = setPlayerGeneration(args);
                sender.sendMessage(ChatColor.GREEN + message);
                break;
            case CommandConst.COMMAND_UNSET:
                message = checkUnsetArgs(args);
                if (!message.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + message);
                    break;
                }
                message = unsetPlayerGeneration(args);
                sender.sendMessage(ChatColor.GREEN + message);
                break;
            case CommandConst.COMMAND_INFO:
                if (!(args.length == 1)) {
                    sender.sendMessage(ChatColor.RED + "error: コマンドが間違っています \nusage: /aging " + CommandConst.COMMAND_INFO);
                    return true;
                }
                plugin.info(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "usage: /aging <" + CommandConst.COMMAND_START + " | " + CommandConst.COMMAND_STOP + " | " + CommandConst.COMMAND_CONF + " | " + CommandConst.COMMAND_RESTART + " | " + CommandConst.COMMAND_SET + " | " + CommandConst.COMMAND_UNSET + " | " + CommandConst.COMMAND_INFO +">");
                break;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
       if (!CommandConst.MAIN_COMMAND.equals(command.getName())) {
            return new ArrayList<>();
        }

        if (1 == args.length) {
            return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                    ? Stream.of(CommandConst.COMMAND_START, CommandConst.COMMAND_STOP, CommandConst.COMMAND_CONF, CommandConst.COMMAND_SET, CommandConst.COMMAND_UNSET, CommandConst.COMMAND_RESTART, CommandConst.COMMAND_INFO)
                    : Stream.of(CommandConst.COMMAND_START, CommandConst.COMMAND_STOP, CommandConst.COMMAND_CONF, CommandConst.COMMAND_SET, CommandConst.COMMAND_UNSET, CommandConst.COMMAND_RESTART, CommandConst.COMMAND_INFO)
            ).filter(e -> e.startsWith(args[0])).collect(Collectors.toList());
        }

        if (2 == args.length) {
            switch (args[0]) {
                case CommandConst.COMMAND_CONF:
                    return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                            ? Stream.of(ConfigConst.PERIOD, ConfigConst.INIT_AGE, ConfigConst.REJUVENATE_AGE)
                            : Stream.of(ConfigConst.PERIOD, ConfigConst.INIT_AGE, ConfigConst.REJUVENATE_AGE)
                    ).filter(e -> e.startsWith(args[1])).collect(Collectors.toList());

                case CommandConst.COMMAND_SET:
                    return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                            ? Stream.of(Generation.Type.BABY.name, Generation.Type.KIDS.name, Generation.Type.YOUNG.name, Generation.Type.ADULT.name, Generation.Type.ELDERLY.name)
                            : Stream.of(Generation.Type.BABY.name, Generation.Type.KIDS.name, Generation.Type.YOUNG.name, Generation.Type.ADULT.name, Generation.Type.ELDERLY.name)
                    ).filter(e -> e.startsWith(args[1])).collect(Collectors.toList());

                case CommandConst.COMMAND_UNSET:
                    return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                            ? getPlayerName()
                            : getPlayerName()
                    ).filter(e -> e.startsWith(args[1])).collect(Collectors.toList());
                default:
                    return new ArrayList<>();
            }
        }

        if (3 == args.length) {
            if (CommandConst.COMMAND_SET.equals(args[0])) {
                if (!isGenerationName(args[1])) {
                    return new ArrayList<>();
                }
                return (sender.hasPermission(CommandConst.MAIN_COMMAND)
                        ? getPlayerName()
                        : getPlayerName()
                ).filter(e -> e.startsWith(args[2])).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private Stream<String> getPlayerName() {
        Collection<Player> list;
        list = (Collection<Player>) Bukkit.getOnlinePlayers();
        ArrayList<String> names = new ArrayList<>();

        for (Player player : list) {
            names.add(player.getName());
        }
        return names.stream();
    }

    /**
     * 引数の文字列がGeneraionのEnumに含まれるかチェックする
     *
     * @param args チェック対象文字列
     * @return Generation.Type[BABY|KIDS|YOUNG|ADULT|ELDERLY].name のいずれかに一致する場合はtrue, それ以外はfalse
     */
    private boolean isGenerationName(String args) {
        return Arrays.stream(Generation.Type.values()).anyMatch(e -> e.name.equals(args));
    }

    /**
     * setコマンドの引数チェック
     *
     * @param args [0]:set, [1]:playerName
     * @return コマンド不正がない場合は空文字, 引数エラーがある場合はエラーメッセージ
     */
    private String checkSetArgs(String[] args) {
        if (!(3 == args.length)) {
            return "error: 引数が正しくありません \nusage: /aging " + CommandConst.COMMAND_SET + " <baby | kids | young | adult | elderly> <playerName>";
        }
        if (!isGenerationName(args[1])) {
            return "error: コマンドが間違っています \nusage: /aging " + CommandConst.COMMAND_SET + " <baby | kids | young | adult | elderly> <playerName>";
        }
        if (getPlayerName().noneMatch(e -> e.equals(args[2]))) {
            return "error: ユーザー名が間違っています";
        }
        return "";
    }

    /**
     * unsetコマンドの引数チェック
     *
     * @param args [0]:unset, [1]:playerName
     * @return コマンド不正がない場合は空文字, 引数エラーがある場合はエラーメッセージ
     */
    private String checkUnsetArgs(String[] args) {
        if (!(2 == args.length)) {
            return "error: 引数が正しくありません \nusage: /aging " + CommandConst.COMMAND_UNSET + "<playerName>";
        }
        if (getPlayerName().noneMatch(e -> e.equals(args[1]))) {
            return "error: ユーザー名が間違っています";
        }
        return "";
    }

    /**
     * confコマンドの引数チェック
     *
     * @param args [0]:conf, [1]:[period|init_age|rejuvenate_age], [2]:[trik|age]
     * @return コマンド不正がない場合は空文字, 引数エラーがある場合はエラーメッセージ
     * @throws NumberFormatException 引数の数値変換による例外
     */
    private String checkConfArgs(String[] args) throws NumberFormatException {
        // コマンドが間違っている
        if (args.length < 2 || !(ConfigConst.PERIOD.equals(args[1]) || ConfigConst.INIT_AGE.equals(args[1]) || ConfigConst.REJUVENATE_AGE.equals(args[1]))) {
            return "error: コマンドが間違っています \nusage: /aging " + CommandConst.COMMAND_CONF + " <" + ConfigConst.PERIOD + " [" +  CommandConst.MIN_INIT_PERIOD + "-" + CommandConst.MAX_INIT_PERIOD + "] | "
                    + ConfigConst.INIT_AGE + " [" + CommandConst.MIN_INIT_AGE + "-" + CommandConst.MAX_INIT_AGE + "] | "
                    + ConfigConst.REJUVENATE_AGE + " [" + CommandConst.MIN_REJUVENATE_AGE + "-" + CommandConst.MAX_REJUVENATE_AGE +"]>";
        }

        if (ConfigConst.PERIOD.equals(args[1])) {
            if (!(3 == args.length)) {
                return "error: 引数の数が正しくありません \nusage: /aging "+ CommandConst.COMMAND_CONF + " " + ConfigConst.PERIOD + " [" + CommandConst.MIN_INIT_PERIOD +  "-" + CommandConst.MAX_INIT_PERIOD + "]";
            }
            if (!args[2].chars().allMatch(Character::isDigit)) {
                return "error: 数値を指定してください \nusage: /aging " + CommandConst.COMMAND_CONF + " " + ConfigConst.PERIOD + " [" + CommandConst.MIN_INIT_PERIOD +  "-" + CommandConst.MAX_INIT_PERIOD + "]";
            }
            int trik = Integer.parseInt(args[2]);
            if (trik < CommandConst.MIN_INIT_PERIOD || CommandConst.MAX_INIT_PERIOD < trik) {
                return "error: trikは" + CommandConst.MIN_INIT_PERIOD + "〜" + CommandConst.MAX_INIT_PERIOD + "の間で設定してください";
            }
        }
        if (ConfigConst.INIT_AGE.equals(args[1])) {
            if (!(3 == args.length)) {
                return "error: 引数の数が正しくありません \nusage: /aging "+ CommandConst.COMMAND_CONF + " " + ConfigConst.INIT_AGE + " [" + CommandConst.MIN_INIT_AGE +  "-" + CommandConst.MAX_INIT_AGE + "]";
            }
            if (!args[2].chars().allMatch(Character::isDigit)) {
                return "error: 数値を指定してください \nusage: /aging " + CommandConst.COMMAND_CONF + " " + ConfigConst.INIT_AGE + " [" + CommandConst.MIN_INIT_AGE +  "-" + CommandConst.MAX_INIT_AGE + "]";
            }
            int initAge = Integer.parseInt(args[2]);
            if (initAge < CommandConst.MIN_INIT_AGE || CommandConst.MAX_INIT_AGE < initAge) {
                return "error: init_ageは" + CommandConst.MIN_INIT_AGE + "〜" + CommandConst.MAX_INIT_AGE + "の間で設定してください";
            }
        }
        if (ConfigConst.REJUVENATE_AGE.equals(args[1])) {
            if (!(3 == args.length)) {
                return "error: 引数の数が正しくありません \nusage: /aging "+ CommandConst.COMMAND_CONF + " " + ConfigConst.REJUVENATE_AGE + " [" + CommandConst.MIN_REJUVENATE_AGE +  "-" + CommandConst.MAX_REJUVENATE_AGE + "]";
            }
            if (!args[2].chars().allMatch(Character::isDigit)) {
                return "error: 数値を指定してください \nusage: /aging " + CommandConst.COMMAND_CONF + " " + ConfigConst.REJUVENATE_AGE + " [" + CommandConst.MIN_REJUVENATE_AGE +  "-" + CommandConst.MAX_REJUVENATE_AGE + "]";
            }
            int initAge = Integer.parseInt(args[2]);
            if (initAge < CommandConst.MIN_REJUVENATE_AGE || CommandConst.MAX_REJUVENATE_AGE < initAge) {
                return "error: rejuvenate_ageは" + CommandConst.MIN_REJUVENATE_AGE + "〜" + CommandConst.MAX_REJUVENATE_AGE + "の間で設定してください";
            }
        }
        return "";
    }

    /**
     * 老化プラグインの各種設定値を変更する<br>
     * 事前にcheckConfArgs()で値を検証してから呼び出すこと
     *
     * @param args [0]:conf, [1]:[period|init_age|rejuvenate_age|rejuvenate_item], [2]:[trik|age|itemName]
     * @return 設定値変更の成功メッセージ
     * @throws NumberFormatException 引数の数値変換による例外
     */
    private String setConf(String[] args) throws NumberFormatException {
        if (ConfigConst.PERIOD.equals(args[1])) {
            int trik = Integer.parseInt(args[2]);
            plugin.setConfig(ConfigConst.PERIOD, trik);
            return "info: 1年経過のtrik数を " + trik + " に変更しました \n起動中の場合はrestartで反映できます ";
        }

        if (ConfigConst.INIT_AGE.equals(args[1])) {
            int init_age = Integer.parseInt(args[2]);
            plugin.setConfig(ConfigConst.INIT_AGE, init_age);
            return "info: リスポーン時の初期年齢を " + init_age + " に変更しました";
        }

        if (ConfigConst.REJUVENATE_AGE.equals(args[1])) {
            int rejuvenate_age = Integer.parseInt(args[2]);
            plugin.setConfig(ConfigConst.REJUVENATE_AGE, rejuvenate_age);
            return "info: 若返る年齢を " + rejuvenate_age + " に変更しました";
        }
        return "";
    }

    /**
     * プレイヤーの世代を固定する
     *
     * @param args [0]:conf, [1]:Generaion.Type.[BABY|KIDS|YOUNG|ADULT|ELDERLY].name, [2]: playerName
     * @return 世代固定成功メッセージ
     */
    private String setPlayerGeneration(String[] args) {
        Generation.Type generation = Generation.Type.valueOf(args[1].toUpperCase(Locale.ROOT));
        Player player = Bukkit.getPlayer(args[2]);

        plugin.setIsAging(player, false);
        plugin.setPlayerAge(player, generation.min_age);
        return "info: " + args[2] + " を " + generation.dispName + " に世代固定しました";
    }

    /**
     * プレイヤーの世代固定を解除する
     *
     * @param args [0]:unset, [1]:playerName
     * @return 世代固定解除のメッセージ
     */
    private String unsetPlayerGeneration(String[] args) {
        Player player = Bukkit.getPlayer(args[1]);
        plugin.setIsAging(player, true);
        return "info: " + args[1] + " の世代固定を解除しました ";
    }

}
