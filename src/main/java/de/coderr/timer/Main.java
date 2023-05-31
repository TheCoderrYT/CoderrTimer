package de.coderr.timer;

import de.coderr.timer.manager.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public static Main coderrtimer; //instance
    public static de.coderr.core.Main coderrcore;

    public static ChatColor themecolor;
    public static ChatColor fontcolor;
    public static String consoleprefix;
    public static String ingameprefix;

    public static TimerManager timerManager;

    @Override
    public void onEnable() {
        consoleprefix = "[CoderrTimer] ";
        coderrtimer = this;

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("CoderrCore")) {
            coderrcore = (de.coderr.core.Main) Bukkit.getServer().getPluginManager().getPlugin("CoderrCore");

            themecolor = de.coderr.core.Main.themecolor;
            fontcolor = de.coderr.core.Main.fontcolor;

            System.out.println(consoleprefix + "CoderrCore-Einstellungen wurden geladen.");
        } else {
            System.out.println(consoleprefix + "CoderrCore-Plugin wurde nicht geladen.");

            boolean configexist = true;
            if (getConfig().contains("theme.primarycolor")) {
                themecolor = ChatColor.valueOf(getConfig().getString("theme.primarycolor"));
            } else {
                getConfig().set("theme.primarycolor","GOLD");
                configexist = false;
            }
            if (getConfig().contains("theme.fontcolor")) {
                fontcolor = ChatColor.valueOf(getConfig().getString("theme.fontcolor"));
            } else {
                getConfig().set("theme.fontcolor","GRAY");
                configexist = false;
            }
            if (!configexist) {
                saveConfig();
                System.out.println(consoleprefix + "Config wurde erstellt.");
            }
        }

        ingameprefix = ChatColor.DARK_GRAY + "[" + themecolor + "Timer" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

        timerManager = new TimerManager();

        Bukkit.getPluginManager().registerEvents(timerManager,this);
        this.getCommand("timer").setExecutor(timerManager);
        this.getCommand("timer").setTabCompleter(timerManager);
    }

    @Override
    public void onDisable() {
        timerManager.onDisable();
    }
}
