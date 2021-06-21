package net.kunmc.lab.task;

import net.kunmc.lab.aging.Aging;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

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
            plugin.getLogger().log(Level.WARNING, "このスレッドはすでにスケジュール済です");
        }catch(Exception e) {
            plugin.getLogger().log(Level.WARNING, e.getMessage());
        }
    }
}
