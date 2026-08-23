package io.github.thebusybiscuit.mobcapturer;

import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.mobcapturer.listeners.MobCaptureListener;
import io.github.thebusybiscuit.mobcapturer.listeners.PelletListener;
import io.github.thebusybiscuit.mobcapturer.setup.Registry;
import io.github.thebusybiscuit.mobcapturer.setup.Setup;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;

import net.guizhanss.guizhanlib.minecraft.utils.MinecraftVersionUtil;
import net.guizhanss.minecraft.guizhanlib.updater.GuizhanUpdater;

/**
 * MobCapturer Slimefun addon.
 *
 * @author TheBusyBiscuit
 * @author ybw0014
 */
public class MobCapturer extends JavaPlugin implements SlimefunAddon {

    private static MobCapturer instance;

    private Registry registry;

    @Nonnull
    public static MobCapturer getInstance() {
        return instance;
    }

    private static void setInstance(@Nonnull MobCapturer plugin) {
        instance = plugin;
    }

    @Nonnull
    public static Registry getRegistry() {
        return getInstance().registry;
    }

    @Override
    public void onEnable() {
        setInstance(this);

        if (!getServer().getPluginManager().isPluginEnabled("GuizhanLibPlugin")) {
            getLogger().log(Level.SEVERE, "MobCapturer requires GuizhanLibPlugin to run.");
            getLogger().log(Level.SEVERE, "Download it from: https://50L.cc/gzlib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            MinecraftVersionUtil.isAtLeast(1, 16, 4);
        } catch (NoSuchMethodError e) {
            getLogger().log(Level.SEVERE, "MobCapturer requires the latest GuizhanLibPlugin version.");
            getLogger().log(Level.SEVERE, "Download it from: https://50L.cc/gzlib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Config cfg = new Config(this);
        new Metrics(this, 6672);

        if (cfg.getBoolean("options.auto-update") && getPluginVersion().startsWith("Build")) {
            GuizhanUpdater.start(this, getFile(), "wickidcow", "SF_MobCapturer", "master");
        }

        registry = new Registry(cfg);
        Setup.setup();

        new PelletListener(this);
        new MobCaptureListener(this);
    }

    @Override
    @Nonnull
    public String getBugTrackerURL() {
        return "https://github.com/wickidcow/SF_MobCapturer/issues";
    }

    @Override
    @Nonnull
    public JavaPlugin getJavaPlugin() {
        return this;
    }
}
