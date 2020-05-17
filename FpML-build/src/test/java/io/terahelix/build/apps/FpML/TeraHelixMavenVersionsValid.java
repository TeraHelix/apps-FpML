package io.terahelix.build.apps.FpML;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import io.terahelix.build.ValidateTeraHelixMavenDependencyVersions;

/**
 * A test case to ensure that all the Terahelix maven versions are valid.
 */
public class TeraHelixMavenVersionsValid
{
	
	public static final Path ROOT_POM = Paths.get("../pom.xml");  
	
	@Test
	public void testMavenVersions() throws Exception
	{
	
		try
		{
			ValidateTeraHelixMavenDependencyVersions.throwIfProjectNotValid(ROOT_POM);
		} 
		catch (Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
