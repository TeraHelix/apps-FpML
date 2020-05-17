package io.terahelix.xsd.spear;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.terahelix.apps.westpac.FpMLView;
import io.terahelix.common.HelixRuntimeException;

/**
 * A hard coded example package mapper.
 * 
 */
public class ExamplePackageMapper implements PackageMapper
{
	public String getPackage(String p)
	{
		FpMLView[] vs = FpMLView.values();
		for (int i = 0; i < vs.length; i++)
		{
			if(p.startsWith(vs[i].getJAXBPackage())) return vs[i].getSpearPackage();
		}
		throw new HelixRuntimeException("Could not map the package : " + p);
	}

	@Override
	public boolean isTargetMappedItem(String undersDef)
	{
		boolean matched = false;
		
		FpMLView[] vs = FpMLView.values();
		for (int i = 0; i < vs.length; i++)
		{
			if(undersDef.startsWith(vs[i].getSpearPackage())) 
			{
				matched = true;
				break;
			}
		}
		return matched; 
	}

	@Override
	public boolean shouldGenerateFile(Class<?> inputClass)
	{
		return true;
	}

	@Override
	public String getXSDNamespace(String inputPackage)
	{
		if(inputPackage.startsWith("org.fpml.fpml_5.confirmation"))
		{
			return "http://www.fpml.org/FpML-5/confirmation";
		}
		if(inputPackage.startsWith("org.fpml.fpml_5.legal"))
		{
			return "http://www.fpml.org/FpML-5/legal";
		}
		if(inputPackage.startsWith("org.fpml.fpml_5.pretrade"))
		{
			return "http://www.fpml.org/FpML-5/pretrade";
		}
		if(inputPackage.startsWith("org.fpml.fpml_5.recordkeeping"))
		{
			return "http://www.fpml.org/FpML-5/recordkeeping";
		}
		if(inputPackage.startsWith("org.fpml.fpml_5.reporting"))
		{
			return "http://www.fpml.org/FpML-5/reporting";
		}
		if(inputPackage.startsWith("org.fpml.fpml_5.transparency"))
		{
			return "http://www.fpml.org/FpML-5/transparency";
		}
		if(inputPackage.startsWith("org.genericode.xml"))
		{
			return "http://xml.genericode.org/2004/ns/CodeList/0.2/";
		}
		if(inputPackage.startsWith("org.w3"))
		{
			return "http://www.w3.org/2001/XMLSchema";  
		}
		throw new HelixRuntimeException("Could not map the package to a namespace : " + inputPackage);
	}

	public Path getOutputPath(Class<?> k, String spearSourceCodeDirectory)
	{
		String pkg = k.getPackageName();
		final String flName = CodeUtils.getClassOutputFileName(k);
		
		if(pkg.startsWith("org.w3") == true)
		{
			return Paths.get("target/FpML/OrgW3/" + flName);
		}
		if(pkg.startsWith("org.genericode.xml") == true)
		{
			return Paths.get("target/FpML/OrgGenericCode/" + flName);
		}
		if(pkg.startsWith("org.fpml.fpml_5.confirmation") == true)
		{
			return Paths.get("target/FpML/Confirmation/" + flName);
		}
		if(pkg.startsWith("org.fpml.fpml_5.reporting") == true)
		{
			return Paths.get("target/FpML/Reporting/" + flName);
		}
		if(pkg.startsWith("org.fpml.fpml_5.pretrade") == true)
		{
			return Paths.get("target/FpML/PreTrade/" + flName);
		}
		if(pkg.startsWith("org.fpml.fpml_5.transparency") == true)
		{
			return Paths.get("target/FpML/Transparency/" + flName);
		}
		if(pkg.startsWith("org.fpml.fpml_5.recordkeeping") == true)
		{
			return Paths.get("target/FpML/RecordKeeping/" + flName);
		}
		if(pkg.startsWith("org.fpml.fpml_5.legal") == true)
		{
			return Paths.get("target/FpML/Legal/" + flName);
		}
		
		throw new HelixRuntimeException("Could not map the target class to output  : " + k);
	}
}
