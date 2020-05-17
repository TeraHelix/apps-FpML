package io.terahelix.xsd.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import XSDModel.ComplexType;
import XSDModel.Member;
import XSDModel.XSDInstance;
import io.terahelix.common.HelixLogger;
import io.terahelix.xsd.spear.ExamplePackageMapper;

public class JAXB_XSD_ToModelConverterTest
{
	private static final HelixLogger logger = HelixLogger.getLogger(JAXB_XSD_ToModelConverter.class);

	@Test
	public void testCodeListXSDModelConverterTest() throws Exception 
	{
		final JAXB_XSDInstances_Model model = outputReports("../FpML-model/codelist/fpml-gen/src/main/resources/");
		
		Map<String, XSDInstance> instances = model.getInstances();
		Assert.assertTrue(instances.size() > 0);
		
		HashSet<XSDInstance> values = new HashSet<>(instances.values());
		Optional<ComplexType> complexType = model.findComplexTypeByName(values, "CodeListDocument");
		
		Assert.assertTrue(complexType.isPresent() == true);
	}
	
	@Test
	public void testConfirmationsListXSDModelConverterTest() throws Exception 
	{
		final JAXB_XSDInstances_Model model = outputReports("../FpML-model/confirmation/fpml-gen/src/main/resources/");
		
		Map<String, XSDInstance> instances = model.getInstances();
		Assert.assertTrue(instances.size() > 0);
	}
	
	private static JAXB_XSDInstances_Model outputReports(String path) throws Exception
	{
		Path root = Paths.get(path);
		final JAXB_XSDGroupsEnums_Model grpModel = JAXB_XSD_ToModelConverter.convertModel(root);
		String groupReport = grpModel.report();
		logger.info("\n" + groupReport);
		final JAXB_XSDInstances_Model model = JAXB_XSD_ToModelConverter.convertModel(grpModel, root);
		String isntancesReport = model.report();
		logger.info("\n" + isntancesReport);
		Map<String, XSDInstance> instances = model.getInstances();
		Assert.assertTrue(instances.size() > 0);
		return model;
	}
	
	@Test
	public void testConfirmationXSDModelConverterTest() throws Exception 
	{
		Path root = Paths.get("../FpML-model/confirmation/fpml-gen/src/main/resources/");
		
		final JAXB_XSDGroupsEnums_Model grpModel = JAXB_XSD_ToModelConverter.convertModel(root);
		final JAXB_XSDInstances_Model model = JAXB_XSD_ToModelConverter.convertModel(grpModel, root);
		
		Map<String, XSDInstance> instances = model.getInstances();
		Assert.assertTrue(instances.size() > 0);
		
		instances.forEach((k,v) ->
		{
			logger.info(k);
		});
		
		ExamplePackageMapper mapper = new ExamplePackageMapper();
		
		Assert.assertEquals("http://www.fpml.org/FpML-5/confirmation", mapper.getXSDNamespace("org.fpml.fpml_5.confirmation"));
		
		XSDInstance xsdInstance = instances.get("confirmation/fpml-com-5-11.xsd");
		Assert.assertNotNull(xsdInstance);
	
		Map<String, ComplexType> complexTypes = xsdInstance.getComplexTypes();
		
		complexTypes.forEach((k,v) -> 
		{
			logger.info("Complex Type : " + k);
		});
		
		ComplexType complexType = complexTypes.get("GasDeliveryPoint");
		
		Assert.assertNotNull(complexType);
		
		System.out.println("---------------- " + complexType.getName());
		
		Optional<String> optDoc = complexType.getDocumentation();
		
		Assert.assertTrue(optDoc.isPresent());
		
		System.out.println(optDoc.get());
		System.out.println();
		
		Map<String, Member> members = complexType.getMembers();
		
		members.forEach((k,v) -> 
		{
			System.out.println(k);
		});
		
		
		
	}
	
}


















