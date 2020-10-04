package de.Zorro909.CommandLib.BungeeCord;

import de.Zorro909.CommandAPI.Abstractions.AbstractMessageChannel;
import de.Zorro909.CommandAPI.Abstractions.AbstractUser;
import de.Zorro909.CommandAPI.Abstractions.ChannelType;
import de.Zorro909.ConfigurationLibrary.Messages.Language;
import de.Zorro909.ConfigurationLibrary.Messages.StringReplace;
import de.Zorro909.ConfigurationLibrary.Messages.TranslatableMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeCordMessageChannel extends AbstractMessageChannel {
	CommandSender entity = null;
	boolean global = false;
	String channelIdentifier = "minecraft:";

	public BungeeCordMessageChannel(CommandSender entity, boolean global) {
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
		return new BungeeCordMessageChannel(this.entity, true);
	}

	public AbstractMessageChannel getPrivateMessageChannel() {
		return new BungeeCordMessageChannel(this.entity, false);
	}

	public boolean sendMessage(String message) {
		if (this.global) {
			ProxyServer.getInstance().broadcast(new TextComponent(message));
		} else {
			this.entity.sendMessage(new TextComponent(message));
		}
		return true;
	}

	public boolean sendMessage(TranslatableMessage message, Language lang, StringReplace... replaces) {
		String text = message.getText(lang, replaces);
		if (this.global) {
			ProxyServer.getInstance().broadcast(new TextComponent(text));
		} else {
			this.entity.sendMessage(new TextComponent(text));
		}
		return true;
	}

	public CommandSender getEntity() {
		return this.entity;
	}

	public AbstractUser getOriginatingCaller() {
		if (this.entity instanceof CommandSender) {
			return new BungeeCordUser((CommandSender) this.entity);
		}
		return null;
	}
}