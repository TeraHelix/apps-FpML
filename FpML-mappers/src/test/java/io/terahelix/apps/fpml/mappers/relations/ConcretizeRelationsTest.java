package io.terahelix.apps.fpml.mappers.relations;

import java.nio.file.Paths;

import org.junit.Test;

import io.terahelix.apps.FpML.ConcretizeRelations;

/**
 * @author jsteenkamp
 *
 */
public class ConcretizeRelationsTest
{
	
	@Test
	public void testConcretize() throws Exception
	{
		ConcretizeRelations.concretizeRelations(Paths.get("target/test-concretized-relations"));
	}

}
