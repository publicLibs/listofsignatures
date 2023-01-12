/**
 *
 */
package com.github.publiclibs.listofsignatures.reader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.github.publiclibs.listofsignatures.data.classes.ClassObj;
import com.github.publiclibs.listofsignatures.data.classes.ConstructorObj;
import com.github.publiclibs.listofsignatures.data.methods.MethodObj;

/**
 * @author freedom1b2830
 * @date 2023-января-12 04:27:12
 */
public class LSClassReader extends ClassReader {

	private final ClassObj result;

	public LSClassReader(final String fullClassName) throws IOException {
		super(fullClassName);
		result = new ClassObj(fullClassName);
	}

	public LSClassReader(final String fullClassName, final byte[] classFile) {
		super(classFile);
		result = new ClassObj(fullClassName);
	}

	public LSClassReader(final String className, final byte[] classFileBuffer, final int classFileOffset,
			final int classFileLength) {
		super(classFileBuffer, classFileOffset, classFileLength);
		result = new ClassObj(className);
	}

	public LSClassReader(final String fullClassName, final InputStream inputStream) throws IOException {
		super(inputStream);
		result = new ClassObj(fullClassName);
	}

	public LSClassReader(final String className, final Path path) throws IOException {
		super(Files.readAllBytes(path));
		result = new ClassObj(className);
	}

	private void print(final String methodName, final int access, final String desc) {
		// args
		final var classArgsAr = Type.getArgumentTypes(desc);
		final var argsLen = classArgsAr.length;
		final List<Type> argList = Arrays.asList(classArgsAr);
		final var args = new String[argsLen];
		for (var i = 0; i < args.length; i++) {
			final var type = argList.get(i);
			args[i] = type.getClassName();
		}

		if ("<init>".equals(methodName)) {
			final var constructor = new ConstructorObj();
			constructor.arguments = args;
			constructor.access = access;
			if (result.constructors == null) {
				result.constructors = new ConstructorObj[] { constructor };
			} else {
				final var oldConstructors = result.constructors;
				final var newConstructors = new ConstructorObj[oldConstructors.length + 1];
				for (var i = 0; i < oldConstructors.length; i++) {
					newConstructors[i] = oldConstructors[i];
				}
				newConstructors[newConstructors.length - 1] = constructor;
				result.constructors = newConstructors;
			}
		} else {
			// return
			final var returnType = Type.getReturnType(desc);
			final var returnCLassName = returnType.getClassName();

			final var stringBuilder = new StringBuilder();
			stringBuilder.append(returnCLassName).append(" ").append(methodName);

			final var method = new MethodObj();
			method.access = access;
			method.returnClassName = returnCLassName;
			method.name = methodName;
			method.arguments = args;
			if (result.methods == null) {
				result.methods = new MethodObj[] { method };
			} else {
				final var oldMethods = result.methods;
				final var newMethods = new MethodObj[oldMethods.length + 1];
				for (var i = 0; i < oldMethods.length; i++) {
					newMethods[i] = oldMethods[i];
				}
				newMethods[newMethods.length - 1] = method;
			}
		}

	}

	/**
	 * @return
	 *
	 */
	public ClassObj read() {
		final ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9) {
			public @Override MethodVisitor visitMethod(final int access, final String name, final String desc,
					final String signature, final String[] exceptions) {
				print(name, access, desc);
				return super.visitMethod(access, name, desc, signature, exceptions);
			}
		};
		accept(classVisitor, 0);
		return result;
	}
}
