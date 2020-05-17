package io.terahelix.apps.westpac;

/**
 * An enumeration of FpML views 
 * 
 * @author jsteenkamp
 *
 */
public enum FpMLView
{	
	CodeList("org.w3._2000._09.xmldsig_", "Org.W3", "westpac-model-codelist-spear-gen"),
	GeneriCode("org.genericode.xml._2004.ns.codelist._0", "Org.XML.GeneriCode", "westpac-model-codelist-spear-gen"),
	Confirmation("org.fpml.fpml_5.confirmation", "Org.FpML.Confirmation", "westpac-model-confirmation-spear-gen"),
	Legal("org.fpml.fpml_5.legal","Org.FpML.Legal", "westpac-model-legal-spear-gen"),
	PreTrade("org.fpml.fpml_5.pretrade", "Org.FpML.PreTrade", "westpac-model-pretrade-spear-gen"),
	RecordKeeping("org.fpml.fpml_5.recordkeeping",  "Org.FpML.RecordKeeping", "westpac-model-recordkeeping-spear-gen"),
	Reporting("org.fpml.fpml_5.reporting","Org.FpML.Reporting", "westpac-model-reporting-spear-gen" ),
	Transparency("org.fpml.fpml_5.transparency", "Org.FpML.Transparency", "westpac-model-transparency-spear-gen"),
	Example("au.com.westpac.example", " Au.Com.Westpac.Example", "westpac-model-example");
	
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




