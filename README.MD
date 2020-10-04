# CommandLib

CommandLib is a Java library for dealing with commands in various Situations via an easy to use Annotation based Interface.

## Features

- Annotation based
- Backend independent (Spigot, BungeeCord, Discord)
- Dynamic command registration
- Automatic argument casting
- Supports automatic and easy custom Tab Completion (Minecraft)
- Supports async Command execution

## Maven

```
<dependencies>
  <dependency>
    <groupId>com.github.Zorro909.CommandLib</groupId>
    <artifactId>[Spigot|BungeeCord|Discord]CommandLib</artifactId>
    <version>-SNAPSHOT</version>
</dependencies>
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

## Usage

```java
@Command(value = "ping")
public class PingCommand {
  
  @Description
  public static String description = "It's a ping Command ;)";

  @Usage
  public static String usage = "/ping {message}";

  @RootCommandHandler
  public static void ping(@CommandChannel AbstractMessageChannel channel){
    channel.sendMessage("Pong!");
  }

  @SubCommandHandler(command = "[]")
  public static void pingBack(@CommandChannel AbstractMessageChannel channel, String[] message) {
    channel.sendMessage(String.join(" ", message);
  }
```

Complete examples can be found in the [CommandLib-Examples](https://github.com/Zorro909/CommandLib-Examples) Project

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)
