package io.terahelix.apps.FpML;

import java.util.Optional;

import XSDModel.ComplexType;
import XSDModel.GroupMember;
import XSDModel.Member;
import XSDModel.XSDEnum;
import XSDModel.XSDEnumMember;
import io.terahelix.xsd.spear.DocAndSourceLinksProvider;

public class FpMLDocAndSourceLinksProvider implements DocAndSourceLinksProvider 
{
	
	///m_spearSource.append(String.format("@FpMLDoc(\"%s\")\n", parent.constructExternalDocumentationAnnotation(enumType)));
	public static final String BASE_DOCS_URL = "https://westpac.th-labs.online/source/xref/";

	public Optional<String> constructSourceLinkURL(String xsdFile, int lineNumber)
	{
		String pathContext = xsdFile.substring(0, xsdFile.indexOf("/"));
		String prefixPath = "westpac-model-" + pathContext + "-fpml/FpML/" + xsdFile;
		return Optional.of(BASE_DOCS_URL + prefixPath + "#" + lineNumber);
	}
	
	public Optional<String> constructExternalDocumentationAnnotation(ComplexType complexType)
	{
		StringBuilder url = new StringBuilder();
		url.append("https://www.fpml.org/spec/fpml-5-11-5-tr-1/html/");
		String xsdFile = complexType.getParent().getXSDFile();
		String dir = xsdFile.substring(0, xsdFile.indexOf("/"));
		String fileName = xsdFile.substring(xsdFile.indexOf("/") + 1).replace('.', '_');
		url.append(dir);
		url.append("/schemaDocumentation/schemas/");
		url.append(fileName);
		url.append("/complexTypes/" + complexType.getName() + ".html");
		return Optional.of("@FpMLDoc(\"" + url.toString() + "\")");
	}
	
	public Optional<String> constructExternalDocumentationAnnotation(ComplexType complexType, Member complexMember)
	{
		StringBuilder url = new StringBuilder();
		url.append("https://www.fpml.org/spec/fpml-5-11-5-tr-1/html/");
		String xsdFile = complexMember.getXSDFile();
		String dir = xsdFile.substring(0, xsdFile.indexOf("/"));
		String fileName = xsdFile.substring(xsdFile.indexOf("/") + 1).replace('.', '_');
		url.append(dir);
		url.append("/schemaDocumentation/schemas/");
		url.append(fileName);
		
		if(complexMember.isValidOriginGroupMember() == true)
		{
			GroupMember originGroupMember = complexMember.getOriginGroupMember().get();
			url.append("/groups/" + originGroupMember.getParent().getName() + "/" + complexMember.getName() + ".html" );
		}
		else
		{
			url.append("/complexTypes/" + complexType.getName() + "/" + complexMember.getName() + ".html" );
		}
		
		return Optional.of("@FpMLDoc(\"" + url.toString() + "\")");
	}
	
	public Optional<String> constructExternalDocumentationAnnotation(XSDEnum enumType)
	{
		StringBuilder url = new StringBuilder();
		url.append("https://www.fpml.org/spec/fpml-5-11-5-tr-1/html/");
		String xsdFile = enumType.getParent().getXSDFile();
		String dir = xsdFile.substring(0, xsdFile.indexOf("/"));
		String fileName = xsdFile.substring(xsdFile.indexOf("/") + 1).replace('.', '_');
		url.append(dir);
		url.append("/schemaDocumentation/schemas/");
		url.append(fileName);
		url.append("/simpleTypes/" + enumType.getName() + ".html");
		return Optional.of("@FpMLDoc(\"" + url.toString() + "\")");
	}
	
	public Optional<String> constructExternalDocumentationAnnotation(	XSDEnum enumType,
	                                                 					XSDEnumMember member)
	{
		StringBuilder url = new StringBuilder();
		url.append("https://www.fpml.org/spec/fpml-5-11-5-tr-1/html/");
		String xsdFile = enumType.getParent().getXSDFile();
		String dir = xsdFile.substring(0, xsdFile.indexOf("/"));
		String fileName = xsdFile.substring(xsdFile.indexOf("/") + 1).replace('.', '_');
		url.append(dir);
		url.append("/schemaDocumentation/schemas/");
		url.append(fileName);
		url.append("/simpleTypes/" + enumType.getName() + ".html" );
		return Optional.of("@FpMLDoc(\"" + url.toString() + "\")");
	}
	
	
	
}
