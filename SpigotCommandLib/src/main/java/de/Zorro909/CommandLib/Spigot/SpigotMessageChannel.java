package de.Zorro909.CommandLib.Spigot;

import de.Zorro909.CommandAPI.Abstractions.AbstractMessageChannel;
import de.Zorro909.CommandAPI.Abstractions.AbstractUser;
import de.Zorro909.CommandAPI.Abstractions.ChannelType;
import de.Zorro909.ConfigurationLibrary.Messages.Language;
import de.Zorro909.ConfigurationLibrary.Messages.StringReplace;
import de.Zorro909.ConfigurationLibrary.Messages.TranslatableMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpigotMessageChannel extends AbstractMessageChannel {
	CommandSender entity = null;
	boolean global = false;
	String channelIdentifier = "minecraft:";

	public SpigotMessageChannel(CommandSender entity, boolean global) {
		if (global) {
			this.entity = entity;
			this.channelIdentifier = String.valueOf(this.channelIdentifier) + "_global";
			this.global = true;
		} else {
			this.entity = entity;
			this.channelIdentifier = String.valueOf(this.channelIdentifier) + entity.getName();
		}
	}

	public String getChannelIdentifier() {
		return this.channelIdentifier;
	}

	public Object getBackingChannel() {
		return this.entity;
	}

	public ChannelType getChannelType() {
		return this.global ? ChannelType.PUBLIC : ChannelType.PRIVATE;
	}

	public AbstractMessageChannel getPublicMessageChannel() {
		return new SpigotMessageChannel(this.entity, true);
	}

	public AbstractMessageChannel getPrivateMessageChannel() {
		return new SpigotMessageChannel(this.entity, false);
	}

	public boolean sendMessage(String message) {
		if (this.global) {
			this.entity.getServer().broadcastMessage(message);
		} else {
			this.entity.sendMessage(message);
		}
		return true;
	}

	public boolean sendMessage(TranslatableMessage message, Language lang, StringReplace... replaces) {
		String text = message.getText(lang, replaces);
		if (this.global) {
			this.entity.getServer().broadcastMessage(text);
		} else {
			this.entity.sendMessage(text);
		}
		return true;
	}

	public CommandSender getEntity() {
		return this.entity;
	}

	public AbstractUser getOriginatingCaller() {
		if (this.entity instanceof Player) {
			return new SpigotUser((Player) this.entity);
		}
		return null;
	}
}