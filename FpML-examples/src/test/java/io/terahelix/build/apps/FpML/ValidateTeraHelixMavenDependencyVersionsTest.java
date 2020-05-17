package io.terahelix.build.apps;

import java.nio.file.Path;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.terahelix.build.ValidateTeraHelixMavenDependencyVersions;

public class ValidateTeraHelixMavenDependencyVersionsTest
{
	@Test
	public void testGetAllPOMs() throws Exception
	{
		Path rootPom = TeraHelixMavenVersionsValid.ROOT_POM;
		List<Path> allFiles = ValidateTeraHelixMavenDependencyVersions.getAllInScopePOMFiles(rootPom);

		allFiles.forEach(p -> {
			Assert.assertTrue(p.toAbsolutePath().toString() + " is dodgy, it is in target. Should not contain : " + ValidateTeraHelixMavenDependencyVersions.TARGET_DIR + " | " ,
							  p.toAbsolutePath().toString().contains("target") == false);

		});

		Assert.assertFalse(allFiles.contains(rootPom));
	}


}
