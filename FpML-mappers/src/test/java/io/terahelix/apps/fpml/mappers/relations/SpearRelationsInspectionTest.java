package io.terahelix.apps.fpml.mappers.relations;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.terahelix.apps.FpML.relations.SpearRelationsInspection;
import io.terahelix.common.HelixLogger;
import io.terahelix.spear.lang.spear.core.typeInterfaces.MemberIfc;
import io.terahelix.spear.lang.spearParseTree.SPT.Struct;

public class SpearRelationsInspectionTest
{
	private static final HelixLogger logger = HelixLogger.getLogger(SpearRelationsInspectionTest.class);
	
	private final String subjectSpear = Org.FpML.Confirmation.Money.$GetSpearAppClass().getName();
	
	@Test
	public void testCodeForRelation() throws Exception 
	{
		final Collection<Struct> convTypes = SpearRelationsInspection.convertToTypes(subjectSpear);
		String relCode = SpearRelationsInspection.getRelationCodeString(convTypes);
		
		Assert.assertNotNull(relCode);
		
		logger.info("Code was : \n" + relCode);
		
	}
	
	@Test
	public void testGenerateKnownRelations() throws Exception 
	{
		io.terahelix.spear.lang.spear.core.types.Struct reldescr = SpearRelationsInspection.getRelationDescription(subjectSpear);
		Assert.assertNotNull(reldescr);
		List<MemberIfc> members = reldescr.getMembers();
		Assert.assertTrue(members.size() > 0 );
		
		logger.info("\n" + SpearRelationsInspection.getRawTextDescription(reldescr));
		
	}
	
}
