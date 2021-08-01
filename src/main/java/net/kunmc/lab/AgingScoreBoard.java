package net.kunmc.lab;

import net.kunmc.lab.constants.Generation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class AgingScoreBoard {
    private final static String OBJECTIVE_NAME = "歳";
    private Scoreboard scoreboard;
    private Team teamBaby;
    private Team teamKids;
    private Team teamYoung;
    private Team teamAdult;
    private Team teamElderly;

    public AgingScoreBoard() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        teamBaby = initTeam(Generation.Type.BABY);
        teamKids = initTeam(Generation.Type.KIDS);
        teamYoung = initTeam(Generation.Type.YOUNG);
        teamAdult = initTeam(Generation.Type.ADULT);
        teamElderly = initTeam(Generation.Type.ELDERLY);

        Objective objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy");
        objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    private Team initTeam(Generation.Type generation) {
        Team team = scoreboard.registerNewTeam(generation.name);
        team.setSuffix(generation.color.toString() + " " + generation.dispName + ChatColor.RESET.toString() );
        team.setAllowFriendlyFire(true);
        team.setCanSeeFriendlyInvisibles(false);
        return team;
    }

    private void destroyTeam() {
        teamBaby.unregister();
        teamKids.unregister();
        teamYoung.unregister();
        teamAdult.unregister();
        teamElderly.unregister();
        teamBaby = null;
        teamKids = null;
        teamYoung = null;
        teamAdult = null;
        teamElderly = null;
    }

    public void remove() {
        destroyTeam();
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        objective.unregister();
    }

    private Team getTeamByGeneration(Generation.Type generation) {
        switch (generation) {
            case BABY:
                return teamBaby;
            case KIDS:
                return teamKids;
            case YOUNG:
                return teamYoung;
            case ADULT:
                return teamAdult;
            case ELDERLY:
                return teamElderly;
            default:
                return null;
        }
    }

    public boolean addTeam(Player player, Generation.Type generation) {
        Team team = getTeamByGeneration(generation);
        if (null == team) {
            return false;
        }
        team.addEntry(player.getName());
        return true;
    }

    public boolean removeTeam(Player player, Generation.Type generation) {
        Team team = getTeamByGeneration(generation);
        if (null == team) {
            return false;
        }
        team.removeEntry(player.getName());
        return true;
    }

    public void setShowPlayer(Player player) {
        player.setScoreboard(scoreboard);
    }

    public void setScore(Player player, int age) {
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        Score score;
        try {
            score = objective.getScore(player);
        } catch(NullPointerException e) {
            return;
        }
        score.setScore(age);

        Generation.Type generation = Generation.getGeneration(age);
        addTeam(player, generation);
    }

}