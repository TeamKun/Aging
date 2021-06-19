package net.kunmc.lab.task;

import net.kunmc.lab.aging.Aging;
import org.bukkit.scheduler.BukkitRunnable;

public class AgingTask extends BukkitRunnable {
    private Aging plugin;

    public AgingTask(Aging plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            plugin.aging();
        }catch(IllegalStateException ise) {
            plugin.getLogger().info("this task was already scheduled.");
        }catch(Exception e) {
            plugin.getLogger().info("task catched exception.");
        }
    }
}
