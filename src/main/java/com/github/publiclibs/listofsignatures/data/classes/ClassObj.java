/**
 *
 */
package com.github.publiclibs.listofsignatures.data.classes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.publiclibs.listofsignatures.data.methods.MethodObj;

/**
 * @author freedom1b2830
 * @date 2023-января-12 04:24:43
 */
public class ClassObj implements Serializable {
	private static final long serialVersionUID = -5729127808662383342L;

	/**
	 * @param arguments
	 * @return
	 */
	private static StringBuilder createArgString(final String[] arguments) {
		final var argBuilder = new StringBuilder();
		final var args = Arrays.asList(arguments).iterator();
		while (args.hasNext()) {
			final var arg = args.next();
			argBuilder.append(arg);
			if (args.hasNext()) {
				argBuilder.append(",");
			}
		}
		return argBuilder;
	}

	private String simpleName;
	public ConstructorObj[] constructors;
	public MethodObj[] methods;

	private String fullClassName;

	/**
	 * @param className
	 * @param simpleClassName
	 */
	public ClassObj(final String fullClassName) {
		convertToSimple(fullClassName);
	}

	private String convertToSimple(final String fullIn) {
		final var MAVEN_TARGET_CLASSES = "target/classes/";
		var full = fullIn;
		if (full.contains(MAVEN_TARGET_CLASSES)) {
			full = full.split(MAVEN_TARGET_CLASSES)[1];
		}
		final var CLASSEXT = ".class";
		if (full.endsWith(CLASSEXT)) {
			full = full.substring(0, full.length() - CLASSEXT.length());
		}
		fullClassName = full.replaceAll("/", ".");
		final var data = fullClassName.split("\\.");
		simpleName = data[data.length - 1];

		return fullClassName;
	}

	/**
	 * @param ret
	 * @param b
	 * @param full
	 */
	private void dump(final CopyOnWriteArrayList<String> ret, final boolean onlyPublic, final boolean full) {

		if (getConstructorsCount() > 0) {
			final List<ConstructorObj> constructorsList = Arrays.asList(constructors);

			for (final ConstructorObj constructorObj : constructorsList) {
				if (onlyPublic && !constructorObj.isPublic()) {
					continue;
				}
				if (full) {
					final var argBuilder = createArgString(constructorObj.arguments);
					if (constructorObj.getArgumentsCount() > 0) {
						ret.addIfAbsent(fullClassName + "::new(" + argBuilder + ")");
					} else {
						ret.addIfAbsent(fullClassName + "::new");
					}
				} else {
					ret.addIfAbsent(fullClassName + "::new");
				}

			}
		} // constr

		// methods
		if (getMethodsCount() > 0) {
			final List<MethodObj> methodsList = Arrays.asList(methods);
			for (final MethodObj methodObj : methodsList) {
				if (onlyPublic && !methodObj.isPublic()) {
					continue;
				}
				final var name = methodObj.name;
				if (full) {
					ret.addIfAbsent(fullClassName + "::" + name + "(" + createArgString(methodObj.arguments) + ")");
				} else {
					ret.addIfAbsent(fullClassName + "::" + name);
				}
			}
		}
	}

	/**
	 * @return the constructors
	 */
	public int getConstructorsCount() {
		if (constructors == null) {
			return 0;
		}
		return constructors.length;
	}

	/**
	 * @return the methods
	 */
	public int getMethodsCount() {
		if (methods == null) {
			return 0;
		}
		return methods.length;
	}

	public CopyOnWriteArrayList<String> getSignsForJazzer(final boolean full) {
		final var ret = new CopyOnWriteArrayList<String>();

		dump(ret, true, full);

		return ret;
	}

	/**
	 * @return the simpleName
	 */
	public String getSimpleName() {
		return simpleName;
	}
}
