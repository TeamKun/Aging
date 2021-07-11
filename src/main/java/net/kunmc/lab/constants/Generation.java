package net.kunmc.lab.constants;

import net.kyori.adventure.text.format.NamedTextColor;

public class Generation {
    public enum Type {
        ELDERLY("elderly", "老人",66, 99, null, NamedTextColor.RED),
        ADULT("adult", "大人",30, 65, ELDERLY, NamedTextColor.GOLD),
        YOUNG("young", "若者",20, 29, ADULT, NamedTextColor.YELLOW),
        KIDS("kids", "未成年",6, 19, YOUNG, NamedTextColor.GREEN),
        BABY("baby", "赤ちゃん",0, 5, KIDS, NamedTextColor.BLUE);

        public final String name;
        public final String dispName;
        public final int min_age;
        public final int max_age;
        public final Type nextGeneration;
        public final NamedTextColor color;

        Type(String name, String dispName, int min_age, int max_age, Type nextGeneration, NamedTextColor color) {
            this.name = name;
            this.dispName = dispName;
            this.min_age = min_age;
            this.max_age = max_age;
            this.nextGeneration = nextGeneration;
            this.color = color;
        }

        public String getPathName() {
            return this.name + "." ;
        }

        public boolean hasNext() {
            return this.nextGeneration == null ? false : true;
        }

        public String getMessage() {
            return "あなたは " + this.dispName + " になりました。";
        }

    }

    /**
     * 引数の年齢がどの世代に該当するか判定する
     * @param age　判定対象の年齢
     * @return Generation.Type 世代
     */
    public static Type getGeneration(int age) {
        if(Type.BABY.max_age >= age) {
            return Type.BABY;
        }
        if(Type.KIDS.max_age >= age) {
            return Type.KIDS;
        }
        if(Type.YOUNG.max_age >= age) {
            return Type.YOUNG;
        }
        if(Type.ADULT.max_age >= age) {
            return Type.ADULT;
        }
        if(Type.ELDERLY.max_age >= age) {
            return Type.ELDERLY;
        }
        return null;
    }
}
