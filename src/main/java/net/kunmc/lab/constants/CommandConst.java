package net.kunmc.lab.constants;

public final class CommandConst {
    public final static String MAIN_COMMAND = "aging";

    public final static String COMMAND_START = "start";
    public final static String COMMAND_STOP = "stop";
    public final static String COMMAND_SUSPEND = "suspend";
    public final static String COMMAND_RESTART = "restart";

    public final static String COMMAND_CONF = "conf";
    public final static String ARGS1_PERIOD = "period";
    public final static String ARGS1_INIT_AGE = "init_age";
    public final static int MIN_INIT_AGE = 0;
    public final static int MAX_INIT_AGE = 99;
    public final static String ARGS1_REJUVENATE_AGE = "rejuvenate_age";
    public final static String ARGS1_REJUVENATE_ITEM = "rejuvenate_item";

    public final static String ARGS2_WALKSPEED = "walkspeed";
    public final static int MIN_WALKSPEED = 10;
    public final static int MAX_WALKSPEED = 60;

    public final static String ARGS2_MAXHP = "maxhp";
    public final static String ARGS2_FOODLEVEL = "foodlevel";
    public final static String ARGS2_USEKANJI = "useKanji";
    public final static String ARGS2_CHECKHIRAGANA = "checkHiragana";
    public final static String ARGS2_CANEAT = "caneat";

    public final static String COMMAND_SET = "set";
    public final static String COMMAND_UNSET = "unset";

    private CommandConst() {}
}
