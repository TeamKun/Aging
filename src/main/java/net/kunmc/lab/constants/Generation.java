package net.kunmc.lab.constants;

import org.bukkit.ChatColor;

public class Generation {
    public enum Type {
        ELDERLY("elderly", "老人", 66, 99, null, ChatColor.RED, net.md_5.bungee.api.ChatColor.RED),
        ADULT("adult", "大人", 30, 65, ELDERLY, ChatColor.GOLD, net.md_5.bungee.api.ChatColor.GOLD),
        YOUNG("young", "若者", 20, 29, ADULT, ChatColor.YELLOW, net.md_5.bungee.api.ChatColor.YELLOW),
        KIDS("kids", "未成年", 6, 19, YOUNG, ChatColor.GREEN, net.md_5.bungee.api.ChatColor.GREEN),
        BABY("baby", "赤ちゃん", 0, 5, KIDS, ChatColor.AQUA, net.md_5.bungee.api.ChatColor.AQUA);

        public final String name;
        public final String dispName;
        public final int min_age;
        public final int max_age;
        public final Type nextGeneration;
        public final ChatColor color;
        public final net.md_5.bungee.api.ChatColor md_5Color;

        Type(String name, String dispName, int min_age, int max_age, Type nextGeneration, ChatColor color, net.md_5.bungee.api.ChatColor md_5Color) {
            this.name = name;
            this.dispName = dispName;
            this.min_age = min_age;
            this.max_age = max_age;
            this.nextGeneration = nextGeneration;
            this.color = color;
            this.md_5Color = md_5Color;
        }

        public String getPathName() {
            return this.name + ".";
        }

        public String getMessage() {
            return "あなたは " + this.dispName + " になりました";
        }

        public int getRange() {
            return max_age - min_age + 1;
        }
    }

    /**
     * 引数の年齢がどの世代に該当するか判定する
     *
     * @param age 　判定対象の年齢
     * @return Generation.Type 世代
     */
    public static Type getGeneration(int age) {
        if (Type.BABY.max_age >= age) {
            return Type.BABY;
        }
        if (Type.KIDS.max_age >= age) {
            return Type.KIDS;
        }
        if (Type.YOUNG.max_age >= age) {
            return Type.YOUNG;
        }
        if (Type.ADULT.max_age >= age) {
            return Type.ADULT;
        }
        if (Type.ELDERLY.max_age >= age) {
            return Type.ELDERLY;
        }
        return null;
    }
}
