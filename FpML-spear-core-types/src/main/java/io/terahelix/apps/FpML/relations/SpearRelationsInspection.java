package io.terahelix.apps.FpML.relations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_LongestLine;
import io.terahelix.common.HelixLogger;
import io.terahelix.common.HelixRuntimeException;
import io.terahelix.common.TH_FileUtils;
import io.terahelix.spear.javaRuntime.SPTHelpers;
import io.terahelix.spear.lang.spear.core.tools.Spear;
import io.terahelix.spear.lang.spear.core.typeInterfaces.MemberIfc;
import io.terahelix.spear.lang.spear.core.typeInterfaces.TypeIfc;
import io.terahelix.spear.lang.spearParseTree.SPT.Struct;
import io.terahelix.spear.lang.spearParseTree.SPT.Type;

/**
 * Some tools that will help with the inspection of relations in Spear.
 * 
 * 
 * @author jsteenkamp
 *
 */
public class SpearRelationsInspection
{
	private static final HelixLogger logger = HelixLogger.getLogger(SpearRelationsInspection.class);
	
	
	/**
	 */
	public static String getRelationName(Collection<Struct> allTypes)
	{
		final StringBuilder bname = new StringBuilder();
		for(Struct s : allTypes)
		{
			bname.append(s.getName());
			bname.append("_");
		}
		bname.append("Relation");
		return bname.toString();
	}
	
	/**
	 * Gets the relation definition for the given set of spear types.
	 * 
	 * @param spearTypes
	 * @return
	 */
	public static String getRelationCodeString(Collection<Struct> allTypes)
	{
		StringBuilder bldr = new StringBuilder();
		
		final Set<String> uniqueNameSpaces = allTypes.stream().map(s -> s.getName().substring(0, s.getName().lastIndexOf("."))).collect(Collectors.toSet());
		
		bldr.append("namespace " + SpearRelationsInspection.class.getSimpleName() +" ;");
		bldr.append("\n");
		for(String namespace : uniqueNameSpaces )
		{
			bldr.append("import " + namespace + ".*;");
			bldr.append("\n");
		}
		
		bldr.append("relation " + getRelationName(allTypes));
		bldr.append("\n");
		bldr.append("{");
		bldr.append("\n");
		
		for(Struct s : allTypes )
		{
			bldr.append(s.getName() + " |");
		}
		bldr.deleteCharAt(bldr.length() - 1);
		bldr.append("\n");
		bldr.append("}");
		bldr.append("\n");
		
		return bldr.toString();
	}
	
	/**
	 * Returns a description of a relation if we had converted the supplied types to it.
	 * Useful in doing a 'what-if' of the relation. 
	 * 
	 * @return
	 */
	public static io.terahelix.spear.lang.spear.core.types.Struct getRelationDescription(String... spearTypes)
	{
		final Collection<Struct> allTypes = convertToTypes(spearTypes);
		
		final String property = System.getProperty("java.io.tmpdir");
		final File fl = new File(property, UUID.randomUUID().toString());
		try
		{
			fl.mkdirs();
			String[] cargs = { "-in", fl.getAbsolutePath(), "-tr", SpearRelationsInspection.class.getSimpleName() };
			 
			File content = new File(fl, "GeneratedRelation.spr");
			final String relationName = getRelationName(allTypes);
		    final String contentStr = getRelationCodeString(allTypes);
		        
		    Files.writeString(content.toPath(), contentStr);
		 
		    io.terahelix.spear.lang.spear.core.types.TypeRegistry tr = Spear.compile( cargs );
		    
		    TypeIfc typeIfc = tr.get(relationName);
		    io.terahelix.spear.lang.spear.core.types.Struct reldescr = (io.terahelix.spear.lang.spear.core.types.Struct)typeIfc;
		    if(reldescr == null) 
		    {
		    	throw new HelixRuntimeException("Unable to find the relation description. Name was : " + relationName + " | Type registry was : \n" + tr) ;
		    }
		    return reldescr;
		}
		catch(Exception e)
		{
			throw new HelixRuntimeException("Unable to construct relation out of types : " + Arrays.toString(spearTypes) + " | encountered exception : " + e, e);
		}
		finally
		{
			try
			{
				TH_FileUtils.deleteDir(fl);
			} 
			catch (IOException e)
			{
				logger.warn("Unable to clean up the directory - " + fl + " - nothing actionable to do. So carrying on. Exception was : " + e);
			}
		}
	}
	
	public static String getRawTextDescription(io.terahelix.spear.lang.spear.core.types.Struct relDescription)
	{
		StringBuilder res = new StringBuilder();
		res.append("\n");
		
		AsciiTable at = new AsciiTable();
		at.addRule();
		at.addRow("Raw Fields Description: " + relDescription.getName());
		at.addRule();
		res.append(at.render());
		res.append("\n");
		
		
		at = new AsciiTable();
		at.addRule();
		at.addRow("Field Name",
		          "Field Type",
		          "Is Optional"
		          );
		at.addRule();
		
		for(MemberIfc member: relDescription.getMembers())
		{
			at.addRow(member.getName(),
			          member.getType().getExtendedName(),
			          member.isOptional());
			
			at.addRule();
		}
		CWC_LongestLine cwc = new CWC_LongestLine();
		at.getRenderer().setCWC(cwc);
		res.append(at.render());
		return res.toString();
	}
	
	public static Collection<Struct> convertToTypes(String... spearTypes)
	{
		return Arrays.stream(spearTypes).map(t -> 
		{
			Type structType = SPTHelpers.getSPT(t);
			if(structType instanceof Struct == false)
			{
				throw new HelixRuntimeException("Could not convert the type - " + t + " - to an SPT Struct type - invalid entry");
			}
			return (Struct)SPTHelpers.getSPT(t);
		}).collect(Collectors.toSet());
	}
	
	
}











