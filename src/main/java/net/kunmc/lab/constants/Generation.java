package net.kunmc.lab.constants;
public class Generation {
    public enum Type {
        ELDERLY("elderly", "老人",66, 99, null),
        ADULT("adult", "大人",30, 65, ELDERLY),
        YOUNG("young", "若者",20, 29, ADULT),
        KIDS("kids", "未成年",6, 19, YOUNG),
        BABY("baby", "赤ちゃん",0, 5, KIDS);

        public final String name;
        public final String dispName;
        public final int min_age;
        public final int max_age;
        public final Type nextGeneration;

        Type(String name, String dispName, int min_age, int max_age, Type nextGeneration) {
            this.name = name;
            this.dispName = name;
            this.min_age = min_age;
            this.max_age = max_age;
            this.nextGeneration = nextGeneration;
        }

        public String getName() {
            return this.name;
        }

        public String getPathName() {
            return this.name + "." ;
        }

        public int getMinAge() {
            return this.min_age;
        }

        public int getMaxAge() {
            return this.max_age;
        }

        public Type getNext() {
            return this.nextGeneration;
        }

        public boolean hasNext() {
            return this.nextGeneration == null ? false : true;
        }

        public String getMessage() {
            return "あなたは " + this.dispName + " になりました！";
        }

    }

    /**
     * 引数の年齢がどの世代に該当するか判定する
     * @param age　判定対象の年齢
     * @return Generation.Type 世代
     */
    public static Type getGeneration(int age) {
        if(Type.BABY.getMaxAge() >= age) {
            return Type.BABY;
        }
        if(Type.KIDS.getMaxAge() >= age) {
            return Type.KIDS;
        }
        if(Type.YOUNG.getMaxAge() >= age) {
            return Type.YOUNG;
        }
        if(Type.ADULT.getMaxAge() >= age) {
            return Type.ADULT;
        }
        if(Type.ELDERLY.getMaxAge() >= age) {
            return Type.ELDERLY;
        }
        return null;
    }
}