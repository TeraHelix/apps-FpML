package io.terahelix.apps.FpML.types;

import io.terahelix.apps.FpML.FpMLView;

/**
 * The different types of examples we have in this project.
 * 
 * @author jsteenkamp
 *
 */
public enum FpMLPreTradeExamplesType implements FpMLExamplesType, Comparable<FpMLPreTradeExamplesType>
{
	CLEARING("clearing"),
	LIMIT_CHECK("limit-check"),
	PRODUCT_DEFINITIONS("product-definitions"),
	REG_REPORTING("reg-reporting"),
	PRODUCT_CREDIT_DERIVATIVES("products/credit-derivatives"),
	PRODUCT_INTEREST_RATE_DERIVATIVES("products/interest-rate-derivatives");

	private final String path;
	
	private FpMLPreTradeExamplesType(String pathP)
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
		return "pretrade";
	}
	
	@Override
	public FpMLView getFpmlView()
	{
		return FpMLView.PreTrade;
	}
	
}
