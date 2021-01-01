package ca.nicbo.minecraftbut.youareachicken;

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
public class YouAreAChicken extends JavaPlugin implements Listener {
    private static final int LEVITATION_AMPLIFIER = 252; // This is the closest I could get to the chicken fall speed

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
        disguiseAsChicken(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrinkMilk(PlayerItemConsumeEvent event) {
        getServer().getScheduler().runTask(this, () -> giveSlowFall(event.getPlayer()));
    }

    @SuppressWarnings("ConstantConditions") // Player will always have max health attribute
    private static void disguiseAsChicken(Player player) {
        MobDisguise chicken = new MobDisguise(DisguiseType.CHICKEN);
        chicken.setNotifyBar(DisguiseConfig.NotifyBar.NONE);
        DisguiseAPI.disguiseToAll(player, chicken);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(4);
        giveSlowFall(player);
    }

    private static void giveSlowFall(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, Integer.MAX_VALUE, LEVITATION_AMPLIFIER, false, false));
    }
}
