/*
 * This file is part of the j4r library.
 *
 * Copyright (C) 2020-2023 His Majesty the King in right of Canada
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
package j4r.lang.codetranslator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class FindNearestMethodTest {

	@Test
	public void scoreForArraysTest() {
		FakeClass2[] obsArray = new FakeClass2[2];
		FakeClass[] refArray = new FakeClass[3];
		Class<?>[] refTypes = new Class<?>[] {refArray.getClass()};
		Class<?>[] obsTypes = new Class<?>[] {obsArray.getClass()};
		
		double score = REnvironment.doParameterTypesMatch(refTypes, obsTypes);
		Assert.assertEquals("Testing score", 1.0, score, 1E-8);
	}
	
	@Test 
	public void findNearestConstructorTest() throws NoSuchMethodException {
		List<Class<?>> parms1 = new ArrayList<Class<?>>();
		parms1.add(int.class);
		Constructor c1 = REnvironment.findNearestConstructor(ArrayList.class, parms1);
		
		List<Class<?>> parms2 = new ArrayList<Class<?>>();
		parms2.add(Integer.class);
		Constructor c2 = REnvironment.findNearestConstructor(ArrayList.class, parms2);
		Assert.assertEquals("Testing if the two constructors are the same", c1, c2);
	}
	
}
