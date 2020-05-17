package io.terahelix.westpac.fpml;

import io.terahelix.apps.westpac.FpMLView;

/**
 * The different types of examples we have in this project.
 * 
 * @author jsteenkamp
 *
 */
public enum FpMLRecordKeepingExamplesType implements FpMLExamplesType, Comparable<FpMLRecordKeepingExamplesType>
{
	EVENTS("events"),
	FLAT_PRODUCT("flat-product"),
	FULL_PRODUCT("full-product"),
	NEW_EVENTS("new-events"),
	PACKAGES("packages"),
	PRODUCTS("products"),
	SAMPLES("samples"),
	SCRIPTS("scripts"),
	USE_CASES("use-cases");

	private final String path;
	
	private FpMLRecordKeepingExamplesType(String pathP)
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
		return "recordkeeping";
	}
	
	@Override
	public FpMLView getFpmlView()
	{
		return FpMLView.RecordKeeping;
	}
	
}
