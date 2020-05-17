package io.terahelix.westpac.fpml;

import io.terahelix.apps.westpac.FpMLView;

/**
 * The different types of examples we have in this project.
 * 
 * @author jsteenkamp
 *
 */
public enum FpMLCodeListExamplesType implements FpMLExamplesType, Comparable<FpMLCodeListExamplesType>
{
	ALL("");

	private final String path;
	
	private FpMLCodeListExamplesType(String pathP)
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
		return "codelist";
	}
	@Override
	public FpMLView getFpmlView()
	{
		return FpMLView.CodeList;
	}
	
}
