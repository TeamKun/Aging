package net.kunmc.lab.constants;

public final class CommandConst {
    public final static String MAIN_COMMAND = "aging";

    public final static String COMMAND_START = "start";
    public final static String COMMAND_STOP = "stop";
    public final static String COMMAND_SUSPEND = "suspend";
    public final static String COMMAND_RESOME = "resome";
    public final static String COMMAND_RESTART = "restart";
    public final static String COMMAND_CONF = "conf";
    public final static String COMMAND_SET = "set";
    public final static String COMMAND_UNSET = "unset";

    public final static int MIN_INIT_PERIOD = 10;
    public final static int MAX_INIT_PERIOD = 300;
    public final static int MIN_INIT_AGE = 0;
    public final static int MAX_INIT_AGE = 99;
    public final static int MIN_REJUVENATE_AGE = 0;
    public final static int MAX_REJUVENATE_AGE = 99;

    private CommandConst() {}
}
