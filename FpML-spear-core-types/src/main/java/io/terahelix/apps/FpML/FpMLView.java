package io.terahelix.apps.FpML;

/**
 * An enumeration of FpML views 
 * 
 * @author jsteenkamp
 *
 */
public enum FpMLView
{	
	CodeList("org.w3._2000._09.xmldsig_", "Org.W3", "FpML-model-codelist-spear-gen"),
	GeneriCode("org.genericode.xml._2004.ns.codelist._0", "Org.XML.GeneriCode", "FpML-model-codelist-spear-gen"),
	Confirmation("org.fpml.fpml_5.confirmation", "Org.FpML.Confirmation", "FpML-model-confirmation-spear-gen"),
	Legal("org.fpml.fpml_5.legal","Org.FpML.Legal", "FpML-model-legal-spear-gen"),
	PreTrade("org.fpml.fpml_5.pretrade", "Org.FpML.PreTrade", "FpML-model-pretrade-spear-gen"),
	RecordKeeping("org.fpml.fpml_5.recordkeeping",  "Org.FpML.RecordKeeping", "FpML-model-recordkeeping-spear-gen"),
	Reporting("org.fpml.fpml_5.reporting","Org.FpML.Reporting", "FpML-model-reporting-spear-gen" ),
	Transparency("org.fpml.fpml_5.transparency", "Org.FpML.Transparency", "FpML-model-transparency-spear-gen");
	
	private final String jaxbPackage;
	private final String spearPackage;
	private final String spearProjectName;
	
	private FpMLView(String jaxbPackage, 
	                 String spearPackage,
	                 String spearProjectName) 
	{
		this.jaxbPackage = jaxbPackage;
		this.spearPackage = spearPackage;
		this.spearProjectName = spearProjectName;
	}

	public String getJAXBPackage()
	{
		return jaxbPackage;
	}
	public String getSpearPackage()
	{
		return spearPackage;
	}
	
	public String getSpearProjectName()
	{
		return spearProjectName;
	}

	public static String getFullJAXBContext()
	{
		return getJAXBContext(CodeList,
		                  	GeneriCode,
		                	Confirmation,
		                	Legal,
		                	PreTrade,
		                	RecordKeeping,
		                	Reporting,
		                	Transparency);
	}
	
	public static String getJAXBContext(FpMLView...fpMLViews)
	{
		StringBuilder cxt = new StringBuilder();
		for (int i = 0; i < fpMLViews.length; i++)
		{
			if(i != 0) cxt.append(":");
			cxt.append(fpMLViews[i].getJAXBPackage());
		}
		return cxt.toString();
	}
	
}




