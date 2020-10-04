package de.Zorro909.CommandLib.Discord;

import de.Zorro909.CommandAPI.CommandAPI;
import net.dv8tion.jda.api.JDA;

public class DiscordCommandLib {

	public static void registerDiscordCommandAPI(String packageName, String commandPrefix, JDA jda) {
		CommandAPI.registerCommandAPI(
				packageName, "discord", jda,
				(cla, annot) -> new DiscordCommandHandler(cla, annot, commandPrefix, jda));
	}
}
