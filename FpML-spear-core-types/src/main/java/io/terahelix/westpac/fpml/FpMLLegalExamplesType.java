package io.terahelix.westpac.fpml;

import io.terahelix.apps.westpac.FpMLView;

/**
 * The different types of examples we have in this project.
 * 
 * @author jsteenkamp
 *
 */
public enum FpMLLegalExamplesType implements FpMLExamplesType, Comparable<FpMLLegalExamplesType>
{
	DOCUMENT("legal-document");

	private final String path;
	
	private FpMLLegalExamplesType(String pathP)
	{
		path = pathP;
	}
	
	@Override
	public String getPath()
	{
		return path;
	}

	@Override
	public String getResourcesRootPath()
	{
		return "legal";
	}
	
	@Override
	public FpMLView getFpmlView()
	{
		return FpMLView.Legal;
	}
	
}
