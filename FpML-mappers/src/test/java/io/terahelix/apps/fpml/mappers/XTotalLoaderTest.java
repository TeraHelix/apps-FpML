package io.terahelix.apps.fpml.mappers;

import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import Org.FpML.Confirmation.Product;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import io.terahelix.apps.FpML.types.FpMLConfirmationExamplesType;
import io.terahelix.apps.FpML.types.FpMLExample;
import io.terahelix.apps.FpML.types.FpMLExamplesProvider;
import io.terahelix.apps.FpML.types.FpMLExamplesType;
import io.terahelix.apps.fpml.mapper.SpearRelationMapperService;
import io.terahelix.apps.fpml.mapper.SpearXMLMapperService;
import io.terahelix.common.HelixLogger;
import io.terahelix.spear.javaRuntime.SpearObject;

/**
 * Roundtrip of all the types.
 * 
 * @author jsteenkamp
 *
 */
public class XTotalLoaderTest
{
	private static final HelixLogger logger = HelixLogger.getLogger(XTotalLoaderTest.class);
	
	private final SpearXMLMapperService xmlService;
	private final SpearRelationMapperService relationService;
	
	public XTotalLoaderTest()
	{
		xmlService = SpearXMLMapperService.getInstance();
		relationService = SpearRelationMapperService.getInstance();
	}
	
	private static class ExampleReport
	{
		public String description;
		public int totalParseAttempted;
		public int totalParseSuccesses;
		
		public int totalMissingRelation;
		public int totalNoProductRelation;
		public int totalRelationAttempted;
		public int totalRelationSucceeded;
		
		public double getParsePercentage()
		{
			return (((double)totalParseSuccesses) / totalParseAttempted) * 100.0;
		}

		public double getRelationPercentage()
		{
			return (((double)totalRelationSucceeded) / totalRelationAttempted) * 100.0;
		} 
	}
	
	
	@Test  @Ignore // Tasos ignored

	public void testAllCodeListExampleTypes() throws Exception 
	{
		final List<Class<? extends FpMLExamplesType>> exampleTypes = FpMLExamplesType.ALL_IMPLS;
		
		int sucess = 0;
		int failure = 0;
		
		for(Class<? extends FpMLExamplesType> exampleType : exampleTypes) 
		{
			Method allEnumValues = exampleType.getDeclaredMethod("values");
			FpMLExamplesType[] result = (FpMLExamplesType[])allEnumValues.invoke(null);

			for (int j = 0; j < result.length; j++)
			{
				FpMLExamplesType exampleTypeEnum = result[j];
				final List<FpMLExample<FpMLExamplesType>> allTypeExamples = FpMLExamplesProvider.getExamples(exampleTypeEnum);
				
				logger.info("Reading a Total of " + allTypeExamples.size() + " examples for - " + exampleTypeEnum + " (" + exampleType.getName() + ")" );
				for(FpMLExample<FpMLExamplesType> exampleInstance : allTypeExamples)
				{
					Reader xmlInput = FpMLExamplesProvider.readExample(exampleInstance);
					
					logger.info("Converting : " + exampleInstance );
					try
					{
						SpearObject spearResult = xmlService.convertToSpearFromXML(xmlInput, Optional.of(exampleInstance.getFullPath()));
						Assert.assertTrue(spearResult.isComplete());
						sucess++;
					} 
					catch (Exception e)
					{
						logger.error("FAIL : " + e, e);
						failure++;
					}
					
				}
			}
		}
		
		logger.info("\n\nSUCCESS : " + sucess + " | FAILURE : " + failure );
		
	}
	
	@Test @Ignore // Tasos ignored
	public void testAllConfirmationTypes() throws Exception 
	{
		final FpMLConfirmationExamplesType[] values = FpMLConfirmationExamplesType.values();
		
		final Map<FpMLConfirmationExamplesType, List<FpMLExample>> parseFailures = new HashMap<>();
		final Map<FpMLConfirmationExamplesType, List<FpMLExample>> relationFailures = new HashMap<>();
		final Map<String, String> relationSucesses = new TreeMap<>();
		
		final TreeMap<FpMLConfirmationExamplesType, ExampleReport> testReports = new TreeMap<>();
		
		for(FpMLConfirmationExamplesType t : values)
		{			
			List<FpMLExample<FpMLConfirmationExamplesType>> examples = FpMLExamplesProvider.getExamples(t);
			//examples = examples.stream().filter(p -> p.getName().equals("ird-ex29-non-deliverable-settlement-swap.xml")).collect(Collectors.toList());
			
			logger.info("Reading a Total of " + examples.size() + " examples for - " + t);
			
			ExampleLoop : for(FpMLExample example : examples)
			{
				final ExampleReport exReport = testReports.computeIfAbsent(t, x -> 
				{
					ExampleReport ex = new ExampleReport();
					ex.description = x.getPath();
					return ex;
				});
				
				exReport.totalParseAttempted++;
				SpearObject spearResult = null;
				try
				{
					Reader xmlInput = FpMLExamplesProvider.readExample(example);
					spearResult = xmlService.convertToSpearFromXML(xmlInput, Optional.of(example.getFullPath()));
					Assert.assertTrue(spearResult.isComplete());
					exReport.totalParseSuccesses++;
				} 
				catch (Exception e)
				{
					parseFailures.computeIfAbsent(t, s -> new ArrayList<>()).add(example);
					continue ExampleLoop;
				}
				
				//Now the Relation
				List<Product> products = relationService.extractProducts(spearResult, example.getName());
				if(products.isEmpty() == true)
				{
					exReport.totalNoProductRelation++;
				}
				else
				{
					Optional<Class<?>> relationTypeOpt = relationService.getProductRelationType(products);
					if(relationTypeOpt.isPresent() == true)
					{
						exReport.totalRelationAttempted++;
						try
						{
							List<SpearObject> covertedRelations = relationService.convertToProductRelations(spearResult, example.getName());
							for (SpearObject relation : covertedRelations)
							{
								Assert.assertTrue(relation.isComplete());
							}
							relationSucesses.put(example.getName(), products.get(0).$getSpearAppClass().getName());
							exReport.totalRelationSucceeded++;
						}
						catch (Exception e)
						{
							relationFailures.computeIfAbsent(t, s -> new ArrayList<>()).add(example);
						}
					}
					else
					{
						exReport.totalMissingRelation++;	
					}
				}
			}
		}
		
		logger.info("\n\n---------------------------------\n PARSE FAILURES\n---------------------------------\n");
		parseFailures.forEach((k,v) -> 
		{
			v.forEach(logger::info);
		});
		logger.info("\n\n-----------------------------------------------------------------\n");
		
		logger.info("\n\n---------------------------------\n RELATION FAILURES\n---------------------------------\n");
		parseFailures.forEach((k,v) -> 
		{
			v.forEach(logger::info);
		});
		logger.info("\n\n-----------------------------------------------------------------\n\n\n\n");
		
		
		System.out.println("HashMap<String, String> workingName = new HashMap<>();");
		relationSucesses.forEach((k,v) -> 
		{
			
			System.out.println("workingName.put(\"" + k + "\",\"" + v +"\");");
		});
		
		System.out.println();
		
		//Ok, now create a table output.
		final AsciiTable at = new AsciiTable();
		at.addRule();
		
		at.addRow("Confirmation Descr.", "Parse Attempts", "Parse Success", "Parse %", "No Product Relation", "Missing Relations", "Relation Attempts", "Relation Success", "Relation %" );
		at.addRule();
		
		testReports.forEach((k,v) -> 
		{
			at.addRow(	v.description,
						v.totalParseAttempted,
						v.totalParseSuccesses,
						v.getParsePercentage(),
						v.totalNoProductRelation,
						v.totalMissingRelation,
						v.totalRelationAttempted,
						v.totalRelationSucceeded,
						Double.isNaN(v.getRelationPercentage()) ? "" : v.getRelationPercentage());
		});
		
 
		at.addRule();
		
		CWC_LongestLine cwc = new CWC_LongestLine();
		at.getRenderer().setCWC(cwc);
		
		logger.info("\n" + at.render());
		
	}
 
	
}

























