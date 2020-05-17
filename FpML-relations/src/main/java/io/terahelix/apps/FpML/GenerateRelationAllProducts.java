package io.terahelix.apps.FpML;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import io.terahelix.apps.FpML.relations.SpearRelationsInspection;
import io.terahelix.common.HelixLogger;
import io.terahelix.spear.javaRuntime.SPTHelpers;
import io.terahelix.spear.javaRuntime.util.SpearClasses;
import io.terahelix.spear.lang.spearParseTree.SPT.Struct;
import io.terahelix.spear.lang.spearParseTree.SPT.Type;
import io.terahelix.spear.lang.spearParseTree.SPT.TypeRef;

/**
 * Generates some relations for all the products.
 * 
 * @author jsteenkamp
 *
 */
public class GenerateRelationAllProducts
{
	private static final HelixLogger logger = HelixLogger.getLogger(GenerateRelationAllProducts.class);
	
	public static final int FIELD_CUT_OFF = 100; //Beyond which we go 'soft'

	public static void main(String...args ) throws Exception
	{
		GenerateRelations();
	}
	
	
	public static void GenerateRelations() throws Exception 
	{
		
		saveFileOfRelations(Path.of("target/relation-gen/FpML_Confirmation_Relations.spr"),
							"Org.FpML.Confirmation.Relation",
							(FpMLView.Confirmation.getSpearPackage() + ".Product"),
							FpMLView.Confirmation.getSpearProjectName());
		
		saveFileOfRelations(Path.of("target/relation-gen/FpML_Legal_Relations.spr"),
							"Org.FpML.Legal.Relation",
							(FpMLView.Legal.getSpearPackage() + ".Product"),
							FpMLView.Legal.getSpearProjectName());
		
		saveFileOfRelations(Path.of("target/relation-gen/FpML_PreTrade_Relations.spr"),
							"Org.FpML.PreTrade.Relation",
							(FpMLView.PreTrade.getSpearPackage() + ".Product"),
							FpMLView.PreTrade.getSpearProjectName());
		
		saveFileOfRelations(Path.of("target/relation-gen/FpML_RecordKeeping_Relations.spr"),
							"Org.FpML.RecordKeeping.Relation",
							(FpMLView.RecordKeeping.getSpearPackage() + ".Product"),
							FpMLView.RecordKeeping.getSpearProjectName()
							);
		
		saveFileOfRelations(Path.of("target/relation-gen/FpML_Reporting_Relations.spr"),
							"Org.FpML.Reporting.Relation",
							(FpMLView.Reporting.getSpearPackage() + ".Product"),
							FpMLView.Reporting.getSpearProjectName());
		
		saveFileOfRelations(Path.of("target/relation-gen/FpML_Transparency_Relations.spr"),
							"Org.FpML.Transparency.Relation",
							(FpMLView.Transparency.getSpearPackage() + ".Product"),
							 FpMLView.Transparency.getSpearProjectName());
	}
	
	public static HashSet<Struct> fullParentLineage(Struct spt) 
	{
		HashSet<Struct> fullLineage = new HashSet<>();
		fullParentLineage(spt, fullLineage);
		return fullLineage;
	}
	
	public static void fullParentLineage(Struct spt, HashSet<Struct> fullLineage) 
	{
		Optional<List<Type>> parentsOpt = spt.getParents();
		if(parentsOpt.isPresent() == true)
		{
			List<Type> parents = parentsOpt.get();
			for(Type tp : parents)
			{
				if(tp instanceof TypeRef)
				{
					Type sptTypeRefStruct = SPTHelpers.getSPT(((TypeRef) tp).getName());
					if(sptTypeRefStruct instanceof Struct)
					{
						fullLineage.add((Struct)sptTypeRefStruct);
						fullParentLineage((Struct)sptTypeRefStruct, fullLineage);
					}
				}
			}
		}
	}
	
	private static void saveFileOfRelations(Path fileName,
	                                 		String namespaceDef,
	                                 		String productType, 
	                                 		String projectName) throws Exception
	{
		StringBuilder fileDef = new StringBuilder();
		fileDef.append("namespace " + namespaceDef + ";\n\n");
	
		fileDef.append("import XSD.FpML.*; \n" );
		fileDef.append("import Org.FpML.Confirmation.*; \n" );
		fileDef.append("import Org.FpML.PreTrade.*; \n" );
		fileDef.append("import Org.FpML.RecordKeeping.*; \n" );
		fileDef.append("import Org.FpML.Reporting.*; \n" );
		fileDef.append("import Org.FpML.Transparency.*; \n" );
		fileDef.append("import Org.FpML.Legal.*; \n\n" );
		
		Set<String> allSpts = SPTHelpers.getAllSPTNames(projectName);
		
		Map<String, String> relationToType = new TreeMap<>();
		
		for(String s : allSpts)
		{
			Type sptRoot = SPTHelpers.getSPT(s);
			if(sptRoot instanceof Struct == false)
			{
				continue;
			}
			
			Struct spt = (Struct)sptRoot;
			
			HashSet<Struct> fullParentLineage = fullParentLineage(spt);
			Set<String> fullParentNames = fullParentLineage.stream().map(x -> x.getName()).collect(Collectors.toSet());
			
			logger.info("Sub Type : " + spt.getName());
			logger.info("");
			logger.info("");
			
			Optional<List<Type>> childrenOpt = spt.getChildren();
			boolean hasNoChildren = childrenOpt.orElse(Collections.emptyList()).isEmpty();
			
			logger.info("Name : " + spt.getName() + " | hasNoChildren = " + hasNoChildren + " |  Parents : " + fullParentNames);
			
			if(fullParentNames.contains(productType) && hasNoChildren == true)
			{
				boolean isSoft;
				try
				{
					isSoft = isSoft(spt.getName());
				} 
				catch (Exception e)
				{
					logger.info("Could not parse : " + e + " - skipping : " + spt.getName());
					continue;
				}
				
				String subName = spt.getName().substring(spt.getName().lastIndexOf(".") + 1);
				String relationName = subName + "_Relation";

				logger.info("Found Match : " + spt.getName() + " (" + relationName + ") (Parent : " + productType + ")");
				fileDef.append("@SourceTypeOfRelation(TypeName = \"" + spt.getName() + "\") \n" );
				if(isSoft == true) fileDef.append("@SoftClass\n" );
				fileDef.append("relation " + relationName + "\n" );
				fileDef.append("{\n");
				fileDef.append("  " + spt.getName()  );
				fileDef.append("\n");
				fileDef.append("}\n\n");

				relationToType.put(relationName, spt.getName());
				
				continue;
			}
		}
		
		Path parent = fileName.getParent();
		if(Files.exists(parent) == false)
		{
			Files.createDirectories(parent);
		}
		
		Files.writeString(fileName, fileDef);
		
		logger.info("Wrote :  " + fileName.toAbsolutePath().toString() );
		
		System.out.println("\n\n\n");
		
		relationToType.forEach((k,v) -> 
		{
			System.out.println("relationsToTypesPre.put(" + k + ".class," + v + ".class);");
			System.out.println("typesToRelationsPre.put(" + v + ".class," + k + ".class);");
		});
		
	}
	
	private static boolean isSoft(String typeName)
	{
		try
		{
			SpearClasses.forName(typeName);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Could not find : " + typeName + " | " +e);
		}
		
		io.terahelix.spear.lang.spear.core.types.Struct potentialStruct = SpearRelationsInspection.getRelationDescription(typeName);
		if(potentialStruct.getMembers().size() > FIELD_CUT_OFF)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	

}























