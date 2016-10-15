package me.mcmainiac.gmc;

import com.google.common.collect.ImmutableMap;
import me.mcmainiac.gmc.excpetions.GameModeNotFoundException;
import me.mcmainiac.gmc.excpetions.PlayerNotFoundException;
import me.mcmainiac.gmc.helpers.Commands;
import me.mcmainiac.gmc.helpers.Config;
import me.mcmainiac.gmc.helpers.MetricsLite;
import me.mcmainiac.gmc.tasks.UpdaterTask;
import me.mcmainiac.gmc.utils.MessageColor;
import me.mcmainiac.gmc.utils.MessageFormat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.security.InvalidParameterException;

/**
 * <h1>GameModeControl V1.3.9</h1><br>
 *
 * <p>Helps you and your admins to control
 * game modes faster and more accurate
 * than ever before.</p>
 *
 * {@link} <a href="http://bit.ly/MC-GMC">bit.ly/MC-GMC</a>
 * @author MCMainiac
 */
public class Main extends JavaPlugin {
	public static final String pre = "\u00A77[GMC] \u00A7r";
	public static Config config;

	private static final CommandSender console = Bukkit.getConsoleSender();

	//--------------
	// Plugin commands
	//--------------
	@Override
	public void onEnable() {
		Main.config = new Config(this);
		Bukkit.getPluginManager().registerEvents(Commands.getInstance(), this);

		Commands.setPlugin(this);
		Commands.resetPlayers();

		// Auto-Updater
		checkForUpdates(config.getBoolean("options.auto-update"));

		// plugin metrics (mcstats.org)
		if (config.getBoolean("options.mcstats")) {
			try {
				MetricsLite metrics = new MetricsLite(this);
				if (!metrics.start())
					log("Failed to start plugin metrics", MessageColor.ERROR);
			} catch (IOException e) {
				log("Failed to enable plugin metrics!", MessageColor.ERROR);
			}
		}
	}

	@Override
	public void onDisable() {
		Commands.resetPlayers();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		try {
			switch (commandLabel.toLowerCase()) {
			case "gamemode":
			case "gm":
				return (args.length > 0 && Commands.Gamemode(sender, new String[]{args[0], (args.length > 1 ? args[1] : null), "gamemode"}));
			case "gm0":
			case "survival":
				return Commands.Gamemode(sender, new String[]{"0", (args.length > 0 ? args[0] : null), "survival"});
			case "gm1":
			case "creative":
				return Commands.Gamemode(sender, new String[]{"1", (args.length > 0 ? args[0] : null), "creative"});
			case "gm2":
			case "adventure":
				return Commands.Gamemode(sender, new String[]{"2", (args.length > 0 ? args[0] : null), "adventure"});
			case "gm3":
			case "spectator":
				return Commands.Gamemode(sender, new String[]{"3", (args.length > 0 ? args[0] : null), "spectator"});

			case "gmonce":
				return Commands.OneTimeGamemode(sender, args);
			case "gmtemp":
				return Commands.TemporaryGamemode(sender, args);

			case "gmh": return Commands.Help(sender, args);
			case "gmi": Commands.Info(sender); return true;
			case "gmr": Commands.Reload(sender); return true;
			default: return false;
			}
		} catch (InvalidParameterException ipe) {
			log("An invalid parameter was given!", MessageColor.ERROR);
			log("Please check your config!", MessageColor.ERROR);
		} catch (GameModeNotFoundException gme) {
			log("The specified GameMode was not found!", MessageColor.ERROR);
			gme.printStackTrace();
		}
		return true;
	}

	//--------------
	// Utilities
	//--------------
	public void checkForUpdates(boolean update) {
		UpdaterTask ut = new UpdaterTask(this, this.getFile(), update);
		Bukkit.getScheduler().runTaskAsynchronously(this, ut);
	}

	public static Player getPlayerByName(String name) throws PlayerNotFoundException {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().equalsIgnoreCase(name)) return p;
		}
		throw new PlayerNotFoundException("Player not found: '" + name + "'");
	}

	public static void log(String log) {
		log(log, MessageColor.INFO);
	}

	public static void log(String log, MessageColor color) {
		switch (color) {
		case BLACK:	console.sendMessage(pre + "\u00A70" + log); break;
		case DARK_BLUE: console.sendMessage(pre + "\u00A71" + log); break;
		case DARK_GREEN: console.sendMessage(pre + "\u00A72" + log); break;
		case DARK_AQUA: console.sendMessage(pre + "\u00A73" + log); break;
		case DARK_RED: console.sendMessage(pre + "\u00A74" + log); break;
		case DARK_PURPLE: console.sendMessage(pre + "\u00A75" + log); break;
		case GOLD:
			case GM_SURVIVAL:
				console.sendMessage(pre + "\u00A76" + log); break;
		case GRAY:
			default:
				console.sendMessage(pre + "\u00A77" + log); break;
		case DARK_GRAY: console.sendMessage(pre + "\u00A78" + log); break;
		case BLUE:
			case GM_ADVENTURE:
				console.sendMessage(pre + "\u00A79" + log); break;
		case GREEN:
			case SUCCESS:
			case GM_SPECTATOR:
				console.sendMessage(pre + "\u00A7a" + log); break;
		case AQUA:
			case GM_CREATIVE:
				console.sendMessage(pre + "\u00A7b" + log); break;
		case RED:
			case ERROR:
			console.sendMessage(pre + "\u00A7c" + log); break;
		case LIGHT_PURPLE: console.sendMessage(pre + "\u00A7d" + log); break;
		case YELLOW:
			case WARNING:
			console.sendMessage(pre + "\u00A7e" + log); break;
		case WHITE: console.sendMessage(pre + "\u00A7f" + log); break;
		}
	}

	public static void send(CommandSender cs, String message) {
		message = replaceColorCodes(message);
		if (cs instanceof Player) cs.sendMessage(message);
		else log(message);
	}

	public static void send(CommandSender cs, String message, ImmutableMap<String, String> context) {
		message = replaceColorCodes(message);

		for (String target : context.keySet()) {
			if (message.contains(target)) {
				String first = message.substring(0, message.indexOf(target));
				String last = message.substring(message.indexOf(target) + target.length(), message.length());
				message = first + context.get(target) + last;
			}
		}

		if (cs instanceof Player) cs.sendMessage(message);
		else log(message);
	}

	private static String replaceColorCodes(String message) {
		message = message.replaceAll("&([0-9a-f])", "\u00A7$1");
		message = message.replaceAll("&([k-o])", "\u00A7$1");
		message = message.replaceAll("&r", MessageFormat.RESET.toString());
		return message;
	}
}
