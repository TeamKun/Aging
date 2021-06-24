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

    // 最高年齢に到達時のダメージ
    public final static double DAMAGE = 20.0d;

    public final static String INIT_AGE = "init_age";
    public final static String REJUVENATE_AGE ="rejuvenate";
    public final static String REJUVENATE_ITEMS = "rejuvenate_item";
    private ConfigConst(){}
}
