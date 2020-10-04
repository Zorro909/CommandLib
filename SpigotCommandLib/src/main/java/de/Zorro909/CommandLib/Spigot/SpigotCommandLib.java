package de.Zorro909.CommandLib.Spigot;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import org.bukkit.plugin.Plugin;

import de.Zorro909.CommandAPI.CommandAPI;
import io.github.classgraph.ClassGraph;

public final class SpigotCommandLib {

	public static void registerSpigotCommandAPI(Plugin pluginInstance) {
		ClassGraph cGraph = new ClassGraph();
		try {
			cGraph.acceptJars(pluginToJarFile(pluginInstance));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(
					"[SpigotCommandLib] Could not find Jar File Path associated to Plugin " + pluginInstance.getName());
			cGraph.acceptPackages(pluginInstance.getClass().getPackage().toString());
		}
		CommandAPI.registerCommandAPI(cGraph, "minecraft", pluginInstance,
				(cla, annot) -> new SpigotCommandHandler(cla, annot));
	}

	private static String pluginToJarFile(Plugin plugin) throws UnsupportedEncodingException {
		String rawName = plugin.getClass().getName();
		int idx = rawName.lastIndexOf('.');
		String classFileName = (idx == -1 ? rawName : rawName.substring(idx + 1)) + ".class";
		String fileName = URLDecoder.decode(
				plugin.getClass().getResource(classFileName).toString().substring("jar:file:".length(), idx),
				Charset.defaultCharset().name());
		return new File(fileName).getAbsolutePath();
	}

}
