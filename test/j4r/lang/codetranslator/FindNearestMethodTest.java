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
package j4r.lang.codetranslator;

import org.junit.Assert;
import org.junit.Test;


public class FindNearestMethodTest {

	@Test
	public void testScoreForArrays() {
		FakeClass2[] obsArray = new FakeClass2[2];
		FakeClass[] refArray = new FakeClass[3];
		Class<?>[] refTypes = new Class<?>[] {refArray.getClass()};
		Class<?>[] obsTypes = new Class<?>[] {obsArray.getClass()};
		
		double score = REnvironment.doParameterTypesMatch(refTypes, obsTypes);
		Assert.assertEquals("Testing score", 1.0, score, 1E-8);
	}
	
}
