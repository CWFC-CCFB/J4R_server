/*
 * This file is part of the j4r library.
 *
 * Copyright (C) 2020-2022 Her Majesty the Queen in right of Canada
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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import j4r.util.ObjectUtility;

public class J4RSystemTest {

	@Test
	public void addToClassPathSimpleTest1() throws Exception {
		String pathToTest1 = ObjectUtility.getPackagePath(getClass()).replace("bin", "test") + "addurltest1";
		try {
			J4RSystem.addToClassPath(pathToTest1);
			Class clazz = Class.forName("hw.HelloWorldTest1");
			clazz.newInstance();
			System.out.println("Succeeded!");
		} catch (GeneralSecurityException e1) {
			if (J4RSystem.isCurrentJVMLaterThanThisVersion("15.9")) {
				return;	// Assert succeeded
			} else {
				e1.printStackTrace();
				Assert.fail();
			}
		} catch (ClassNotFoundException e2) {
			e2.printStackTrace();
			Assert.fail();
		} 
	}
	
	@Test
	public void getURLsFromClassPath() throws Exception {
		List<String> list = J4RSystem.getClassPathURLs();
		Assert.assertTrue("list size", list.size() > 0);
	}

	@Test
	public void addToClassPathSimpleTest2() throws Exception {
		String pathToTest2 = ObjectUtility.getPackagePath(getClass()).replace("bin", "test") + "addurltest2" + File.separator + "helloworldtest2.jar";
		try {
			J4RSystem.addToClassPath(pathToTest2);
			Class clazz = Class.forName("hw2.HelloWorldTest2");
			clazz.newInstance();
			System.out.println("Succeeded!");
		} catch (GeneralSecurityException e1) {
			if (J4RSystem.isCurrentJVMLaterThanThisVersion("15.9")) {
				return;	// Assert succeeded
			} else {
				e1.printStackTrace();
				Assert.fail();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void checkPatternsTest() throws Exception {
		List<String> patterns = new ArrayList<String>();
		patterns.add("junit");
		patterns.add("hamcrest");
		patterns.add("myLibrary1");
		patterns.add("myLibrary2");
		List<String> urls = J4RSystem.getClassPathURLs();
		List<String> patternsNotFound = J4RSystem.checkIfPatternsAreInClassPath(patterns);
		Assert.assertEquals("Checking first output", "myLibrary1", patternsNotFound.get(0));
		Assert.assertEquals("Checking second output", "myLibrary2", patternsNotFound.get(1));
	}

}