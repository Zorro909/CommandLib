
package de.Zorro909.CommandLib.Spigot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.SimplePluginManager;

import de.Zorro909.AnnotationProcessor.MethodInfo;
import de.Zorro909.CommandAPI.ArgumentConverter;
import de.Zorro909.CommandAPI.CommandHandler;
import de.Zorro909.CommandAPI.CommandMethodInfo;
import de.Zorro909.CommandAPI.Abstractions.AbstractMessageChannel;
import de.Zorro909.CommandAPI.Abstractions.AbstractUser;
import de.Zorro909.CommandAPI.Annotations.AutoComplete;
import de.Zorro909.CommandAPI.Annotations.Description;
import de.Zorro909.CommandAPI.Annotations.SubCommandHandler;
import de.Zorro909.CommandAPI.Annotations.Usage;
import de.Zorro909.ConfigurationLibrary.Messages.Language;
import de.Zorro909.ConfigurationLibrary.Messages.StringReplace;
import de.Zorro909.ConfigurationLibrary.Messages.TranslatableMessage;

public class SpigotCommandHandler extends Command implements CommandHandler {
	private CommandMethodInfo rootCommandHandler;
	private List<CommandMethodInfo> subCommandHandlers = new ArrayList<>();
	HashMap<String, MethodInfo> registeredAutoCompleteProviders = new HashMap<>();
	HashMap<Class<?>, ArgumentConverter> argumentConverters = new HashMap<>();

	public SpigotCommandHandler(Class<?> commandClass, de.Zorro909.CommandAPI.Annotations.Command annotation) {
		super(annotation.value(), "", "/" + annotation.value(), Arrays.asList(annotation.aliases()));
		this.argumentConverters.put(Player.class, (name, index, args, context) -> Bukkit.getPlayer(name));

		this.argumentConverters.put(AbstractUser.class,
				(name, index, args, context) -> new SpigotUser(Bukkit.getPlayer(name)));

		this.argumentConverters.put(SpigotUser.class, this.argumentConverters.get(AbstractUser.class));
		try {
			this.registeredAutoCompleteProviders.put("onlinePlayers",
					new MethodInfo(null, getClass().getMethod("autoCompleteOnlinePlayers", new Class[0]), null));
		} catch (NoSuchMethodException | SecurityException e) {

			e.printStackTrace();
		}
	}

	public static List<String> autoCompleteOnlinePlayers() {
		return (List<String>) Bukkit.getOnlinePlayers().stream().map(play -> play.getName())
				.collect(Collectors.toList());
	}

	public void setDescriptionAnnotation(Description descriptionAnnotation) {
		setDescription(descriptionAnnotation.value());
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
			setDescription(((TranslatableMessage) desc).getText(Language.ENGLISH, new StringReplace[0]));
		} else {
			setDescription((String) desc);
		}
	}

	public void setUsageAnnotation(Usage usageAnnotation) {
		setUsage(usageAnnotation.value());
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
			setUsage(((TranslatableMessage) usage).getText(Language.ENGLISH, new StringReplace[0]));
		} else {
			setUsage((String) usage);
		}
	}

	public void registerCommand() {
		getCommandMap().register("commandAPI", this);
	}

	private CommandMap getCommandMap() {
		CommandMap commandMap = null;

		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);
				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			System.err.println(e.getMessage());
		}
		return commandMap;
	}

	public void setRootCommandHandler(MethodInfo commandHandler) {
		this.rootCommandHandler = new CommandMethodInfo("", commandHandler, this);
	}

	public void registerSubCommandHandler(SubCommandHandler handlerAnnotation, MethodInfo commandHandler) {
		this.subCommandHandlers.add(new CommandMethodInfo(handlerAnnotation.command(), commandHandler, this));
	}

	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		if (args.length == 0) {
			try {
				this.rootCommandHandler.execute(new SpigotMessageChannel(sender, false), commandLabel, args,
						this.argumentConverters, null);
			} catch (Exception e) {

				e.printStackTrace();
				return false;
			}
			return true;
		}
		for (CommandMethodInfo cmd : this.subCommandHandlers) {
			if (cmd.check(args)) {
				try {
					cmd.execute(new SpigotMessageChannel(sender, false), commandLabel, args, this.argumentConverters,
							null);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
		}

		try {
			this.rootCommandHandler.execute(new SpigotMessageChannel(sender, false), commandLabel, args,
					this.argumentConverters, null);
		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
		return true;
	}

	public List<String> tabComplete(CommandSender sender, String cmdName, String[] args)
			throws IllegalArgumentException {
		AbstractMessageChannel channel = new SpigotMessageChannel(sender, false);
		List<String> completes = new ArrayList<>();
		for (CommandMethodInfo cmi : this.subCommandHandlers) {
			if (cmi.checkForTabComplete(cmdName, args)) {
				completes.addAll(cmi.tabComplete(args, channel));
			}
		}
		System.out.println(Arrays.toString(completes.toArray()));
		return completes;
	}

	public void registerAutoCompleteProvider(AutoComplete annot, MethodInfo methodInfo) {
		this.registeredAutoCompleteProviders.put(annot.autoCompletionID(), methodInfo);
	}

	public MethodInfo getAutoCompleteProvider(String autoCompletionID) {
		return this.registeredAutoCompleteProviders.get(autoCompletionID);
	}
}