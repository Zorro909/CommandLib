package de.Zorro909.CommandLib.BungeeCord;

import de.Zorro909.CommandAPI.Abstractions.AbstractMessageChannel;
import de.Zorro909.CommandAPI.Abstractions.AbstractUser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCordUser extends AbstractUser {
	CommandSender user;

	public BungeeCordUser(CommandSender player) {
		this.user = player;
	}

	public String getName() {
		return this.user.getName();
	}

	public AbstractMessageChannel openPrivateMessageChannel() {
		return new BungeeCordMessageChannel(this.user, false);
	}

	public boolean hasPermission(String permission) {
		return this.user.hasPermission(permission);
	}

	public boolean ban(String reason) {
		return false;
	}

	public boolean kick(String reason) {
		if (user instanceof ProxiedPlayer) {
			((ProxiedPlayer) user).disconnect(new TextComponent(reason));
			return true;
		}
		return false;
	}
}