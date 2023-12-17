package me.hikemc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class aportfel implements CommandExecutor {

    private Database database;

    public aportfel(Database database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Komenda dostępna tylko dla graczy!");
            return true;
        }

        Player player = (Player) sender;
        FileConfiguration config = Main.getInstance().getConfig();
        Player targetPlayer;

        String permisja = config.getString("permissions.adminPermission");
        if (permisja != null && !player.hasPermission(permisja)) {
            String brakupr = config.getString("messages.noPermission");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', brakupr));
            return true;
        }

        if (args.length < 3) {
            String poprawneuzycie = config.getString("messages.poprawneUzycie");
            player.sendMessage(ChatUtils.fix(poprawneuzycie));
            return true;
        }

        if (args[0].equalsIgnoreCase("konsola")) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Poprawne użycie: /aportfel konsola NICK add/remove/set KWOTA");
                return true;
            }

            targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatUtils.fix(config.getString("messages.playerOffline")));
                return true;
            }
        } else {
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                player.sendMessage(ChatUtils.fix(config.getString("messages.playerOffline")));
                return true;
            }
        }

        if (args[2].matches("-?\\d+(\\.\\d+)?")) {
            double kwota = Double.parseDouble(args[2]);
        } else {
            player.sendMessage(ChatColor.RED + "Kwota musi być liczbą zmiennoprzecinkową (double), np. 0.0, 10.2");
            return true;
        }

        double kwota = Double.parseDouble(args[2]);

        Listeners listeners = new Listeners(database);

        if (args[1].equalsIgnoreCase("add")) {
            String configaddadmin = config.getString("messages.adminAddWallet")
                    .replace("{admin}", sender.getName())
                    .replace("{kwota}", String.valueOf(kwota))
                    .replace("{target}", targetPlayer.getName());
            String configaddplayer = config.getString("messages.playerAddWallet")
                    .replace("{admin}", sender.getName())
                    .replace("{kwota}", String.valueOf(kwota))
                    .replace("{target}", targetPlayer.getName());
            try {
                Statystyki statystyki = listeners.getPlayerStatystyki(targetPlayer);
                database.addPlayerMoney(statystyki, args[2], targetPlayer);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', configaddadmin));
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', configaddplayer));
            } catch (SQLException exception) {
                sender.sendMessage(ChatUtils.fix("&cNie udało się zmienić stanu konta gracza " + targetPlayer.getName()));
                exception.printStackTrace();
            }
        } else if (args[1].equalsIgnoreCase("remove")) {
            String configremovepadmin = config.getString("messages.adminRemoveWallet")
                    .replace("{admin}", sender.getName())
                    .replace("{kwota}", String.valueOf(kwota))
                    .replace("{target}", targetPlayer.getName());
            String configremoveplayer = config.getString("messages.playerRemoveWallet")
                    .replace("{admin}", sender.getName())
                    .replace("{kwota}", String.valueOf(kwota))
                    .replace("{target}", targetPlayer.getName());
            try {
                Statystyki statystyki = listeners.getPlayerStatystyki(targetPlayer);
                database.removePlayerMoney(statystyki, args[2], targetPlayer);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', configremovepadmin));
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', configremoveplayer));
            } catch (SQLException exceptions) {
                sender.sendMessage(ChatUtils.fix("&cNie udało się zmienić stanu konta gracza " + targetPlayer.getName()));
                exceptions.printStackTrace();
            }
        } else if (args[1].equalsIgnoreCase("set")) {
            String configsetpadmin = config.getString("messages.adminSetWallet")
                    .replace("{admin}", sender.getName())
                    .replace("{kwota}", String.valueOf(kwota))
                    .replace("{target}", targetPlayer.getName());
            String configsetplayer = config.getString("messages.playerSetWallet")
                    .replace("{admin}", sender.getName())
                    .replace("{kwota}", String.valueOf(kwota))
                    .replace("{target}", targetPlayer.getName());

            try {
                database.setPlayerMoney(args[2], targetPlayer);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', configsetpadmin));
                targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', configsetplayer));
            } catch (SQLException exception) {
                sender.sendMessage(ChatColor.RED + "Nie udało się ustawić nowego stanu konta gracza!");
                exception.printStackTrace();
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Niepoprawna akcja. Użyj: add, remove lub set.");
        }

        return true;
    }
}
