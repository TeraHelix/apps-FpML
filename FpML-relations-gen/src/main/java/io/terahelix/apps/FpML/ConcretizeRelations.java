package io.terahelix.apps.FpML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import io.terahelix.common.HelixRuntimeException;
import io.terahelix.spear.javaRuntime.SPTHelpers;
import io.terahelix.spear.lang.spearParseTree.SPT.Type;

/**
 * Concretize the relations in this project
 * @author jsteenkamp
 *
 */
public class ConcretizeRelations
{
	public static void main(String... args) throws Exception
	{
		concretizeRelations(Paths.get(args[0]));
	}
	
	public static void concretizeRelations(Path destFolder) throws Exception
	{
		if(Files.exists(destFolder) == false)
		{
			Files.createDirectories(destFolder);
		}
		
		Stream<String> allSPTNames = SPTHelpers.getAllSPTNames("FpML-relations").stream().filter(s -> s.endsWith("_Relation"));
		allSPTNames.forEach(x -> 
		{
			Type spt = SPTHelpers.getSPT(x);
			String res = spt.toString();
			Path dest = destFolder.resolve(x + ".spr");
			try
			{
				Files.writeString(dest, res);
			} 
			catch (IOException e)
			{
				throw new HelixRuntimeException("Could not write to : " + dest + " | encountered : " + e);
			}
			
		});
		
		
	}
	
}
