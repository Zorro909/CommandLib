
package de.Zorro909.CommandAPI.Abstractions;

import de.Zorro909.ConfigurationLibrary.Messages.Language;
import de.Zorro909.ConfigurationLibrary.Messages.StringReplace;
import de.Zorro909.ConfigurationLibrary.Messages.TranslatableMessage;

public abstract class AbstractMessageChannel {
	public abstract String getChannelIdentifier();

	public abstract Object getBackingChannel();

	public abstract ChannelType getChannelType();

	public abstract AbstractMessageChannel getPublicMessageChannel();

	public abstract AbstractMessageChannel getPrivateMessageChannel();

	public abstract boolean sendMessage(String paramString);

	public boolean sendMessage(TranslatableMessage message, StringReplace... replaces) {
		return sendMessage(message, Language.ENGLISH, replaces);
	}

	public abstract AbstractUser getOriginatingCaller();

	public abstract boolean sendMessage(TranslatableMessage paramTranslatableMessage, Language paramLanguage,
			StringReplace... paramVarArgs);
}