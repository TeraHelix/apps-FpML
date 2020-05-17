package io.terahelix.build.apps.FpML;

import java.util.List;

import org.junit.Test;

import io.terahelix.build.TeraHelixModules;
import io.terahelix.build.TeraHelixModules.ShortModuleDescription;

public class TeraHelixModulesTest
{

	@Test
	public void testGetAllModules()
	{
		TeraHelixModules mod =  new TeraHelixModules(TeraHelixMavenVersionsValid.ROOT_POM);
		List<ShortModuleDescription> mods = mod.getModules();
		
		for (ShortModuleDescription sd : mods)
		{
			System.out.println(sd.getMavenDependencyManagementString());
			System.out.println(); 
		}
		
	 
	}
	
}
