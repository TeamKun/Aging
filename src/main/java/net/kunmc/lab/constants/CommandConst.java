package net.kunmc.lab.constants;

public final class CommandConst {
    public final static String MAIN_COMMAND = "aging";

    public final static String COMMAND_START = "start";
    public final static String COMMAND_STOP = "stop";

    public final static String COMMAND_CONF = "conf";
    public final static String ARGS1_PERIOD = "period";

    public final static String ARGS2_WALKSPEED = "walkspeed";
    public final static int MIN_WALKSPEED = 10;
    public final static int MAX_WALKSPEED = 60;

    public final static String ARGS2_MAXHP = "maxhp";
    public final static String ARGS2_FOODLEVEL = "foodlevel";

    public final static String COMMAND_SET = "set";
    public final static String COMMAND_UNSET = "unset";

    private CommandConst() {}
}
