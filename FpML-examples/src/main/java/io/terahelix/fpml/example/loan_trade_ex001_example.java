package io.terahelix.fpml.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import Org.FpML.Confirmation.LoanTradeNotification;
import io.terahelix.apps.fpml.mapper.SpearXMLMapperService;
import io.terahelix.spear.javaRuntime.util.SpearHelpers;

/**
 * A simple example demonstrating the loan_trade_ex001.xml file. 
 * 
 * @author jsteenkamp
 *
 */
public class loan_trade_ex001_example 
{
	final static Path FILE = Paths.get("../FpML-model/confirmation/fpml-gen/src/main/resources/FpML/confirmation/products/loan/loan_trade_ex001.xml"); 
	final static SpearXMLMapperService mapper = SpearXMLMapperService.getInstance();
	
	public static void main(String... args) throws Exception 
	{
		final LoanTradeNotification loanNotf = mapper.convertToSpearFromXML(FILE);
		
		Files.writeString(Paths.get("target/LoanTradeNotification.yaml"), SpearHelpers.toYAML(loanNotf));
		Files.writeString(Paths.get("target/LoanTradeNotification.json"), SpearHelpers.toJson(loanNotf, true));
		
	}
	
}
