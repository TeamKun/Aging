package net.kunmc.lab.constants;

public final class ConfigConstants {
    // metadata 読み出し時のkey
    public final static String METAKEY_AGE = "age";
    public final static String METAKEY_IS_AGING = "is_aging";
    public final static String METAKEY_GENERATION = "generation";

    // 最高年齢に到達時のダメージ
    public final static double DAMAGE = 20.0d;

    // 各世代の最高年齢
    public final static int MAX_BABY_AGE = 5;
    public final static int MAX_KIDS_AGE = 19;
    public final static int MAX_YOUNG_AGE = 29;
    public final static int MAX_ADULT_AGE = 65;
    public final static int MAX_ELDERLY_AGE = 99;

    // 世代
    public final static int BABY = 0;
    public final static int KIDS = 1;
    public final static int YOUNG = 2;
    public final static int ADULT = 3;
    public final static int ELDERLY = 4;
    public final static int DEATH = -1;

    // リスポーン時の年齢
    public final static int INIT_AGE = 0;

    private ConfigConstants(){}
}
