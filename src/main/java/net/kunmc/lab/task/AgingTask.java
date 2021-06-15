package net.kunmc.lab.task;

import net.kunmc.lab.player.PlayerManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AgingTask extends BukkitRunnable {
    private JavaPlugin plugin;

    public AgingTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            PlayerManager.getInstance().agingPlayer();
            // FIXME: プレイヤーの老化処理はこちらに書いた方が良いかも
        }catch(IllegalArgumentException iae) {
            //TODO: if plugin is null
        }catch(IllegalStateException ise) {
            //TODO: of this was already scheduled
        }
    }
}
