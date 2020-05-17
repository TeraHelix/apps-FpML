package io.terahelix.apps.FpML.types;

import io.terahelix.apps.FpML.FpMLView;

/**
 * The different types of examples we have in this project.
 * 
 * @author jsteenkamp
 *
 */
public enum FpMLTransparencyExamplesType implements FpMLExamplesType, Comparable<FpMLTransparencyExamplesType>
{
	GEN_PRODUCT_BOND_OPTIONS("generated-products/bond-options"),
	GEN_PRODUCT_COMMODITY_DERIVATIVES("generated-products/commodity-derivatives"),
	GEN_PRODUCT_CREDIT_DERIVATIVES("generated-products/credit-derivatives"),
	GEN_PRODUCT_DIVIDEND_SWAPS("generated-products/dividend-swaps"),
	GEN_PRODUCT_EQUITY_FORWARDS("generated-products/equity-forwards"),
	GEN_PRODUCT_EQUITY_OPTIONS("generated-products/equity-options"),
	GEN_PRODUCT_EQUITY_SWAPS("generated-products/equity-swaps"),
	GEN_PRODUCT_FX_DERIVATIVES("generated-products/fx-derivatives"),
	GEN_PRODUCT_INFLATION_SWAPS("generated-products/inflation-swaps"),
	GEN_PRODUCT_INTEREST_RATE_DERIVATIVES_SWAPS("generated-products/interest-rate-derivatives"),
	GEN_PRODUCT_VARIANCE_SWAPS("generated-products/variance-swaps"),
	MESSAGES("messages"),
	NEW("new"),
	PACKAGE("package"),
	PRODUCTS("products"),
	USE_CASES_PUBLIC("use-cases-public");
	
	private final String path;
	
	private FpMLTransparencyExamplesType(String pathP)
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
		return "transparency";
	}
	
	@Override
	public FpMLView getFpmlView()
	{
		return FpMLView.Transparency;
	}
	
	
}
