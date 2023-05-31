package de.coderr.timer.manager;

import de.coderr.timer.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TimerManager implements CommandExecutor, Listener, TabCompleter
{
    public File file;
    public YamlConfiguration configuration;
    private int loop;
    private final String msg = "/timer [start|stop|remove] [Players separated by \",\" | timetyp] [player]";

    public TimerManager() {
        loadConfig();
        if (!file.exists()) {
            try {
                new File(file.getPath().replace("timer.yml","")).mkdirs();
                file.createNewFile();
            } catch (IOException ioException) {
                System.err.println(Main.consoleprefix + "Speicherdatei der Timer kann nicht erstellt werden.");
            }
        }
        loop();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("timer")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length >= 1) {
                    int activated = getActiveTimer(p);

                    try {
                        if (args[0].equalsIgnoreCase("start")) {
                            if (args.length >= 2) {
                                int id = 0;
                                if (args[1].contains(",") || Bukkit.getPlayer(args[1]) != null) {
                                    boolean activeTimer = false;
                                    boolean playerOffline = false;
                                    String[] playernames = args[1].split(",");
                                    Player[] players = new Player[playernames.length];
                                    for (int i=0;i< playernames.length;i++) {
                                        if (Bukkit.getPlayer(playernames[i]) != null) {
                                            players[i] = Bukkit.getPlayer(playernames[i]);
                                            if (getActiveTimer(players[i]) != 0) {
                                                activeTimer = true;
                                            }
                                        } else {
                                            playerOffline = true;
                                        }
                                    }
                                    World world = null;
                                    if (args.length == 3) {
                                        if (Bukkit.getWorld(args[2]) != null) {
                                            world = Bukkit.getWorld(args[2]);
                                        }
                                    }
                                    if (activeTimer) {
                                        p.sendMessage(Main.ingameprefix + ChatColor.RED + "Es läuft bei manchen Spielern noch ein Timer.");
                                    } else if (playerOffline) {
                                        p.sendMessage(Main.ingameprefix + ChatColor.RED + "Es sind nicht alle Spieler offline.");
                                    } else {
                                        if (getTimer(players,p,world) != 0) {
                                            if (configuration.getBoolean(getTimer(players,p,world)+".active")) {
                                                p.sendMessage(Main.ingameprefix + ChatColor.RED + "Der Timer läuft bereits.");
                                            } else {
                                                toggleTimer(getTimer(players,p,world));
                                            }
                                        } else {
                                            createTimer(players,p,world);
                                        }
                                    }
                                } else if (isInt(args[1])) {
                                    id = Integer.parseInt(args[1]);
                                    if (configuration.contains(args[1])) {
                                        if (configuration.getBoolean(id+".active")) {
                                            p.sendMessage(Main.ingameprefix + ChatColor.RED + "Der Timer läuft bereits.");
                                        } else {
                                            boolean activeTimer = false;
                                            boolean playerOffline = false;
                                            for (Player m : getPlayers(id)) {
                                                if (m != null) {
                                                    if (getActiveTimer(m) != 0) {
                                                        activeTimer = true;
                                                        break;
                                                    }
                                                } else {
                                                    playerOffline = true;
                                                    break;
                                                }
                                            }
                                            if (activeTimer) {
                                                p.sendMessage(Main.ingameprefix + ChatColor.RED + "Es läuft bei manchen Spielern noch ein Timer.");
                                            } else if (playerOffline) {
                                                p.sendMessage(Main.ingameprefix + ChatColor.RED + "Es sind nicht alle Spieler offline.");
                                            } else {
                                                toggleTimer(id);
                                            }
                                        }
                                    }
                                } else {
                                    p.sendMessage(Main.ingameprefix + ChatColor.RED + msg);
                                }
                            } else {
                                if (getActiveTimer(p) == 0) {
                                    if (getTimer(new Player[]{p}, p, null) == 0) {
                                        createTimer(new Player[]{p}, p, null);
                                    } else {
                                        configuration.set(getTimer(new Player[]{p}, p,null) + ".active", true);
                                        configuration.save(file);
                                    }
                                } else {
                                    p.sendMessage(Main.ingameprefix + ChatColor.RED + "Es läuft bereits ein Timer.");
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("stop")) {
                            if (args.length == 2) {
                                if (isInt(args[1])) {
                                    int id = Integer.parseInt(args[1]);
                                    if (configuration.getBoolean(id+".active")) {
                                        toggleTimer(id);
                                    } else {
                                        p.sendMessage(Main.ingameprefix + ChatColor.RED + "Der Timer ist bereits gestoppt.");
                                    }
                                } else {
                                    p.sendMessage(Main.ingameprefix + ChatColor.RED + "Der Timer existiert nicht.");
                                }
                            } else {
                                if (activated == 0) {
                                    p.sendMessage(Main.ingameprefix + ChatColor.RED + "Der Timer ist bereits gestoppt.");
                                } else {
                                    toggleTimer(activated);
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("remove")) {
                            if (args.length >= 2) {
                                if (p.isOp()) {
                                    if (isInt(args[1])) {
                                        int id = Integer.parseInt(args[1]);
                                        if (args.length == 3) {
                                            Player targetplayer = Bukkit.getPlayer(args[2]);
                                            if (targetplayer != null) {
                                                if (!configuration.getString(id + ".players").equals(p.getUniqueId().toString() + ",")) {
                                                    configuration.set(id + ".players", configuration.getString(id + ".players").replace(targetplayer.getUniqueId().toString() + ",", ""));
                                                    configuration.save(file);
                                                    targetplayer.sendMessage(Main.ingameprefix + ChatColor.GREEN + "Du wurdest aus dem Gruppentimer entfernt.");
                                                    p.sendMessage(Main.ingameprefix + ChatColor.GREEN + targetplayer.getName() + " wurde aus dem Gruppentimer entfernt.");
                                                } else {
                                                    configuration.set(String.valueOf(id), null);
                                                    configuration.save(file);
                                                    targetplayer.sendMessage(Main.ingameprefix + ChatColor.GREEN + "Dein Timer wurde gelöscht.");
                                                    p.sendMessage(Main.ingameprefix + ChatColor.GREEN + targetplayer.getName() + " wurde der Timer gelöscht.");
                                                }
                                            } else {
                                                p.sendMessage(Main.ingameprefix + ChatColor.RED + "Spieler ist nicht online.");
                                            }
                                        } else {
                                            Player[] players = getPlayers(id);
                                            configuration.set(String.valueOf(id), null);
                                            configuration.save(file);
                                            if (players.length > 1) {
                                                for (Player m : players) {
                                                    if (m != null) {
                                                        m.sendMessage(Main.ingameprefix + ChatColor.GREEN + "Der Gruppentimer wurde gelöscht.");
                                                    }
                                                }
                                                p.sendMessage(Main.ingameprefix + ChatColor.GREEN + "Gruppentimer wurde gelöscht.");
                                            } else {
                                                p.sendMessage(Main.ingameprefix + ChatColor.GREEN + "Dein Timer wurde gelöscht.");
                                            }
                                        }
                                    } else {
                                        p.sendMessage(Main.ingameprefix + ChatColor.RED + "Timer existiert nicht.");
                                    }
                                } else {
                                    p.sendMessage(Main.ingameprefix + ChatColor.RED + "Du hast nicht die Berechtigung um Timer von anderen Spielern zu löschen.");
                                }
                            } else {
                                if (getTimer(new Player[]{p},p,null) != 0) {
                                    int timer = getTimer(new Player[]{p},p,null);
                                    configuration.set(String.valueOf(timer), null);
                                    configuration.save(file);
                                    p.sendMessage(Main.ingameprefix + ChatColor.GREEN + "Dein Timer wurde gelöscht.");
                                } else {
                                    p.sendMessage(Main.ingameprefix + ChatColor.RED + "Bei dir läuft kein Timer.");
                                }
                            }
                        }
                    } catch (IOException ioException) {
                        p.sendMessage(Main.ingameprefix + ChatColor.RED + "Beim ausführen des Commands ist ein Fehler entstanden.");
                        p.sendMessage(Main.ingameprefix + ChatColor.RED + "Versuche es mit einer anderen Eingabe noch einmal.");
                    }
                } else {
                    p.sendMessage(Main.ingameprefix + Main.themecolor + "--------- Timer ---------");
                    if (configuration.getKeys(false).size() == 0) {
                        p.sendMessage(Main.ingameprefix + Main.themecolor + "" + ChatColor.ITALIC + "Keine Timer vorhanden");
                    }
                    for (String id : configuration.getKeys(false)) {
                        StringBuilder string = new StringBuilder();
                        string.append(Main.themecolor);
                        string.append(id + ": ");
                        if (Bukkit.getPlayer(UUID.fromString(configuration.getString(id+".creator"))) != null) {
                            string.append(Bukkit.getPlayer(UUID.fromString(configuration.getString(id+".creator"))).getName() + " ");
                        } else {
                            string.append(ChatColor.ITALIC + "offline" + " ");
                        }
                        StringBuilder playerStringBuilder = new StringBuilder();
                        for (Player a : getPlayers(Integer.parseInt(id))) {
                            if (a == null) {
                                playerStringBuilder.append(ChatColor.ITALIC+"offline"+Main.themecolor+",");
                            } else {
                                playerStringBuilder.append(a.getName()+",");
                            }
                        }
                        String playerString = playerStringBuilder.toString();
                        string.append(playerString.substring(0,playerString.length() - 1) + " ");
                        if (configuration.getString(id+".world") != null) {
                            if (Bukkit.getWorld(UUID.fromString(configuration.getString(id+".world"))) != null) {
                                string.append(Bukkit.getWorld(UUID.fromString(configuration.getString(id+".world"))).getName() + " ");
                            } else {
                                string.append("ungeladen ");
                            }
                        }
                        if (configuration.getBoolean(id+".active")) {
                            p.sendMessage(Main.ingameprefix + Main.themecolor + "" + ChatColor.BOLD + string.toString().replace(" ",ChatColor.BOLD+" "));
                        } else {
                            p.sendMessage(Main.ingameprefix + Main.themecolor + "" + string.toString());
                        }
                    }
                }
            } else {
                sender.sendMessage(Main.ingameprefix + ChatColor.RED+"Nur Spieler können diesen Command ausführen.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("start");
            list.add("stop");
            list.add("remove");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("stop")) {
                list.add("Spielername,Spielername,...");
                list.addAll(configuration.getKeys(false));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("remove")) {
                for (Player a : Bukkit.getOnlinePlayers()) {
                    list.add(a.getName());
                }
            } else if (args[0].equalsIgnoreCase("start")) {
                for (World w : Bukkit.getWorlds()) {
                    if (!w.getName().contains("_nether") && !w.getName().contains("_the_end")) {
                        list.add(w.getName());
                    }
                }
            }
        }

        return list;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        if (getActiveTimer(p) != 0) {
            for (int id : getTimers(p)) {
                if (configuration.getString(id+".world") != null) {
                    for (int activeIds : getTimers(p)) {
                        if (configuration.getBoolean(activeIds +".active")) {
                            if (id != activeIds) {
                                if (Bukkit.getWorld(UUID.fromString(configuration.getString(id + ".world"))) != null) {
                                    if (Bukkit.getWorld(UUID.fromString(configuration.getString(id + ".world"))).getName().equals(event.getPlayer().getWorld().getName().replace("_nether", "").replace("_the_end", ""))) {
                                        toggleTimer(activeIds);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void createTimer(Player[] players, Player creator, World world) {
        try {
            int id = 1;
            while (configuration.contains(String.valueOf(id))) {
                id++;
            }
            configuration.set(id+".active",true);
            configuration.set(id+".creator",creator.getUniqueId().toString());
            StringBuilder playersString = new StringBuilder();
            for (Player p : players) {
                playersString.append(p.getUniqueId()).append(",");
            }
            configuration.set(id+".players", playersString.toString());
            if (world != null) {
                configuration.set(id+".world",world.getUID().toString());
            }
            configuration.set(id+".hours",0);
            configuration.set(id+".minutes",0);
            configuration.set(id+".seconds",0);
            configuration.save(file);
        } catch (Exception e) {
            System.err.println(Main.consoleprefix + "Timer konnte nicht erstellt werden");
        }
    }

    public boolean toggleTimer(int id) {
        if (configuration.contains(String.valueOf(id))) {
            try {
                if (configuration.getBoolean(id+".active")) {
                    configuration.set(id + ".active", false);
                } else {
                    configuration.set(id + ".active", true);
                }
                configuration.save(file);
                return true;
            } catch (IOException ignored) {}
        }
        return false;
    }

    private int[] getTimers(Player p) {
        List<Integer> array = new ArrayList<>();
        for (String id : configuration.getKeys(false)) {
            if (configuration.getString(id+".players").contains(p.getUniqueId().toString())) {
                array.add(Integer.parseInt(id));
            }
        }
        if (array.size() > 0) {
            int[] intarray = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                intarray[i] = array.get(i);
            }
            return intarray;
        } else {
            return new int[] {0};
        }
    }
    private boolean hasTimer(Player p) {
        return getTimers(p)[0] != 0;
    }
    public int getActiveTimer(Player p) {
        int activeid = 0;
        if (hasTimer(p)) {
            int[] id = getTimers(p);
            for (int tempid : id) {
                if (configuration.getBoolean(tempid + ".active")) {
                    if (configuration.getString(tempid + ".world") != null) {
                        if (Bukkit.getWorld(UUID.fromString(configuration.getString(tempid + ".world"))) != null) {
                            if (p.getWorld().getName().replace("_nether", "").replace("_the_end", "").equals(Bukkit.getWorld(UUID.fromString(configuration.getString(tempid + ".world"))).getName())) {
                                activeid = tempid;
                                break;
                            }
                        }
                    } else {
                        activeid = tempid;
                        break;
                    }
                }
            }
        }
        return activeid;
    }
    public int getTimer(Player[] players, Player creator, World world) {
        if (hasTimer(creator)) {
            StringBuilder playersString = new StringBuilder();
            for (Player p : players) {
                playersString.append(p.getUniqueId()).append(",");
            }
            for (int id : getTimers(creator)) {
                if (configuration.getString(id + ".players").equals(playersString.toString())) {
                    if (configuration.getString(id+".world") != null) {
                        if (Bukkit.getWorld(UUID.fromString(configuration.getString(id+".world"))) != null) {
                            if (Bukkit.getWorld(UUID.fromString(configuration.getString(id+".world"))) == world) {
                                return id;
                            }
                        }
                    } else {
                        return id;
                    }
                }
            }
        }
        return 0;
    }
    /*
    private int getTimer(Player[] players,Player creator) {
        if (hasTimer(creator)) {
            StringBuilder playersString = new StringBuilder();
            for (Player p : players) {
                playersString.append(p.getUniqueId()).append(",");
            }
            for (int id : getTimers(creator)) {
                if (configuration.getString(id + ".players").equals(playersString.toString())) {
                    return id;
                }
            }
        }
        return 0;
    }
    private int getTimer(Player[] players) {
        StringBuilder playersString = new StringBuilder();
        for (Player p : players) {
            playersString.append(p.getUniqueId()).append(",");
        }
        for (String id : configuration.getKeys(false)) {
            if (configuration.getString(id + ".players").equals(playersString.toString())) {
                return Integer.parseInt(id);
            }
        }
        return 0;
    }
    */
    private Player[] getPlayers(int id) {
        String[] playersuuids = configuration.getString(id+".players").split(",");
        Player[] players = new Player[playersuuids.length];
        for (int i=0;i< players.length;i++) {
            players[i] = Bukkit.getPlayer(UUID.fromString(playersuuids[i]));
        }
        return players;
    }
    private boolean isPlayerOnline(int id) {
        String[] playersuuids = configuration.getString(id+".players").split(",");
        Player[] players = new Player[playersuuids.length];
        boolean playerOnline = false;
        for (int i=0;i< players.length;i++) {
            players[i] = Bukkit.getPlayer(UUID.fromString(playersuuids[i]));
            if (players[i].isOnline()) {
                playerOnline = true;
            }
        }
        return playerOnline;
    }

    private void loop() {
        loop = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.coderrtimer, new TimerTask() {
            @Override
            public void run() {
                for (String id : configuration.getKeys(false)) {
                    if (configuration.getBoolean(id+".active")) {
                        String[] playersuuids = configuration.getString(id+".players").split(",");
                        Player[] players = new Player[playersuuids.length];
                        boolean playerOnline = false;
                        for (int i=0;i< players.length;i++) {
                            players[i] = Bukkit.getPlayer(UUID.fromString(playersuuids[i]));
                            if (players[i] != null) {
                                playerOnline = true;
                            }
                        }
                        if (playerOnline) {
                            boolean playerInWorld = true;
                            if (configuration.getString(id+".world") != null) {
                                playerInWorld = false;
                                World w = Bukkit.getWorld(Bukkit.getWorld(UUID.fromString(configuration.getString(id+".world"))).getName().replace("_nether","").replace("_the_end",""));
                                World nether = Bukkit.getWorld(w.getName()+"_nether");
                                World the_end = Bukkit.getWorld(w.getName()+"_the_end");
                                for (Player p : players) {
                                    if (p.getWorld() == w || p.getWorld() == nether || p.getWorld() == the_end) {
                                        playerInWorld = true;
                                        break;
                                    }
                                }
                            }
                            if (playerInWorld) {
                                try {
                                    int seconds = configuration.getInt(id + ".seconds");
                                    if (seconds == 59) {
                                        int minutes = configuration.getInt(id + ".minutes");
                                        configuration.set(id + ".seconds", 0);
                                        if (minutes == 59) {
                                            configuration.set(id + ".hours", configuration.getInt(id + ".hours"));
                                            configuration.set(id + ".minutes", 0);
                                        } else {
                                            configuration.set(id + ".minutes", minutes + 1);
                                        }
                                    } else {
                                        configuration.set(id + ".seconds", seconds + 1);
                                    }
                                    configuration.save(file);


                                    int hour = configuration.getInt(id + ".hours");
                                    String hourString = "";
                                    if (hour > 9) {
                                        hourString = String.valueOf(hour);
                                    } else {
                                        hourString = "0" + hour;
                                    }

                                    int minute = configuration.getInt(id + ".minutes");
                                    String minuteString = "";
                                    if (minute > 9) {
                                        minuteString = String.valueOf(minute);
                                    } else {
                                        minuteString = "0" + minute;
                                    }

                                    int second = configuration.getInt(id + ".seconds");
                                    String secondString = "";
                                    if (second > 9) {
                                        secondString = String.valueOf(second);
                                    } else {
                                        secondString = "0" + second;
                                    }

                                    for (Player player : players) {
                                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Main.themecolor + hourString + ":" + minuteString + ":" + secondString));
                                    }

                                } catch (IOException ioException) {
                                    System.err.println(Main.consoleprefix + "Timer konnte nicht weitergezählt werden");
                                }
                            }
                        }
                    }
                }
            }
        }, 0,20);
    }

    private void loadConfig() {
        file = new File("plugins//CoderrTimer//timer.yml");
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTask(loop);
    }

    private boolean isInt(String string) {
        try {
            int i = Integer.parseInt(string);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

}
