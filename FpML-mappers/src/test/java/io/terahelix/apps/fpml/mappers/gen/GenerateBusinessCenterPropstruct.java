package io.terahelix.apps.fpml.mappers.gen;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** 
 * A utility class to generate the business center prop structs :
 * 
 * http://www.fpml.org/coding-scheme/business-center-7-27.xml
 * 
 * @author jsteenkamp
 *
 */
public class GenerateBusinessCenterPropstruct
{
	 
	@Test  
	public void testGeneratePropStructs() throws Exception
	{
		StringBuilder genPropStruct = new StringBuilder();
		genPropStruct.append("namespace Org.FpML.CodeList;" + "\n\n");
		
		genPropStruct.append("propstruct BusinessCenter\n{\n");
		
		genPropStruct.append("  Code 						string \n");
		genPropStruct.append("  Source 						string \n");
		genPropStruct.append("  Description 				string \n");
		
 
		/*
		    propinst NewOrderEventType 
		    {
		        Name	 		=            "New Order Event";
		        Mnemonic 		=   		 "MENO";
		        Description		=      		 "Reported when an Industry Member originates an order, receives a customer order, originates a bunched, representative or proprietary order, or receives an order from a non-reporting foreign entity. ";
		    }
		*/
		
		
		File fXmlFile = new File("src/test/resources/fpml/business-center-7-27.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		
		Element el = (Element)doc.getElementsByTagName("SimpleCodeList").item(0);
		
		NodeList rows = el.getChildNodes();
		int len = rows.getLength();
		
		
		for (int i = 0; i < len; i++)
		{
			Node item = rows.item(i);
			if(item instanceof Element == false)
			{
				continue;
			}
			
			Element row = (Element)item;
			NodeList simpleValues = row.getElementsByTagName("SimpleValue");
			
			String v1 = ((Element)simpleValues.item(0)).getTextContent();
			String v2 = ((Element)simpleValues.item(1)).getTextContent();
			String v3 = ((Element)simpleValues.item(2)).getTextContent();
			
			genPropStruct.append("  propinst " + v1 + "\n");
			genPropStruct.append("  {\n");
			genPropStruct.append("     Code 					= 			\"" + v1 + "\"\n");
			genPropStruct.append("     Source 					= 			\"" + v2 + "\"\n");
			genPropStruct.append("     Description				= 			\"" + v3 + "\"\n");
			genPropStruct.append("  }\n");
			
			
			System.out.println(row);
			
		}
		
		
		genPropStruct.append("}\n");
		
		System.out.println(genPropStruct);
	}
	

}



















