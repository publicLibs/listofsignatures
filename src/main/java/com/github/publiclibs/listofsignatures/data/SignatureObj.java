/**
 *
 */
package com.github.publiclibs.listofsignatures.data;

import java.io.Serializable;

import org.objectweb.asm.Opcodes;

/**
 * @author freedom1b2830
 * @date 2023-января-12 05:43:07
 */
public class SignatureObj implements Serializable {

	public int access;

	public String[] arguments;

	/**
	 * @return the arguments
	 */
	public int getArgumentsCount() {
		if (arguments == null) {
			return 0;
		}
		return arguments.length;
	}

	public boolean isPublic() {
		return ((Opcodes.ACC_PUBLIC & access) > 0);
	}
}
