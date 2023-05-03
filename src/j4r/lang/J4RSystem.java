/*
 * This file is part of the j4r library.
 *
 * Copyright (C) 2020-2021 Her Majesty the Queen in right of Canada
 * Author: Mathieu Fortin, Canadian Wood Fibre Centre, Canadian Forest Service.
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
package j4r.lang;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import j4r.net.server.JavaGatewayServer;

/**
 * The REpiceaSystem offers some additional features to the System class.
 * Among others, it allows for dynamic classpath. The methods deliberately
 * call the system class loader and add the path to the class path at this 
 * level.
 * @author Mathieu Fortin - November 2014
 */
public class J4RSystem {

	
	private static String jreVersion;
	
	private static String revision;

	
	static {
		String completeJREVersion = System.getProperty("java.version");
		try {
			jreVersion = completeJREVersion.substring(0, completeJREVersion.indexOf("_"));
			revision = completeJREVersion.substring(completeJREVersion.indexOf("_") + 1);
		} catch (Exception e) {
			jreVersion = completeJREVersion;
			revision = "unknown";
		}
	}

	
	/**
	 * This method returns the temporary input/output directory. It is preferable to the System.getProperty("java.io.tmpdir") method
	 * because it makes sure the path ends with a file separator.
	 * @return a String
	 */
	public static String getJavaIOTmpDir() {
		String directory = System.getProperty("java.io.tmpdir");
		String osName = System.getProperty("os.name");
		if (osName.equals("Linux")) {
			directory = directory.concat(File.separator);
		}
		return directory;
	}
	


	/**
	 * Returns the architecture of the JVM, i.e. either 32-Bit, 64-Bit or unknown.
	 * @return a String
	 */
	public static String getJavaArchitecture() {
		return System.getProperty("sun.arch.data.model");
	}

	
	private static int parseJVMVersion(String jvmVersion) {
		String[] splittedDigits = jvmVersion.split("\\.");
		int firstInteger = Integer.parseInt(splittedDigits[0]);
		if (firstInteger != 1) {
			return firstInteger;	// prior to Java 11 the string is something like 1.8.0. After that it is something like 11.0.6
		} else {
			return Integer.parseInt(splittedDigits[1]);
		}
	}
	
	/**
	 * This method returns true if the current version of the JVM is more recent than the parameter targetJVM
	 * @param targetJVM a String
	 * @param upToThirdDigit a boolean true to test up to the third digit
	 * @return a boolean
	 */
	public static boolean isCurrentJVMLaterThanThisVersion(String targetJVM, boolean upToThirdDigit) {
		int currentVersion = parseJVMVersion(getJVMVersion());
		int targetVersion = parseJVMVersion(targetJVM);
		if (currentVersion > targetVersion) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method returns true if the current version of the JVM is more recent than the parameter targetJVM. The 
	 * test is carried out on the first and second digit only. For instance, versions 1.7.12 and 1.7.17 would be equally
	 * recent.
	 * @param targetJVM a String
	 * @return a boolean
	 */
	public static boolean isCurrentJVMLaterThanThisVersion(String targetJVM) {
		return isCurrentJVMLaterThanThisVersion(targetJVM, false);
	}


	/**
	 * This static methods processes the arguments given to the main function in a very classical manner. 
	 * The language is set if the args parameter contains -l followed by either en or fr. Then the arguments
	 * are returned as a List of String instances for further specific processing.
	 * @param args the input array of String instances
	 * @return a List of String instances
	 */
	public static List<String> setClassicalOptions(String[] args) {
		if (args.length == 0) {
			return new ArrayList<String>();
		} else {
			String inputString = "";
			for (String str : args) {
				inputString = inputString + str + "; ";
			}
			return Arrays.asList(args);
		}
	}
	
	/**
	 * This method scan the arguments for a particular option and returns the value of that option.
	 * @param option the option
	 * @param argumentList a List of String instances
	 * @return a String or null if the option was not found
	 */
	public static String retrieveArgument(String option, List<String> argumentList) {
		if (argumentList.contains(option) && (argumentList.indexOf(option) + 1 < argumentList.size())) {
			return argumentList.get(argumentList.indexOf(option) + 1);
		} else {
			return null;
		}
	}
	
	
	/**
	 * This method returns the version of the virtual machine.
	 * @return a String
	 */
	public static String getJVMVersion() {return jreVersion;}
	
	/**
	 * This method returns the revision of the virtual machine.
	 * @return a String
	 */
	public static String getJVMRevision() {return revision;}

	/**
	 * Returns true if the OS is Windows or false otherwise.
	 * @return a boolean
	 */
	public static boolean isRunningOnWindows() {
		if (System.getProperty("os.name").startsWith("Windows")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Provides the different URLs in the class path. <br>
	 * <br>
	 * This method is only available for private server (local server). If called on a public server, it throws
	 * a SecurityException. 
	 * @return a List of String
	 * @throws Exception a SecurityException if the server is public or a ReflectiveOperationExcception if reflection failed.
	 * @deprecated Use the areThesePatternsInClassPath method instead
	 */
	@Deprecated
	public static List<String> getClassPathURLs() throws ReflectiveOperationException {
		if (JavaGatewayServer.isPublicServerRunning()) {
			throw new SecurityException("The method getClassPathURLs is not accessible for public servers!");
		}
		return getInternalClassPathURLs();
	}
	
	/**
	 * Check if some patterns can be found in the class path. <br>
	 * <br>
	 * Typically this method is used to check if the server is running with the appropriate extensions.
	 * 
	 * @param patterns a List of patterns to be checked
	 * @return the patterns that were not found
	 * @throws ReflectiveOperationException
	 */
	public static List<String> checkIfPatternsAreInClassPath(List<String> patterns) throws ReflectiveOperationException {
		List<String> patternsNotFound = new ArrayList<String>();
		List<String> currentURLs = getInternalClassPathURLs();
		for (String p : patterns) {
			boolean found = false;
			for (String url : currentURLs) {
				if (url.contains(p)) {
					found = true;
					break;
				}
			}
			if (!found)
				patternsNotFound.add(p);
		}
		return patternsNotFound;
	}
	
	
	/**
	 * Provides the different URLs in the class path.
	 * @return a List of String
	 * @throws Exception
	 */
	protected static List<String> getInternalClassPathURLs() throws ReflectiveOperationException {
		URL[] urls;
		if (J4RSystem.isCurrentJVMLaterThanThisVersion("15.9")) {
			String[] classPathURLs = System.getProperty("java.class.path").split(Character.toString(File.pathSeparatorChar));
			List<String> urlStrings = new ArrayList<String>();
			for (String url : classPathURLs) {
				urlStrings.add(url);
			}
			return urlStrings;
		} else if (J4RSystem.isCurrentJVMLaterThanThisVersion("1.8.0")) {
			Object urlClassPath = getURLClassPathObjectWithJava9to15Versions();
			Method met = urlClassPath.getClass().getMethod("getURLs");
			urls = (URL[]) met.invoke(urlClassPath);
		} else {
			URLClassLoader cl = (URLClassLoader) ClassLoader.getSystemClassLoader();
			urls = cl.getURLs();
		}
		List<String> urlStrings = new ArrayList<String>();
		for (URL url : urls) {
			urlStrings.add(url.toString());
		}
		return urlStrings;
	}

	
	
	/**
	 * Dynamically adds a directory or a JAR file to the class path. The JVM must implement
	 * the following option: --add-opens java.base/jdk.internal.loader=ALL-UNNAMED
	 * @param filename a String that stands for the filename.
	 * @throws Exception
	 */
	public static void addToClassPath(String filename) throws Exception {
		if (JavaGatewayServer.isPublicServerRunning()) {
			throw new SecurityException("The method addToClassPath is not accessible for public servers!");
		}
		File f = new File(filename);
		if (f.exists()) {
			URL thisURL = f.toURI().toURL();
			Object target;
			Class<?> targetClass;
			if (J4RSystem.isCurrentJVMLaterThanThisVersion("15.9")) {
				throw new GeneralSecurityException("Java " + getJVMVersion() + " does not support dynamic classpaths. The library should be loaded through the JVM classpath argument.");
			} else if (J4RSystem.isCurrentJVMLaterThanThisVersion("1.8.0")) {
				target = getURLClassPathObjectWithJava9to15Versions();
				targetClass = target.getClass();
			} else {
				target = ClassLoader.getSystemClassLoader();
				targetClass = target.getClass().getSuperclass();
			}
			Method met = targetClass.getDeclaredMethod("addURL", URL.class);
			met.setAccessible(true);
			met.invoke(target, thisURL);

		} else {
			throw new IOException("The file or directory " + filename + " does not exist!");
		}
	}
	
	
	
	private final static Object getURLClassPathObjectWithJava9to15Versions() throws ReflectiveOperationException {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		try {
			Field field = cl.getClass().getDeclaredField("ucp");
			field.setAccessible(true);
			return field.get(cl);
		} catch (ReflectiveOperationException e1) {
			Class clazz = Class.forName("java.lang.reflect.InaccessibleObjectException");
			if (clazz.isAssignableFrom(e1.getClass())) {
				throw new SecurityException("Java " + J4RSystem.jreVersion + " allows dynamic classpaths under certains conditions. You need to specify the JVM option --add-opens java.base/jdk.internal.loader=ALL-UNNAMED .");
			} 
			throw e1;
		} 
	}
	
	
}
