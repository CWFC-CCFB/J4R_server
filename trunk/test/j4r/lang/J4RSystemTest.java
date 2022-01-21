package j4r.lang;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import j4r.util.ObjectUtility;

public class J4RSystemTest {

	@Test
	public void addToClassPathSimpleTest1() throws Exception {
//		String pathToTest1 = ObjectUtility.getPackagePath(getClass()).replace("bin", "test") + "addurltest1";
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

}