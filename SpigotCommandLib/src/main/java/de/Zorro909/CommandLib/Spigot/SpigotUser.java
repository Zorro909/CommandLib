package de.Zorro909.CommandLib.Spigot;

import de.Zorro909.CommandAPI.Abstractions.AbstractMessageChannel;
import de.Zorro909.CommandAPI.Abstractions.AbstractUser;

import org.bukkit.Bukkit;
import org.bukkit.BanList.Type;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpigotUser extends AbstractUser {
	Player user;

	public SpigotUser(Player player) {
		this.user = player;
	}

	public String getName() {
		return this.user.getName();
	}

	public AbstractMessageChannel openPrivateMessageChannel() {
		return new SpigotMessageChannel((CommandSender) this.user, false);
	}

	public boolean hasPermission(String permission) {
		return this.user.hasPermission(permission);
	}

	public boolean ban(String reason) {
		Bukkit.getBanList(Type.NAME).addBan(user.getName(), reason, null, null);
		kick(reason);
		return true;
	}

	public boolean kick(String reason) {
		this.user.kickPlayer(reason);
		return true;
	}
}