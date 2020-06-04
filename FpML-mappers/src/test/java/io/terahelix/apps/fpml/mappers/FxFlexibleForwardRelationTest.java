package io.terahelix.apps.fpml.mappers;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;

import Org.FpML.Confirmation.FxFlexibleForward;
import Org.FpML.Confirmation.Relation.FxFlexibleForward_Relation;
import Org.FpML.Confirmation.Relation.FxFlexibleForward_Relation_Builder;
import io.terahelix.apps.fpml.mapper.SpearRelationMapperService;
import io.terahelix.common.HelixLogger;
import io.terahelix.spear.javaRuntime.SpearObject;
import io.terahelix.spear.javaRuntime.SpearStruct;
import io.terahelix.spear.javaRuntime.SpearStructMerger;
import io.terahelix.spear.javaRuntime.util.SpearHelpers;

/**
 * A simple test case for the flexible forward relation 
 * 
 * @author jsteenkamp
 *
 */
public class FxFlexibleForwardRelationTest
{
	private static final HelixLogger logger = HelixLogger.getLogger(FxFlexibleForwardRelationTest.class);
	
	private final SpearRelationMapperService mapper;
	
	public FxFlexibleForwardRelationTest()
	{
		mapper = SpearRelationMapperService.getInstance();
	}
	
	@Test @Ignore
	public void testRelationFullyQualified() throws Exception
	{
		byte[] bts= Files.readAllBytes(Path.of("src/test/resources/fpml/test/Sample-FxFlexibleForward.json"));
		FxFlexibleForward flexForward = SpearHelpers.fromJson(bts);
		FxFlexibleForward_Relation_Builder flexForwardRelation = FxFlexibleForward_Relation.Create();
		SpearStructMerger merger = flexForwardRelation.getMerger();

		merger.assign( flexForward );

		SpearStruct result = flexForwardRelation.build();

		logger.info("Result : \n"  + SpearHelpers.toJson(result, true));
	}

	@Test @Ignore
	public void testRelation() throws Exception
	{
		byte[] bts= Files.readAllBytes(Path.of("src/test/resources/fpml/test/Sample-FxFlexibleForward.json"));
		FxFlexibleForward flexForward = SpearHelpers.fromJson(bts);
		SpearObject relation = mapper.toRelation(flexForward);
		logger.info("Result : \n"  + SpearHelpers.toJson(relation, true));
	}

}
