package net.kunmc.lab.aging;

import net.kunmc.lab.task.AgingTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Aging extends JavaPlugin {
    private BukkitTask task;

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void start() {
        task = new AgingTask(this).runTaskTimer(this, 20, 20);
    }

    public void stop() {
        task.cancel();
    }
}
