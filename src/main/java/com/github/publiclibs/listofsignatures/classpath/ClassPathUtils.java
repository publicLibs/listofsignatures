/**
 *
 */
package com.github.publiclibs.listofsignatures.classpath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author freedom1b2830
 * @date 2023-января-22 16:24:20
 */
public class ClassPathUtils {

	/**
	 * @param cp
	 * @return
	 */
	public static String createCParg(final CopyOnWriteArrayList<Path> cps) {
		if (cps.isEmpty()) {
			return "";
		}

		final var arg = new StringBuilder();
		arg.append("--cp ");
		arg.append("\"");
		final var iter = cps.iterator();
		while (iter.hasNext()) {
			final var cp = iter.next();

			arg.append(cp.toAbsolutePath());
			if (iter.hasNext()) {
				arg.append(File.pathSeparatorChar);
			}
		}
		arg.append("\"");
		return arg.toString();
	}

	/**
	 * @param jars2
	 * @param inputPath
	 */
	public static void findAllJars(final CopyOnWriteArrayList<Path> jars, final Path inputPath) {
		if (Files.isDirectory(inputPath)) {
			try (var list = Files.list(inputPath)) {
				list.forEachOrdered(inDirPath -> {
					findAllJars(jars, inDirPath);
				});
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else if (Files.isRegularFile(inputPath)) {
			final var name = inputPath.toFile().getName();
			if (name.endsWith(".jar")) {
				jars.add(inputPath);
			}
		}

	}

	/**
	 * @param path
	 * @param classes
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void findClassesInDir(final Path path, final CopyOnWriteArrayList<Path> classes) throws IOException {
		if (Files.isDirectory(path)) {
			try (var list = Files.list(path)) {
				list.forEachOrdered(inDirPath -> {
					try {
						findClassesInDir(inDirPath, classes);
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else if (Files.isRegularFile(path)) {
			final var name = path.toFile().getName();
			if (name.endsWith(".class")) {
				try (var fis = new FileInputStream(path.toFile())) {
					final var classNode = new ClassNode();
					new ClassReader(fis).accept(classNode, ClassReader.EXPAND_FRAMES);
					final var realname = classNode.name;
					final var pa = path.normalize().toAbsolutePath();
					final var data = pa.toString().split(realname)[0];
					if (pa.toString().startsWith(data)) {
						final var newPath = Paths.get(data).normalize().toAbsolutePath();
						classes.addIfAbsent(newPath);
					}

				}

				// FIX SPLIT BY NAME [0]
			}
		}

	}

	public static void recognize(final CopyOnWriteArrayList<Path> jars, final CopyOnWriteArrayList<Path> classes,
			final Path path) throws IOException {
		if (Files.notExists(path)) {
			System.out.println("WARN classPath not exit " + path);
		}
		if (Files.isRegularFile(path)) {
			if (path.getFileName().toString().endsWith(".jar")) {
				jars.addIfAbsent(path);
			}
		} else if (Files.isDirectory(path)) {
			findClassesInDir(path, classes);
		}
	}

	public static void recognizeClassPath(final CopyOnWriteArrayList<Path> jars,
			final CopyOnWriteArrayList<Path> classes) throws IOException {
		final var classPaths = System.getProperty("java.class.path").split(File.pathSeparator);
		for (final String string : classPaths) {
			final var path = Paths.get(string).toAbsolutePath().normalize();
			recognize(jars, classes, path);
		}
	}
}
