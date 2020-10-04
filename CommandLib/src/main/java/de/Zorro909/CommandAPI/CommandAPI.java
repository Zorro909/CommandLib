package de.Zorro909.CommandAPI;

import java.util.HashMap;

import de.Zorro909.AnnotationProcessor.AnnotationProcessor;
import de.Zorro909.CommandAPI.Annotations.AutoComplete;
import de.Zorro909.CommandAPI.Annotations.Command;
import de.Zorro909.CommandAPI.Annotations.Description;
import de.Zorro909.CommandAPI.Annotations.RootCommandHandler;
import de.Zorro909.CommandAPI.Annotations.SubCommandHandler;
import de.Zorro909.CommandAPI.Annotations.Usage;
import de.Zorro909.ConfigurationLibrary.Messages.TranslatableMessage;
import io.github.classgraph.ClassGraph;

public class CommandAPI {
	private static HashMap<Object, HashMap<Class<?>, CommandHandler>> registeredCommands = new HashMap<>();

	public static void registerCommandAPI(String packageName, String backendIdentifier, Object indicator,
			CommandHandlerProvider chProvider) {
		ClassGraph cGraph = new ClassGraph();
		cGraph.enableClassInfo().acceptPackages(packageName);
		registerCommandAPI(cGraph, backendIdentifier, indicator, chProvider);
	}

	public static void registerCommandAPI(ClassGraph preConfiguredClassGraph, String backendIdentifier,
			Object indicator, CommandHandlerProvider chProvider) {
		registeredCommands.put(indicator, new HashMap<>());

		AnnotationProcessor<Command> commandProcessor = new AnnotationProcessor(Command.class);
		commandProcessor.registerClassAnnotationVerifier((annot, clazz) -> {
			if ((annot.supportsBackends()).length != 0) {
				boolean found = false;
				String[] arrayOfString;
				int i = (arrayOfString = annot.supportsBackends()).length;
				for (byte b = 0; b < i; b++) {
					String backend = arrayOfString[b];
					if (backend.equalsIgnoreCase(backendIdentifier))
						found = true;
				}
				if (!found)
					return true;
			}
			System.out.println("Found Command Class for Command " + annot.value());
			((HashMap<Class<?>, CommandHandler>) registeredCommands.get(indicator)).put(clazz,
					chProvider.provide(clazz, annot));
			return true;
		});
		commandProcessor.processClassGraph(preConfiguredClassGraph);
		AnnotationProcessor<Description> descriptionProcessor = new AnnotationProcessor(Description.class);
		descriptionProcessor.registerClassAnnotationVerifier((annot, clazz) -> {
			if (annot.value().isEmpty()) {
				return false;
			}
			if (((HashMap) registeredCommands.get(indicator)).containsKey(clazz)) {
				((CommandHandler) ((HashMap) registeredCommands.get(indicator)).get(clazz))
						.setDescriptionAnnotation(annot);
			}
			return true;
		});
		descriptionProcessor.registerFieldAnnotationValidator((annot, field) -> {
			if (String.class.isAssignableFrom(field.getType())
					|| TranslatableMessage.class.isAssignableFrom(field.getType())) {
				if (((HashMap) registeredCommands.get(indicator)).containsKey(field.getDeclaringClass())) {
					((CommandHandler) ((HashMap) registeredCommands.get(indicator)).get(field.getDeclaringClass()))
							.setDescriptionAnnotation(annot, field);
				}

				return true;
			}

			return false;
		});
		descriptionProcessor.processClassGraph(preConfiguredClassGraph);

		AnnotationProcessor<Usage> usageProcessor = new AnnotationProcessor(Usage.class);
		usageProcessor.registerClassAnnotationVerifier((annot, clazz) -> {
			if (annot.value().isEmpty()) {
				return false;
			}
			if (((HashMap) registeredCommands.get(indicator)).containsKey(clazz)) {
				((CommandHandler) ((HashMap) registeredCommands.get(indicator)).get(clazz)).setUsageAnnotation(annot);
				return true;
			}
			return true;
		});
		usageProcessor.registerFieldAnnotationValidator((annot, field) -> {
			if (String.class.isAssignableFrom(field.getType())
					|| TranslatableMessage.class.isAssignableFrom(field.getType())) {
				if (((HashMap) registeredCommands.get(indicator)).containsKey(field.getDeclaringClass())) {
					((CommandHandler) ((HashMap) registeredCommands.get(indicator)).get(field.getDeclaringClass()))
							.setUsageAnnotation(annot, field);

					return true;
				}

				return true;
			}
			return false;
		});
		usageProcessor.processClassGraph(preConfiguredClassGraph);

		AnnotationProcessor<AutoComplete> autoCompleteProcessor = new AnnotationProcessor(AutoComplete.class);

		autoCompleteProcessor.setMethodRegistrar((annot, methodInfo) -> {
			if (((HashMap) registeredCommands.get(indicator)).containsKey(methodInfo.getDeclaringClass())) {
				((CommandHandler) ((HashMap) registeredCommands.get(indicator)).get(methodInfo.getDeclaringClass()))
						.registerAutoCompleteProvider(annot, methodInfo);
			}

			return true;
		});

		autoCompleteProcessor.processClassGraph(preConfiguredClassGraph);

		AnnotationProcessor<RootCommandHandler> rootCommandHandlerProcessor = new AnnotationProcessor(
				RootCommandHandler.class);

		rootCommandHandlerProcessor.setMethodRegistrar((annot, methodInfo) -> {
			if (((HashMap) registeredCommands.get(indicator)).containsKey(methodInfo.getDeclaringClass())) {
				CommandHandler handler = (CommandHandler) ((HashMap) registeredCommands.get(indicator))
						.get(methodInfo.getDeclaringClass());

				handler.setRootCommandHandler(methodInfo);
			}

			return true;
		});
		AnnotationProcessor<SubCommandHandler> subCommandHandlerProcessor = new AnnotationProcessor(
				SubCommandHandler.class);

		subCommandHandlerProcessor.setMethodRegistrar((annot, methodInfo) -> {
			if (((HashMap) registeredCommands.get(indicator)).containsKey(methodInfo.getDeclaringClass())) {
				CommandHandler handler = (CommandHandler) ((HashMap) registeredCommands.get(indicator))
						.get(methodInfo.getDeclaringClass());

				handler.registerSubCommandHandler(annot, methodInfo);
			}

			return true;
		});
		rootCommandHandlerProcessor.processClassGraph(preConfiguredClassGraph);
		subCommandHandlerProcessor.processClassGraph(preConfiguredClassGraph);

		for (CommandHandler ch : ((HashMap<Class<?>, CommandHandler>) registeredCommands.get(indicator)).values()) {
			ch.registerCommand();
		}
	}

}
