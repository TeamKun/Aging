package net.kunmc.lab.constants;

public final class ConfigConst {
    // metadata 読み出し時のkey
    public final static String METAKEY_AGE = "age";
    public final static String METAKEY_IS_AGING = "is_aging";
    public final static String METAKEY_GENERATION = "generation";

    // 最高年齢に到達時のダメージ
    public final static double DAMAGE = 20.0d;

    // リスポーン時の年齢
    public final static int INIT_AGE = 0;

    private ConfigConst(){}
}
