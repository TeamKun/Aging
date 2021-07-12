package net.kunmc.lab;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class AgingScoreBoard {
    public final static String OBJECTIVE_NAME  = "generation";

    private Scoreboard scoreboard;
    private String title;

    public AgingScoreBoard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective sidebar = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy");
        sidebar.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        title = "世代";
    }

    public void setShowPlayer(Player player) {
        player.setScoreboard(scoreboard);
    }

    public void setScore(String name, int point) {
        Objective obj = scoreboard.getObjective(OBJECTIVE_NAME);
        getScoreItem(obj, name).setScore(point);
    }

    private Score getScoreItem(Objective obj, String name) {
        return obj.getScore(name);
    }

    public void remove() {
        if ( scoreboard.getObjective(DisplaySlot.PLAYER_LIST) != null ) {
            scoreboard.getObjective(DisplaySlot.PLAYER_LIST).unregister();
        }
        scoreboard.clearSlot(DisplaySlot.PLAYER_LIST);
    }

}