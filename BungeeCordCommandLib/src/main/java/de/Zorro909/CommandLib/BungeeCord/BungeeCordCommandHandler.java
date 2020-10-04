
package de.Zorro909.CommandLib.BungeeCord;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.event.EventHandler;

public class BungeeCordCommandHandler extends Command implements CommandHandler, TabExecutor {
	private CommandMethodInfo rootCommandHandler;
	private List<CommandMethodInfo> subCommandHandlers = new ArrayList<>();
	HashMap<String, MethodInfo> registeredAutoCompleteProviders = new HashMap<>();
	HashMap<Class<?>, ArgumentConverter> argumentConverters = new HashMap<>();
	private String description;
	private String usageMessage;
	private Plugin plugin;

	public BungeeCordCommandHandler(Plugin plugin, Class<?> commandClass,
			de.Zorro909.CommandAPI.Annotations.Command annotation) {
		super(annotation.value(), null, annotation.aliases());
		this.plugin = plugin;
		this.argumentConverters.put(ProxiedPlayer.class,
				(name, index, args, context) -> ProxyServer.getInstance().getPlayer(name));

		this.argumentConverters.put(AbstractUser.class,
				(name, index, args, context) -> new BungeeCordUser(ProxyServer.getInstance().getPlayer(name)));

		this.argumentConverters.put(BungeeCordUser.class, this.argumentConverters.get(AbstractUser.class));
	}

	public void setDescriptionAnnotation(Description descriptionAnnotation) {
		description = descriptionAnnotation.value();
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
			description = ((TranslatableMessage) desc).getText(Language.ENGLISH, new StringReplace[0]);
		} else {
			description = (String) desc;
		}
	}

	public void setUsageAnnotation(Usage usageAnnotation) {
		usageMessage = usageAnnotation.value();
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
			usageMessage = ((TranslatableMessage) usage).getText(Language.ENGLISH, new StringReplace[0]);
		} else {
			usageMessage = (String) usage;
		}
	}

	public void registerCommand() {
		ProxyServer.getInstance().getPluginManager().registerCommand(plugin, this);
	}

	public void setRootCommandHandler(MethodInfo commandHandler) {
		this.rootCommandHandler = new CommandMethodInfo("", commandHandler, this);
	}

	public void registerSubCommandHandler(SubCommandHandler handlerAnnotation, MethodInfo commandHandler) {
		this.subCommandHandlers.add(new CommandMethodInfo(handlerAnnotation.command(), commandHandler, this));
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			try {
				this.rootCommandHandler.execute(new BungeeCordMessageChannel(sender, false), getName(), args,
						this.argumentConverters, null);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			return;
		}
		for (CommandMethodInfo cmd : this.subCommandHandlers) {
			if (cmd.check(args)) {
				try {
					cmd.execute(new BungeeCordMessageChannel(sender, false), getName(), args, this.argumentConverters,
							null);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				return;
			}
		}

		try {
			this.rootCommandHandler.execute(new BungeeCordMessageChannel(sender, false), getName(), args,
					this.argumentConverters, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerAutoCompleteProvider(AutoComplete annot, MethodInfo methodInfo) {
		this.registeredAutoCompleteProviders.put(annot.autoCompletionID(), methodInfo);
	}

	public MethodInfo getAutoCompleteProvider(String autoCompletionID) {
		return this.registeredAutoCompleteProviders.get(autoCompletionID);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		AbstractMessageChannel channel = new BungeeCordMessageChannel(sender, false);
		List<String> completes = new ArrayList<>();
		for (CommandMethodInfo cmi : this.subCommandHandlers) {
			if (cmi.checkForTabComplete(getName(), args)) {
				completes.addAll(cmi.tabComplete(args, channel));
			}
		}
		return completes;
	}
}