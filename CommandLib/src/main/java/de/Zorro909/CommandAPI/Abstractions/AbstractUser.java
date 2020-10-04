package de.Zorro909.CommandAPI.Abstractions;

public abstract class AbstractUser {
	public abstract String getName();

	public abstract AbstractMessageChannel openPrivateMessageChannel();

	public abstract boolean hasPermission(String paramString);

	public abstract boolean ban(String paramString);

	public abstract boolean kick(String paramString);
}
