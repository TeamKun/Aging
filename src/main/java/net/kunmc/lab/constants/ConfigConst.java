package net.kunmc.lab.constants;

public final class ConfigConst {
    // metadata 読み出し時のkey
    public final static String METAKEY_AGE = "age";
    public final static String METAKEY_IS_AGING = "is_aging";
    public final static String METAKEY_GENERATION = "generation";

    // config.yml読み出し時のkey
    public final static String PERIOD = "period";
    public final static String WALK_SPEED = "walkspeed";
    public final static String MAX_HP = "maxhp";
    public final static String FOOD_LEVEL = "foodlevel";
    public final static String CANEAT = "caneat";
    public final static String ENDWORD = "endword";
    public final static String USE_CHINESE_CHARACTER = "useKanji";
    public final static String CHECK_HIRAGANA = "checkHiragana";

    public final static String INIT_AGE = "init_age";
    public final static String REJUVENATE_AGE ="rejuvenate_age";
    public final static String REJUVENATE_ITEMS = "rejuvenate_item";

    // 最高年齢に到達時のダメージ
    public final static double DAMAGE = 20.0d;

    // 最低年齢
    public final static int AGE_0 = 0;

    // デフォルト
    public final static int DEF_FOOD_LEVEL = 20;

    private ConfigConst(){}
}
