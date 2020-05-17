package io.terahelix.westpac.fpml;

import java.util.List;

import io.terahelix.apps.westpac.FpMLView;

/**
 * The interface that all FpMLExamplesType should implement
 * 
 * @author jsteenkamp
 *
 */
public interface FpMLExamplesType 
{
	public static final String INDEX_FILE_RESOURCE_PATTERN = "/FpML/%s/FpML-Resources-Index.txt";
	
	/**
	 * All the implementations 
	 */
	public static final List<Class<? extends FpMLExamplesType>> ALL_IMPLS = List.of(FpMLCodeListExamplesType.class, 
	                                                                                FpMLConfirmationExamplesType.class,
	                                                                                FpMLLegalExamplesType.class,
	                                                                                FpMLPreTradeExamplesType.class,
	                                                                                FpMLRecordKeepingExamplesType.class,
	                                                                                FpMLReportingExamplesType.class,
	                                                                                FpMLTransparencyExamplesType.class);
	
	/**
	 * The root path of the resources - such that String.format(INDEX_FILE_RESOURCE_PATTERN, <ResourcesRootPath>) is equal to the resources index
	 * 
	 * @return
	 */
	public String getResourcesRootPath();
	
	/**
	 * Gets the path associated with this example.
	 *  
	 * @return
	 */
	public String getPath();
	
	
	/**
	 * The FpML view we are associated with .
	 * 
	 * @return
	 */
	public FpMLView getFpmlView();
	
	
}
