package us.ajg0702.queue.spigot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {

	final SpigotMain pl;
	public Commands(SpigotMain pl) {
		this.pl = pl;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if(!pl.hasProxy() && pl.getAConfig().getBoolean("check-proxy-response")) {
			sender.sendMessage(color("&cajQueue must also be installed on the proxy!&7 If it has been installed on the proxy, make sure it loaded correctly and try relogging."));
			return true;
		}

		if(!sender.hasPermission("ajqueue.send")) {
			sender.sendMessage(color("&cYou do not have permission to do this!"));
			return true;
		}
		pl.getLogger().info("Sending " +args[0]+ " to " +args[1]+ "queue");
		Player player = Bukkit.getPlayer(args[0]);
		if(player == null) {
			sender.sendMessage(color("&cCannot find that player!"));
			return true;
		}
		String srvname = args[1];
		pl.sendMessage(player, "queue", srvname);

		return true;
	}

	public String color(String txt) {
		return ChatColor.translateAlternateColorCodes('&', txt);
	}

}
