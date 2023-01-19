/**
 *
 */
package com.github.publiclibs.listofsignatures;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.github.publiclibs.listofsignatures.data.classes.ClassObj;
import com.github.publiclibs.listofsignatures.reader.LSClassReader;

/**
 * @author user_dev_new
 *
 */
public class Listofsignatures {

	public static void findClassInDir(final CopyOnWriteArrayList<ClassObj> result, final Path path1)
			throws IOException {
		if (Files.isDirectory(path1)) {
			try (var stream = Files.list(path1)) {
				stream.forEachOrdered((final var path) -> {
					try {
						findClassInDir(result, path);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				});
			}
		} else if (Files.isRegularFile(path1)) {
			final var full = path1.toFile().getAbsolutePath();
			if (full.endsWith(".class")) {
				final var reader = new LSClassReader(full, path1);
				result.addIfAbsent(reader.read());
			}
			if (full.endsWith(".jar")) {
				readZip(result, path1);
			}
		}

	}

	/**
	 * @param appendLibs
	 * @return
	 * @throws IOException
	 */
	private static CopyOnWriteArrayList<ClassObj> getForClassPath(final CopyOnWriteArrayList<Path> appendLibs)
			throws IOException {
		final var classPaths = System.getProperty("java.class.path").split(File.pathSeparator);
		final var result = new CopyOnWriteArrayList<ClassObj>();
		for (final String classPath : classPaths) {
			final var classPathPath = Paths.get(classPath);
			findClassInDir(result, classPathPath);
		}
		for (final Path path : appendLibs) {
			findClassInDir(result, path);
		}

		return result;
	}

	public static Stream<String> getForClassPathWithSettings(final boolean full, final List<String> showOnlyRegExs,
			final CopyOnWriteArrayList<Path> appendLibs) throws IOException {

		final var returnData = new ArrayList<String>();

		final var result = getForClassPath(appendLibs);
		for (final ClassObj classObj : result) {
			final var list = classObj.getSignsForJazzer(full);
			//
			for (final String sign : list) {
				if (show(showOnlyRegExs, sign)) {
					returnData.add(sign);
				}
			}
		}
		return returnData.stream();
	}

	public static void main(final String[] args) throws IOException {
		var full = false;
		final var needShowRegEx = new CopyOnWriteArrayList<String>();
		final var appendLibs = new CopyOnWriteArrayList<Path>();
		for (final String arg : args) {
			final var tmpPath = Paths.get(arg).toAbsolutePath();

			if (arg.equals("FULL")) {
				full = true;
				continue;
			}
			if (Files.exists(tmpPath)) {
				appendLibs.addIfAbsent(tmpPath);
			} else {
				needShowRegEx.addIfAbsent(arg);
			}
		}
		final var result = getForClassPathWithSettings(full, needShowRegEx, appendLibs);
		result.forEachOrdered(System.out::println);
	}

	public static void readZip(final CopyOnWriteArrayList<ClassObj> result, final Path input) throws IOException {
		try (var zipFile = new ZipFile(input.toFile())) {
			final Enumeration<? extends ZipEntry> entriesEnumeration = zipFile.entries();
			while (entriesEnumeration.hasMoreElements()) {
				final ZipEntry zipEntry = entriesEnumeration.nextElement();
				if (zipEntry.getName().endsWith(".class")) {
					try (var is = zipFile.getInputStream(zipEntry)) {
						final var reader = new LSClassReader(zipEntry.getName(), is);
						result.addIfAbsent(reader.read());
					}
				}
			}
		}
	}

	/**
	 * @param ignoreRegExs
	 * @param sign
	 * @return
	 */
	private static boolean show(final List<String> showOnlyRegExs, final String sign) {
		if (showOnlyRegExs.isEmpty()) {
			return true;
		}
		for (final String regEx : showOnlyRegExs) {
			if (sign.matches(regEx)) {
				return true;
			}
		}
		return false;
	}

}
