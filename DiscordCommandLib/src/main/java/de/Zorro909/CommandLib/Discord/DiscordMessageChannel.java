package de.Zorro909.CommandLib.Discord;

import de.Zorro909.CommandAPI.Abstractions.AbstractMessageChannel;
import de.Zorro909.CommandAPI.Abstractions.AbstractUser;
import de.Zorro909.CommandAPI.Abstractions.ChannelType;
import de.Zorro909.ConfigurationLibrary.Messages.Language;
import de.Zorro909.ConfigurationLibrary.Messages.StringReplace;
import de.Zorro909.ConfigurationLibrary.Messages.TranslatableMessage;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class DiscordMessageChannel extends AbstractMessageChannel {
	User user;
	MessageChannel discordChannel;
	String channelIdentifier = "discord:";

	public DiscordMessageChannel(MessageChannel discordChannel, User user) {
		this.discordChannel = discordChannel;
		this.user = user;
		this.channelIdentifier = String.valueOf(this.channelIdentifier) + discordChannel.getId();
	}

	public String getChannelIdentifier() {
		return this.channelIdentifier;
	}

	public Object getBackingChannel() {
		return this.discordChannel;
	}

	public ChannelType getChannelType() {
		return (this.discordChannel instanceof net.dv8tion.jda.api.entities.PrivateChannel) ? ChannelType.PRIVATE
				: ChannelType.PUBLIC;
	}

	public AbstractMessageChannel getPublicMessageChannel() {
		if (getChannelType() == ChannelType.PUBLIC)
			return this;
		return null;
	}

	public AbstractMessageChannel getPrivateMessageChannel() {
		return new DiscordMessageChannel((MessageChannel) this.user.openPrivateChannel().complete(), this.user);
	}

	public boolean sendMessage(String message) {
		this.discordChannel.sendMessage(message).submit();
		return true;
	}

	public boolean sendMessage(TranslatableMessage message, Language lang, StringReplace... replaces) {
		String text = message.getText(lang, replaces);
		return sendMessage(text);
	}

	public AbstractUser getOriginatingCaller() {
		if (getChannelType() == ChannelType.PUBLIC) {
			return new DiscordUser(((TextChannel) this.discordChannel).getGuild().getMember(this.user));
		}
		return new DiscordUser(this.user);
	}
}
