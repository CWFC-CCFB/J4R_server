/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2021 Mathieu Fortin for Rouge Epicea.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package j4r.util;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarUtility {
	
	private static final String JAR_FILE_PREFIX = "jar:file:";

	/**
	 * Return a boolean that indicates whether the class is embedded in a Jar file or not.
	 * @param clazz the class
	 * @return a boolean true means the class is in a jar file or false otherwise
	 */
	public static boolean isEmbeddedInJar(Class<?> clazz) {
		return getJarFileIAmInIfAny(clazz) != null;
	}

	/**
	 * Provide the name of the jar file that contains this class. If the class has not been packaged
	 * in a jar file, then the method returns null. 
	 * @param clazz the class 
	 * @return the name of the jar or null
	 */
	public static String getJarFileIAmInIfAny(Class<?> clazz) {
		String className = clazz.getSimpleName();
		URL resourceURL = clazz.getResource(className + ".class");
		String resourcePath = resourceURL.toString();
		if (resourcePath.startsWith(JAR_FILE_PREFIX)) {
			int indexMark = resourcePath.indexOf("!");
			return resourcePath.substring(JAR_FILE_PREFIX.length(), indexMark);	// we remove the jar: prefix here
		} else {
			return null;
		}
	}
	
	/**
	 * Return the manifest of a jar file.
	 * @param jarFilePath the path of the jar file
	 * @return a Manifest object 
	 * @throws IOException if an I/O error has occurred
	 */
	public static Manifest getManifestFromThisJarFile(String jarFilePath) throws IOException {
		JarFile file = new JarFile(jarFilePath);
		Manifest m = file.getManifest();
		file.close();
		return m;
	}

}
