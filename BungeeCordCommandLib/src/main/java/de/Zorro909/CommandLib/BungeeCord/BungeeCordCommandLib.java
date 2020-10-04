package de.Zorro909.CommandLib.BungeeCord;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import de.Zorro909.CommandAPI.CommandAPI;
import io.github.classgraph.ClassGraph;
import net.md_5.bungee.api.plugin.Plugin;

public final class BungeeCordCommandLib {

	public static void registerBungeeCordCommandAPI(Plugin pluginInstance) {
		ClassGraph cGraph = new ClassGraph();
		try {
			cGraph.acceptJars(pluginToJarFile(pluginInstance));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("[BungeeCordCommandLib] Could not find Jar File Path associated to Plugin "
					+ pluginInstance.getDescription().getName());
			cGraph.acceptPackages(pluginInstance.getClass().getPackage().toString());
		}
		CommandAPI.registerCommandAPI(cGraph, "minecraft", pluginInstance,
				(cla, annot) -> new BungeeCordCommandHandler(pluginInstance, cla, annot));
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
