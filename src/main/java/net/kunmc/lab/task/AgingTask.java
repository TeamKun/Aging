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
        }catch(IllegalArgumentException iae) {
            //TODO: if plugin is null
        }catch(IllegalStateException ise) {
            //TODO: of this was already scheduled
        }
    }
}
