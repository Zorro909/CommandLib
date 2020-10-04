package de.Zorro909.CommandAPI;

import de.Zorro909.AnnotationProcessor.MethodInfo;
import de.Zorro909.CommandAPI.Annotations.AutoComplete;
import de.Zorro909.CommandAPI.Annotations.Description;
import de.Zorro909.CommandAPI.Annotations.SubCommandHandler;
import de.Zorro909.CommandAPI.Annotations.Usage;
import java.lang.reflect.Field;

public interface CommandHandler {
  void setDescriptionAnnotation(Description paramDescription);
  
  void setUsageAnnotation(Usage paramUsage);
  
  void setDescriptionAnnotation(Description paramDescription, Field paramField);
  
  void setUsageAnnotation(Usage paramUsage, Field paramField);
  
  MethodInfo getAutoCompleteProvider(String paramString);
  
  void registerAutoCompleteProvider(AutoComplete paramAutoComplete, MethodInfo paramMethodInfo);
  
  void setRootCommandHandler(MethodInfo paramMethodInfo);
  
  void registerSubCommandHandler(SubCommandHandler paramSubCommandHandler, MethodInfo paramMethodInfo);
  
  void registerCommand();
}
