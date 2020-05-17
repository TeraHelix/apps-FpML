package io.terahelix.apps.FpML.types;

import io.terahelix.apps.FpML.FpMLView;

/**
 * The different types of examples we have in this project.
 * 
 * @author jsteenkamp
 *
 */
public enum FpMLReportingExamplesType implements FpMLExamplesType, Comparable<FpMLReportingExamplesType>
{
	CASH_FLOW_MATCHING("cash-flow-matching"),
	COLLATERAL("collateral"),
	CREDIT_EVENT_NOTICE("credit-event-notice"),
	ENTITY_REPORTING("entity-reporting"),
	EXPOSURE("exposure"),
	PORTFOLIO_RECONCILIATION("portfolio-reconciliation"),
	POSITION_AND_ACTIVITY_REPORTING("position-and-activity-reporting"),
	RESET_REPORTING("reset-reporting"),
	SECURITIES("securities"),
	VALUATION("valuation");
	
	private final String path;
	
	private FpMLReportingExamplesType(String pathP)
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
		return "reporting";
	}
	
	@Override
	public FpMLView getFpmlView()
	{
		return FpMLView.Reporting;
	}
	
	
}
