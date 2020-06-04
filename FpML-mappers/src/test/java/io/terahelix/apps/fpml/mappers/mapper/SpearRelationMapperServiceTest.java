package io.terahelix.apps.fpml.mappers.mapper;

import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;

import ColumnMapping.Definition;
import Org.FpML.Confirmation.FxFlexibleForward;
import Org.FpML.Confirmation.Relation.FxFlexibleForward_Relation;
import io.terahelix.apps.fpml.mapper.SpearRelationMapperService;
import io.terahelix.common.HelixLogger;
import io.terahelix.spear.javaRuntime.util.SpearHelpers;

/**
 * Some test cases for the mapper service.
 * @author jsteenkamp	
 *
 */
public class SpearRelationMapperServiceTest
{
	private static final HelixLogger logger = HelixLogger.getLogger(SpearRelationMapperServiceTest.class);
	
	@Test
	public void testGetRelationType() throws Exception
	{
		SpearRelationMapperService instance = SpearRelationMapperService.getInstance();
		Map<Class<?>, Class<?>> typeToRelationMap = instance.getTypeToRelationMap();
		typeToRelationMap.forEach((k,v) -> 	
		{
			logger.info(k.getName() + " \t -> \t" + v.getName());
		});
		
		Optional<Class<?>> targetRelation = instance.getRelationType(FxFlexibleForward.class);
		Assert.assertTrue(targetRelation.isPresent());
		Assert.assertEquals(FxFlexibleForward_Relation.class, targetRelation.get());
	}
	
	@Test
	public void testGenerateDefaultMapperFxFlexibleForward() throws Exception
	{
		SpearRelationMapperService instance = SpearRelationMapperService.getInstance();
		Definition defMaps = instance.getDefaultMappingDefinition(FxFlexibleForward_Relation.class);
		Assert.assertNotNull(defMaps);
		logger.info("\n" + SpearHelpers.toJson(defMaps, true));
	}
	
	
	
	@Test
	public void testGetRelationsToLevels() throws Exception
	{
		SpearRelationMapperService instance = SpearRelationMapperService.getInstance();
		Map<Integer, SortedSet<Class<?>>> levels = instance.getRelationsToLevels();
		levels.forEach((k,v) -> 
		{
			logger.info(k + " ---> " + v);
		});
	}
 
	
 
	

}






