package de.Zorro909.CommandAPI;

import de.Zorro909.AnnotationProcessor.MethodInfo;
import de.Zorro909.AnnotationProcessor.interfaces.ParameterProvider;
import de.Zorro909.CommandAPI.Abstractions.AbstractMessageChannel;
import de.Zorro909.CommandAPI.Annotations.Argument;
import de.Zorro909.CommandAPI.Annotations.CommandChannel;
import de.Zorro909.CommandAPI.Annotations.RootCommandHandler;
import de.Zorro909.CommandAPI.Annotations.SubCommandHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandMethodInfo {
	private HashMap<Integer, Object> argumentType = new HashMap<>();
	private HashMap<Integer, Integer> argumentMapping = new HashMap<>();
	private HashMap<Integer, MethodInfo> autoCompleteProviders = new HashMap<>();
	private Pattern pattern;
	private MethodInfo methodInfo;
	private boolean isAsync = false;

	public CommandMethodInfo(String command, MethodInfo methodInfo, CommandHandler handler) {
		int totalArguments = 0;
		int nextArgument = 0;
		String regex = "";
		int arg;
		String[] arrayOfString = command.split(" ");
		int arrayLength = arrayOfString.length;
		for (arg = 0; arg < arrayLength; arg++) {
			String part = arrayOfString[arg];
			if (part.equalsIgnoreCase("{}")) {
				regex = regex + " (\\S+)";
				totalArguments++;
			} else if (part.equalsIgnoreCase("[]")) {
				regex = regex + " (.+)";
				this.argumentType.put(Integer.valueOf(totalArguments), String[].class);
				totalArguments++;
			} else {
				regex = regex + " " + part;
				this.argumentType.put(Integer.valueOf(totalArguments), part);
				totalArguments++;
			}
		}

		this.pattern = Pattern.compile(regex.substring(1));

		this.methodInfo = methodInfo;
		if (methodInfo.getAnnotation() instanceof RootCommandHandler) {
			this.isAsync = ((RootCommandHandler) methodInfo.getAnnotation()).async();
		} else if (methodInfo.getAnnotation() instanceof SubCommandHandler) {
			this.isAsync = ((SubCommandHandler) methodInfo.getAnnotation()).async();
		}
		Method method = methodInfo.getAnnotatedMethod();
		Parameter[] params = method.getParameters();
		Annotation[][] annotations = method.getParameterAnnotations();
		for (arg = 0; arg < method.getParameterCount(); arg++) {
			Parameter param = params[arg];
			Annotation[] ann = annotations[arg];
			boolean found = false;
			byte b1;
			int k;
			Annotation[] arrayOfAnnotation1;
			for (k = (arrayOfAnnotation1 = ann).length, b1 = 0; b1 < k;) {
				Annotation annotation = arrayOfAnnotation1[b1];
				if (annotation.annotationType() == CommandChannel.class) {
					found = true;
					this.argumentMapping.put(Integer.valueOf(-1), Integer.valueOf(arg));
					break;
				}
				if (annotation.annotationType() == Argument.class) {
					found = true;
					Argument argu = (Argument) annotation;
					this.argumentType.put(Integer.valueOf(argu.pos()), param.getType());
					this.argumentMapping.put(Integer.valueOf(argu.pos()), Integer.valueOf(arg));
					if (!argu.autoCompletionID().isEmpty())
						this.autoCompleteProviders.put(Integer.valueOf(argu.pos()),
								handler.getAutoCompleteProvider(argu.autoCompletionID()));
					break;
				}
				b1++;
			}

			if (!found) {
				while (this.argumentType.containsKey(Integer.valueOf(nextArgument))) {
					nextArgument++;
				}
				this.argumentType.put(Integer.valueOf(nextArgument), param.getType());
				this.argumentMapping.put(Integer.valueOf(nextArgument), Integer.valueOf(arg));
			}
		}
		if (nextArgument - 1 > totalArguments) {
			throw new RuntimeException("Method expects more Arguments than are available!");
		}
	}

	public boolean check(String[] args) {
		return this.pattern.matcher(String.join(" ", (CharSequence[]) args)).matches();
	}

	public boolean checkForTabComplete(String cmd, String[] args) {
		if (args.length > this.argumentType.size())
			return false;
		for (int i = 0; i < args.length; i++) {
			Object type = this.argumentType.get(Integer.valueOf(i));
			if (type instanceof String && !((String) type).toLowerCase().toLowerCase().startsWith(args[i])) {
				return false;
			}
		}

		return true;
	}

	public List<String> tabComplete(String[] args, AbstractMessageChannel commandChannel) {
		String lastWordR = "";
		Object toFind = this.argumentType.get(Integer.valueOf(0));
		if (args.length > 0) {
			lastWordR = args[args.length - 1].toLowerCase();
			toFind = this.argumentType.get(Integer.valueOf(args.length - 1));
		}
		String lastWord = lastWordR;
		if (toFind instanceof String) {
			if (lastWord.isEmpty() || ((String) toFind).toLowerCase().startsWith(lastWord)) {
				return Arrays.asList(new String[] { (String) toFind });
			}
			return Collections.emptyList();
		}

		if (this.autoCompleteProviders.containsKey(Integer.valueOf(args.length - 1))) {
			try {
				HashMap<Class<? extends Annotation>, ParameterProvider> autoCompleteParameterProvider = new HashMap<>();
				autoCompleteParameterProvider.put(CommandChannel.class, (annot, method, param) -> commandChannel);

				MethodInfo mi = this.autoCompleteProviders.get(Integer.valueOf(args.length - 1));
				List<String> recommendations = (List<String>) mi.execute(autoCompleteParameterProvider,
						(Object[]) args);
				return (List<String>) recommendations.stream().filter(str -> str.toLowerCase().startsWith(lastWord))
						.collect(Collectors.toList());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return Collections.emptyList();
	}

	public void execute(AbstractMessageChannel cs, String cmd, String[] args,
			HashMap<Class<?>, ArgumentConverter> parameterProviders, HashMap<String, Object> context) {
		Object[] arguments = new Object[this.argumentMapping.size()];
		for (Integer sourceArg : this.argumentMapping.keySet()) {
			System.out.println(
					"Processing Source Argument " + sourceArg + " (" + this.argumentMapping.get(sourceArg) + ")");
			if (sourceArg.intValue() == -1) {
				arguments[((Integer) this.argumentMapping.get(sourceArg)).intValue()] = cs;
				continue;
			}
			if (this.argumentType.get(sourceArg) == String.class) {
				arguments[((Integer) this.argumentMapping.get(sourceArg)).intValue()] = args[sourceArg.intValue()];
				continue;
			}
			if (this.argumentType.get(sourceArg) == String[].class) {
				arguments[((Integer) this.argumentMapping.get(sourceArg)).intValue()] = rejoinArray(args, sourceArg);
				continue;
			}
			if (parameterProviders.containsKey(this.argumentType.get(sourceArg))) {
				Object newValue = ((ArgumentConverter) parameterProviders.get(this.argumentType.get(sourceArg)))
						.convert(args[sourceArg.intValue()], sourceArg.intValue(), args, context);
				arguments[((Integer) this.argumentMapping.get(sourceArg)).intValue()] = newValue;
				continue;
			}
			arguments[((Integer) this.argumentMapping.get(sourceArg)).intValue()] = null;
		}

		try {
			if (this.isAsync) {
				(new Thread(() -> {
					try {
						this.methodInfo.getAnnotatedMethod().invoke(null, arguments);
					} catch (IllegalAccessException | IllegalArgumentException
							| java.lang.reflect.InvocationTargetException e) {
						e.printStackTrace();
					}
				})).start();
			} else {
				this.methodInfo.getAnnotatedMethod().invoke((Object) null, arguments);
			}
		} catch (IllegalAccessException | IllegalArgumentException | java.lang.reflect.InvocationTargetException e) {

			e.printStackTrace();
		}
	}

	private Object rejoinArray(String[] args, Integer sourceArg) {
		String[] ret = new String[args.length - sourceArg.intValue()];
		for (int i = 0; i + sourceArg.intValue() < args.length; i++) {
			ret[i] = args[sourceArg.intValue() + i];
		}
		return ret;
	}
}
