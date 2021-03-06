package ca.nicbo.minecraftbut.everyoneisachicken;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Nicbo
 */
public class EveryoneIsAChicken extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        for (Player player : getServer().getOnlinePlayers()) {
            disguiseAsChicken(player);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            DisguiseAPI.undisguiseToAll(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        disguiseAsChicken(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        getServer().getScheduler().runTask(this, () -> giveSlowFall(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrinkMilk(PlayerItemConsumeEvent event) {
        getServer().getScheduler().runTask(this, () -> giveSlowFall(event.getPlayer()));
    }

    @SuppressWarnings("ConstantConditions") // Player will always have max health attribute
    private static void disguiseAsChicken(Player player) {
        MobDisguise chicken = new MobDisguise(DisguiseType.CHICKEN);
        chicken.setNotifyBar(DisguiseConfig.NotifyBar.NONE);
        chicken.setKeepDisguiseOnPlayerDeath(true);
        chicken.getWatcher().setCustomName(player.getName());
        chicken.getWatcher().setCustomNameVisible(true);
        DisguiseAPI.disguiseToAll(player, chicken);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(4);
        giveSlowFall(player);
    }

    // Unfortunately can't use levitation here, causes issues with opening containers
    // Until I find a better solution, slow falling will have to do
    private static void giveSlowFall(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0, false, false));
    }
}
