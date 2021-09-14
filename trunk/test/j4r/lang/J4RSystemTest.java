package j4r.lang;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import j4r.util.ObjectUtility;

public class J4RSystemTest {

	@Test
	public void addToClassPathSimpleTest1() throws Exception {
//		String pathToTest1 = ObjectUtility.getPackagePath(getClass()).replace("bin", "test") + "addurltest1";
		String pathToTest1 = ObjectUtility.getPackagePath(getClass()).replace("bin", "test") + "addurltest1";
		J4RSystem.addToClassPath(pathToTest1);
		try {
			Class clazz = Class.forName("hw.HelloWorldTest1");
			clazz.newInstance();
			System.out.println("Succeeded!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
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
		J4RSystem.addToClassPath(pathToTest2);
		try {
			Class clazz = Class.forName("hw2.HelloWorldTest2");
			clazz.newInstance();
			System.out.println("Succeeded!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}