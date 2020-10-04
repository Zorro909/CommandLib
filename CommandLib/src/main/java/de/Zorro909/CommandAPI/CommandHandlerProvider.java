package de.Zorro909.CommandAPI;

import de.Zorro909.CommandAPI.Annotations.Command;

public interface CommandHandlerProvider {
  CommandHandler provide(Class<?> paramClass, Command paramCommand);
}
