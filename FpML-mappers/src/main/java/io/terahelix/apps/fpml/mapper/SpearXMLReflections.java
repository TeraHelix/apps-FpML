package io.terahelix.apps.fpml.mapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.utils.Pair;

import XSDModel.JAXBClass;
import io.terahelix.common.HelixRuntimeException;
import io.terahelix.spear.javaRuntime.SPTHelpers;
import io.terahelix.spear.javaRuntime.SpearObject;
import io.terahelix.spear.javaRuntime.util.SpearClasses;
import io.terahelix.spear.lang.spearParseTree.SPT.Struct;

/**
 * Some utilities for the reflection of the spear types.
 * 
 * @author jsteenkamp
 *
 */
public class SpearXMLReflections
{
	private SpearXMLReflections() {}
	
	private static final Set<String> STRANGELETS = Set.of("com.sun.org.apache.xerces.internal.dom.ElementNSImpl");
	
	public static boolean isStrangelet(Class<?> candidate)
	{
		String canonicalName = candidate.getCanonicalName();
		return STRANGELETS.contains(canonicalName);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Optional<Pair<Class<?>, Class<?>>> getSpearToJaxbClassFromSPT(String sptName)
	{
		io.terahelix.spear.lang.spearParseTree.SPT.Type spt = SPTHelpers.getSPT(sptName); 
		Optional<JAXBClass> jaxbClassAnnotOpt = getJAXBClassAnnotation(spt);
		
		if(jaxbClassAnnotOpt.isEmpty() == true) return Optional.empty();

		String spearClassName = getSpearClassNameFromSPT(spt); 
		String jaxbClassName = jaxbClassAnnotOpt.get().getClassName();
		
		Class<? extends SpearObject> spearClass;
		try
		{
			spearClass = (Class)SpearClasses.forName(spearClassName);
		} 
		catch (ClassNotFoundException e)
		{
			throw new HelixRuntimeException("Unable to resolve the Spear class name - " + spearClassName + " - this was from the SPT type name : " + sptName, e);
		}
		Class<?> jaxbClass;
		try
		{
			jaxbClass = SpearClasses.forName(jaxbClassName);
		} 
		catch (ClassNotFoundException e)
		{
			throw new HelixRuntimeException("Unable to resolve the JAXB class name - " + jaxbClassName + " - this was from the SPT type name : " + sptName + ". The Spear Class was resolved to : " + spearClass.getName(), e);
		}
	
		return Optional.of(new Pair<>(spearClass, jaxbClass));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Optional<JAXBClass> getJAXBClassAnnotation(io.terahelix.spear.lang.spearParseTree.SPT.Type spt) 
	{
		Optional<List<SpearObject>> annotationListOpt = getJAXBClassAnnotationList(spt);
		if(annotationListOpt.isPresent() == false) return Optional.empty();
		List<SpearObject> annots = annotationListOpt.get();
		return (Optional)annots.stream().filter(s -> {return s instanceof JAXBClass;} ).findAny();
	}
	
	public static Optional<List<SpearObject>> getJAXBClassAnnotationList(io.terahelix.spear.lang.spearParseTree.SPT.Type spt) 
	{
		if(spt instanceof Struct)
		{
			Struct structSpt = (Struct)spt;
			return structSpt.getAnnotationList();
		}
		if(spt instanceof io.terahelix.spear.lang.spearParseTree.SPT.Enum)
		{
			io.terahelix.spear.lang.spearParseTree.SPT.Enum enumSpt = (io.terahelix.spear.lang.spearParseTree.SPT.Enum)spt;
			return enumSpt.getAnnotationList();
		}
		return Optional.empty();
	}
	
	public static String getSpearClassNameFromSPT(io.terahelix.spear.lang.spearParseTree.SPT.Type spt)
	{
		if(spt instanceof Struct)
		{
			Struct structSpt = (Struct)spt;
			return structSpt.getName();
		}
		if(spt instanceof io.terahelix.spear.lang.spearParseTree.SPT.Enum)
		{
			io.terahelix.spear.lang.spearParseTree.SPT.Enum enumSpt = (io.terahelix.spear.lang.spearParseTree.SPT.Enum)spt;
			return enumSpt.getName();
		}
		throw new IllegalArgumentException("Unknown Type : " + spt);
	}

}
