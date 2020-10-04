package de.Zorro909.CommandLib.Discord;

import de.Zorro909.AnnotationProcessor.MethodInfo;
import de.Zorro909.CommandAPI.Abstractions.AbstractUser;
import de.Zorro909.CommandAPI.Annotations.AutoComplete;
import de.Zorro909.CommandAPI.Annotations.Command;
import de.Zorro909.CommandAPI.Annotations.Description;
import de.Zorro909.CommandAPI.Annotations.SubCommandHandler;
import de.Zorro909.CommandAPI.Annotations.Usage;
import de.Zorro909.CommandAPI.ArgumentConverter;
import de.Zorro909.CommandAPI.CommandHandler;
import de.Zorro909.CommandAPI.CommandMethodInfo;
import de.Zorro909.ConfigurationLibrary.Messages.Language;
import de.Zorro909.ConfigurationLibrary.Messages.TranslatableMessage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordCommandHandler extends ListenerAdapter implements CommandHandler {
	private CommandMethodInfo rootCommandHandler;
	private List<CommandMethodInfo> subCommandHandlers = new ArrayList<>();
	private HashMap<Class<?>, ArgumentConverter> argumentConverters = new HashMap<>();
	private String description;
	private String usageMessage;
	private String commandPrefix;
	private List<String> cmdNames = new ArrayList<>();

	private JDA jda;

	public DiscordCommandHandler(Class<?> commandClass, Command annotation, String commandPrefix, JDA jda) {
		this.argumentConverters.put(User.class, (name, index, args, context) -> {
			if (name.startsWith("<@!")) {
				name = name.substring(3);
			} else if (name.startsWith("<@")) {
				name = name.substring(2);
			} else {
				return null;
			}
			return jda.getUserById(name.substring(0, 18));
		});
		this.argumentConverters.put(AbstractUser.class, (name, index, args, context) -> {
			if (name.startsWith("<@!")) {
				name = name.substring(3);
			} else if (name.startsWith("<@")) {
				name = name.substring(2);
			} else {
				return null;
			}

			User user = jda.getUserById(name.substring(0, 18));

			return (user == null) ? null
					: (context.containsKey("guild") ? new DiscordUser(((Guild) context.get("guild")).getMember(user))
							: new DiscordUser(user));
		});

		this.argumentConverters.put(DiscordUser.class, this.argumentConverters.get(AbstractUser.class));

		this.cmdNames.add(String.valueOf(commandPrefix) + annotation.value().toLowerCase());
		byte b;
		int i;
		String[] arrayOfString;
		for (i = (arrayOfString = annotation.aliases()).length, b = 0; b < i;) {
			String cmd = arrayOfString[b];
			this.cmdNames.add(String.valueOf(commandPrefix) + cmd.toLowerCase());
			b++;
		}

		this.commandPrefix = commandPrefix;
		this.jda = jda;
	}

	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getMessage().getAuthor().isBot())
			return;
		String rawMessage = event.getMessage().getContentRaw();
		String cmd = rawMessage.split(" ", 2)[0];
		if (!this.cmdNames.contains(cmd)) {
			return;
		}
		String[] args = new String[0];
		if (rawMessage.length() > cmd.length()) {
			args = rawMessage.substring(cmd.length() + 1).split(" ");
		}
		HashMap<String, Object> context = createContext(event);
		if (args.length == 0) {
			try {
				this.rootCommandHandler.execute(new DiscordMessageChannel(event.getChannel(), event.getAuthor()),
						cmd.substring(this.commandPrefix.length()), args, this.argumentConverters, context);
			} catch (Exception e) {

				e.printStackTrace();
				return;
			}
			return;
		}
		for (CommandMethodInfo cmdInfo : this.subCommandHandlers) {
			if (cmdInfo.check(args)) {
				try {
					cmdInfo.execute(new DiscordMessageChannel(event.getChannel(), event.getAuthor()),
							cmd.substring(this.commandPrefix.length()), args, this.argumentConverters, context);
				} catch (Exception e) {
					e.printStackTrace();

					return;
				}

				return;
			}
		}
		try {
			this.rootCommandHandler.execute(new DiscordMessageChannel(event.getChannel(), event.getAuthor()),
					cmd.substring(this.commandPrefix.length()), args, this.argumentConverters, context);
		} catch (Exception e) {

			e.printStackTrace();
			return;
		}
	}

	private HashMap<String, Object> createContext(MessageReceivedEvent event) {
		HashMap<String, Object> context = new HashMap<>();
		if (event.getChannelType() == ChannelType.TEXT) {
			context.put("guild", event.getGuild());
			context.put("author", event.getAuthor());
		}
		return context;
	}

	public void setDescriptionAnnotation(Description descriptionAnnotation) {
		this.description = descriptionAnnotation.value();
	}

	public void setDescriptionAnnotation(Description descriptionAnnotation, Field descriptionField) {
		Object desc;
		try {
			desc = descriptionField.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		if (desc instanceof TranslatableMessage) {
			this.description = ((TranslatableMessage) desc).getText(Language.ENGLISH,
					new de.Zorro909.ConfigurationLibrary.Messages.StringReplace[0]);
		} else {
			this.description = (String) desc;
		}
	}

	public void setUsageAnnotation(Usage usageAnnotation) {
		this.usageMessage = usageAnnotation.value();
	}

	public void setUsageAnnotation(Usage usageAnnotation, Field usageField) {
		Object usage;
		try {
			usage = usageField.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		if (usage instanceof TranslatableMessage) {
			this.usageMessage = ((TranslatableMessage) usage).getText(Language.ENGLISH,
					new de.Zorro909.ConfigurationLibrary.Messages.StringReplace[0]);
		} else {
			this.usageMessage = (String) usage;
		}
	}

	public void registerCommand() {
		this.jda.addEventListener(new Object[] { this });
	}

	public void setRootCommandHandler(MethodInfo commandHandler) {
		this.rootCommandHandler = new CommandMethodInfo("", commandHandler, this);
	}

	public void registerSubCommandHandler(SubCommandHandler handlerAnnotation, MethodInfo commandHandler) {
		this.subCommandHandlers.add(new CommandMethodInfo(handlerAnnotation.command(), commandHandler, this));
	}

	public void registerAutoCompleteProvider(AutoComplete annot, MethodInfo methodInfo) {
	}

	public MethodInfo getAutoCompleteProvider(String autoCompletionID) {
		return null;
	}
}