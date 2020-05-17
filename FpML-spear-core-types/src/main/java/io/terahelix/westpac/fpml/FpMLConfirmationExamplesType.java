package io.terahelix.westpac.fpml;

import io.terahelix.apps.westpac.FpMLView;

/**
 * The different types of examples we have in this project.
 * 
 * @author jsteenkamp
 *
 */
public enum FpMLConfirmationExamplesType implements FpMLExamplesType, Comparable<FpMLConfirmationExamplesType>
{
	BOND_OPTIONS("products/bond-options"),
	COMMODITY_DERIVATIVES("products/commodity-derivatives"),
	CORRLATION_SWAPS("products/correlation-swaps"),
	CREDIT_DERIVATIVES("products/credit-derivatives"),
	DIVIDEND_SWAPS("products/dividend-swaps"),
	EQUITY_FORWARD("products/equity-forwards"),
	EQUITY_OPTIONS("products/equity-options"),
	EQUITY_SWAPS("products/equity-swaps"),
	FX_DERIVATIVES("products/fx-derivatives"),
	INFLATION_SWAPS("products/inflation-swaps"),
	INTERST_RATE_DERIVATIVES("products/interest-rate-derivatives"),
	LOANS("products/loan"),
	REPOS("products/repo"),
	SECURITIES("products/securities"),
	VARIANCE_SWAPS("products/variance-swaps"),
	VOLATILITY_SWAPS("products/volatility-swaps");

	private final String path;
	
	private FpMLConfirmationExamplesType(String pathP)
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
		return "confirmation";
	}

	@Override
	public FpMLView getFpmlView()
	{
		return FpMLView.Confirmation;
	}
	
}
