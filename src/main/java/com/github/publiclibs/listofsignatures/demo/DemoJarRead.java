/**
 *
 * Для связи со мной используйте почту freedom1b2830@gmail.com
 * Ключ pgp 4388DF6D2D19DA0BD7BB0FBBDBA96F466835877C
 * был отправлен на hkps://keyserver.ubuntu.com
 * также в https://raw.githubusercontent.com/freedom1b2830/freedom1b2830/main/data/pgp.4388DF6D2D19DA0BD7BB0FBBDBA96F466835877C.pub
 */
package com.github.publiclibs.listofsignatures.demo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.publiclibs.listofsignatures.Listofsignatures;
import com.github.publiclibs.listofsignatures.data.classes.ClassObj;

/**
 * @author freedom1b2830
 * @date 2023-февраля-23 14:40:50
 */
public class DemoJarRead {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		final Path input = Paths.get("/home/user_dev_new/short.jar");
		final CopyOnWriteArrayList<ClassObj> result = new CopyOnWriteArrayList<>();
		Listofsignatures.readZip(result, input);

		for (final ClassObj classObj : result) {
			System.out.println(classObj.getSignsForJazzer(true));
		}

	}

}
