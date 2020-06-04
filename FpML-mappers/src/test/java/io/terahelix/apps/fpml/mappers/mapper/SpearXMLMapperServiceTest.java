package io.terahelix.apps.fpml.mappers.mapper;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.github.javaparser.utils.Pair;

import Org.FpML.Confirmation.BusinessDayConventionEnum;
import Org.FpML.Confirmation.Product;
import Org.FpML.Confirmation.Trade;
import io.terahelix.apps.FpML.types.FpMLConfirmationExamplesType;
import io.terahelix.apps.FpML.types.FpMLExample;
import io.terahelix.apps.FpML.types.FpMLExamplesProvider;
import io.terahelix.apps.fpml.mapper.SpearRelationMapperService;
import io.terahelix.apps.fpml.mapper.SpearXMLMapperService;
import io.terahelix.apps.fpml.mapper.SpearXMLReflections;
import io.terahelix.common.HelixLogger;
import io.terahelix.spear.javaRuntime.SpearObject;
import io.terahelix.spear.javaRuntime.SpearStruct;
import io.terahelix.spear.javaRuntime.util.SpearClasses;
import io.terahelix.spear.javaRuntime.util.SpearHelpers;

public class SpearXMLMapperServiceTest
{
	private static final HelixLogger logger = HelixLogger.getLogger(SpearXMLMapperServiceTest.class);
	
	
	private final SpearXMLMapperService xmlService;
	private final SpearRelationMapperService relationService;
	
	public static final String FxEx27FlexibleTermForwardXml = "fx-ex27-flexible-term-forward.xml";
	
	public SpearXMLMapperServiceTest()
	{
		xmlService = SpearXMLMapperService.getInstance();
		relationService = SpearRelationMapperService.getInstance();
	}
	
	@Test @Ignore
	public void testCreateRelationsFromTradeEnvelope() throws Exception 
	{
		FpMLExample<FpMLConfirmationExamplesType> subject = FpMLExamplesProvider.getExamples(FpMLConfirmationExamplesType.FX_DERIVATIVES).stream().filter(e -> e.getName().equals(FxEx27FlexibleTermForwardXml)).findAny().get();
		Reader xmlInput = FpMLExamplesProvider.readExample(subject);
		SpearObject spearResult = xmlService.convertToSpearFromXML(xmlInput, Optional.of(subject.getFullPath()));
		
		List<SpearObject> allRelations = relationService.convertToProductRelations(spearResult, FxEx27FlexibleTermForwardXml);
		
		logger.info("Converted a total of " + allRelations.size() + " relations.");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void TestFxEx27FlexibleTermForwardXmlConversion() throws Exception 
	{	
		FpMLExample<FpMLConfirmationExamplesType> subject = FpMLExamplesProvider.getExamples(FpMLConfirmationExamplesType.FX_DERIVATIVES).stream().filter(e -> e.getName().equals(FxEx27FlexibleTermForwardXml)).findAny().get();
		Reader xmlInput = FpMLExamplesProvider.readExample(subject);
		SpearObject spearResult = xmlService.convertToSpearFromXML(xmlInput, Optional.of(subject.getFullPath()));
		Assert.assertTrue(spearResult.isComplete());
		logger.info("SUCCESS - Converted : " + subject + " -> \n\n");
		
		SpearStruct struct = (SpearStruct)spearResult;
		int tradeIndex = struct.$getIndexOfName("Trade");
		
		Optional<Object> tradeObject = struct.get(tradeIndex);
		List<Trade> item = (List<Trade>)tradeObject.get();
		
		Assert.assertEquals(1, item.size());
		
		Trade trade = item.get(0);
		Product product = trade.getProduct().get();
		
		logger.info("PRODUCT : \n " + SpearHelpers.toJson(product, true)); 
	}
	
	
	@Test
	public void testAssertCorrectMappingStruct() throws Exception 
	{
		Optional<Pair<Class<?>, Class<?>>> spearToJaxb = SpearXMLReflections.getSpearToJaxbClassFromSPT(Org.FpML.Confirmation.FxFlexibleForward.$GetSpearAppClass().getName());
		Assert.assertNotNull(spearToJaxb);
		Assert.assertTrue(spearToJaxb.isPresent());
	}
	
	@Test
	public void testAssertCorrectMappingEnum() throws Exception 
	{
		Optional<Pair<Class<?>, Class<?>>> spearToJaxb = SpearXMLReflections.getSpearToJaxbClassFromSPT(BusinessDayConventionEnum.class.getName());
		Assert.assertNotNull(spearToJaxb);
		Assert.assertTrue(spearToJaxb.isPresent());
	}
	
	@Test
	public void testAssertCorrectMappingGeneriCode() throws Exception 
	{
		Optional<Pair<Class<?>, Class<?>>> spearToJaxb = SpearXMLReflections.getSpearToJaxbClassFromSPT(Org.GeneriCode.CodeListDocument.class.getName());
		Assert.assertNotNull(spearToJaxb);
		Assert.assertTrue(spearToJaxb.isPresent());
	}
	
	@Test
	public void testAssertLoadingOfInnerClass() throws Exception 
	{
		String[] classes = new String[] 
		{
			"org.genericode.xml._2004.ns.codelist._0.KeyColumnRef$Annotation",
			"org.genericode.xml._2004.ns.codelist._0.Annotation"	
		};
		
		for(String clazz : classes)
		{
			Class<?> forName = SpearClasses.forName(clazz);
			Assert.assertNotNull(forName);
		}
		
	}
	
	@Test
	public void testAssertAnnotationEquivalent() throws Exception 
	{
		final Optional<Pair<Class<?>, Class<?>>> jaxbPairItem = SpearXMLReflections.getSpearToJaxbClassFromSPT(Org.GeneriCode.Annotation.class.getName());
		logger.info("JAXB Pair Item : " + jaxbPairItem);
		
		Assert.assertTrue(jaxbPairItem.isPresent() == true);
		
	}
	

	
	 
	
	
}















