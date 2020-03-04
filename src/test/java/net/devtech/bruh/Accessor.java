package net.devtech.bruh;

import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Accessor {
	public static final String NMS_VERSION = "v1_15_R1/";

	public static void main(String[] args) throws IOException {
		// instructions
		// run build tools
		// find craftbukkit-1.X.X-R0.1-SNAPSHOT.jar in CraftBukkit/target
		// extract it
		// run it against this program
		// download crusty mappings, bug ramdisk or something
		// download the latest tiny remapper from here https://maven.fabricmc.net/net/fabricmc/tiny-remapper/
		// download the yarn mappings used by fukkit https://maven.fabricmc.net/net/fabricmc/yarn/
		// run tiny remapper as follows
		// java -jar <remapper>.jar craftbukkit-1.X.X-R0.1-SNAPSHOT.jar inter-cb-1.X.X.jar crusty-1.X.X.tiny named intermediary
		// java -jar <remapper>.jar inter-cb-1.X.X.jar crustbukkit-1.X.X.jar <yarn mappings> intermediary yarn
		// go into the zip file, and delete every directory except for org.bukkit.craftbukkit
		File file = new File("craftbukkit-1.15.2-R0.1-SNAPSHOT.jar");
		File out = new File("crustbukkit-1.15.2-R0.1-SNAPSHOT.jar");
		relocate(file, out);
	}

	private static void download(String url, File file) throws IOException {
		URL u = new URL(url);
		copy(u.openStream(), new FileOutputStream(file));
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) != -1) output.write(buffer, 0, len);
		output.close();
		input.close();
	}

	public static void relocateDeps(File file) {

	}

	public static void relocate(File file, File result) throws IOException {
		List<Relocation> relocations = new ArrayList<>();
		for (String name : getNMSLabeledPackages(file)) {
			relocations.add(new Relocation(name, name.replace(NMS_VERSION, "")));
		}
		JarRelocator relocator = new JarRelocator(file, result, relocations);
		relocator.run();
	}

	public static List<String> getNMSLabeledPackages(File file) throws IOException {
		List<String> nms = new ArrayList<>();
		ZipFile zip = new ZipFile(file);
		Enumeration<ZipEntry> enumeration = (Enumeration<ZipEntry>) zip.entries();
		while (enumeration.hasMoreElements()) {
			ZipEntry entry = enumeration.nextElement();
			if(entry.isDirectory()) {
				String name = entry.getName();
				if(name.endsWith(NMS_VERSION))
					nms.add(name);
			}
		}
		return nms;
	}

}
