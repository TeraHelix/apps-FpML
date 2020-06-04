package io.terahelix.apps.fpml.mapper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import com.github.javaparser.utils.Pair;

import XSD.Core.AnyValue;
import XSD.Core.AnyValue_Builder;
import XSDModel.JAXBEnumConstant;
import io.terahelix.apps.FpML.FpMLView;
import io.terahelix.common.HelixLogger;
import io.terahelix.common.HelixRuntimeException;
import io.terahelix.spear.javaRuntime.SPTCorruptedException;
import io.terahelix.spear.javaRuntime.SPTHelpers;
import io.terahelix.spear.javaRuntime.SpearEnum;
import io.terahelix.spear.javaRuntime.SpearObject;
import io.terahelix.spear.javaRuntime.SpearObjectWithKey;
import io.terahelix.spear.javaRuntime.SpearStruct;
import io.terahelix.spear.javaRuntime.typeDescriptors.KeyOfTD;
import io.terahelix.spear.javaRuntime.typeDescriptors.ListTD;
import io.terahelix.spear.javaRuntime.typeDescriptors.OptTD;
import io.terahelix.spear.javaRuntime.typeDescriptors.StructTD;
import io.terahelix.spear.javaRuntime.typeDescriptors.TypeDescriptor;
import io.terahelix.spear.javaRuntime.util.SpearHelpers;
import io.terahelix.spear.lang.spearParseTree.SPT.EnumTag;

/**
 * A service that encapsulates all the back / forth XML mapping logic for this application.
 * 
 * @author jsteenkamp
 *
 */
public class SpearXMLMapperService
{
	private static final HelixLogger logger = HelixLogger.getLogger(SpearXMLMapperService.class);
	
	private static final AtomicReference<SpearXMLMapperService> _instance = new AtomicReference<>();
	private static final ReentrantLock lock = new ReentrantLock();
	
	public static SpearXMLMapperService getInstance()
	{
		SpearXMLMapperService mapper = _instance.get();
		if(mapper != null) return mapper;
		lock.lock();
		try
		{
			mapper = _instance.get();
			if(mapper != null) return mapper;
			mapper = new SpearXMLMapperService();
			_instance.set(mapper);
			return mapper;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	private final JAXBContext jaxbContext;
	private final Unmarshaller jaxbUnmarshaller;
	private final Marshaller jaxbMarshaller;
	
	private final Map<Class<?>, Class<?>> spearToJaxbClasses;
	private final Map<Class<?>, Class<?>> jaxbToSpearClasses;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SpearXMLMapperService()
	{
		try
		{
			jaxbContext = JAXBContext.newInstance(FpMLView.getFullJAXBContext());
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			Set<String> allSPTNames = SPTHelpers.getAllSPTNames();
			if(allSPTNames.size() == 0)
			{
				throw new HelixRuntimeException("Could not find any spear types associated with : " + FpMLView.Confirmation.getSpearProjectName() + " - seems you have serious project setup problems." );
			}
			
			Map<Class<?>, Class<?>> spearToJaxbClassesPre = new ConcurrentHashMap<>();
			Map<Class<?>, Class<?>> jaxbToSpearClassesPre = new ConcurrentHashMap<>();
			
			for(String sptName : allSPTNames)
			{
				final Optional<Pair<Class<?>, Class<?>>> spearToJaxbClassOpt = SpearXMLReflections.getSpearToJaxbClassFromSPT(sptName);
				
				if(spearToJaxbClassOpt.isEmpty() == true) continue;
				
				Pair<Class<?>, Class<?>> spearToJaxbClass = spearToJaxbClassOpt.get();
				
				spearToJaxbClassesPre.put(spearToJaxbClass.a, spearToJaxbClass.b);
				jaxbToSpearClassesPre.put(spearToJaxbClass.b, spearToJaxbClass.a);
			}
			
			spearToJaxbClasses = Collections.unmodifiableMap(spearToJaxbClassesPre);
			jaxbToSpearClasses = Collections.unmodifiableMap(jaxbToSpearClassesPre);
			
		} 
		catch (JAXBException e)
		{
			String msg = "Unable to initialise the JAXB machinery ! " + e;
			logger.error(msg, e);
			throw new HelixRuntimeException(msg, e);
		} 
		catch (SPTCorruptedException e)
		{
			String msg = "Seems the type registry for : " + FpMLView.Confirmation.getSpearProjectName() + " - is corrupted. Something is seriously wrong with the project setup: " + e;
			logger.error(msg, e);
			throw new HelixRuntimeException(msg, e);
		} 
	}

	public Unmarshaller getUnmarshaller()
	{
		return jaxbUnmarshaller; 
	}
	
	public Marshaller getMarshaller()
	{
		return jaxbMarshaller; 
	}

	public Map<Class<?>, Class<?>> getSpearToJaxbClasses()
	{
		return spearToJaxbClasses;
	}

	public Map<Class<?>, Class<?>> getJaxbToSpearClasses()
	{
		return jaxbToSpearClasses;
	}
	
	public SpearObjectDescription getSpearClassDescription(Object obj)
	{
		if(obj instanceof SpearObject && spearToJaxbClasses.containsKey(((SpearObject)(obj)).$getSpearAppClass()))
		{
			return SpearObjectDescription.SpearJaxb;
		}
		if(obj instanceof List)
		{
			return SpearObjectDescription.SpearList;
		}
		if(SpearXMLEffectivelyPrimitives.isSpearJaxbPrimitiveEquivalent(obj))
		{
			return SpearObjectDescription.EffectivelyPrimitive;
		}
		return SpearObjectDescription.Undefined;
	}
	
	private JaxbObjectDescription getJaxbClassDescription(Object obj)
	{
		if(obj instanceof JAXBElement)
		{
			JAXBElement jaxbEls = (JAXBElement)(obj);
			return getJaxbClassDescription(jaxbEls.getValue());
		}
		
		final Class<? extends Object> clazz = obj.getClass();
		if(jaxbToSpearClasses.containsKey(clazz))
		{
			return JaxbObjectDescription.JaxbSpear;
		}
		if(obj instanceof List)
		{
			return JaxbObjectDescription.JaxbList;
		}
		if(SpearXMLEffectivelyPrimitives.isJaxbSpearPrimitiveEquivalent(obj))
		{
			return JaxbObjectDescription.EffectivelyPrimitive;
		}
		if(SpearXMLReflections.isStrangelet(clazz) == true)
		{
			return JaxbObjectDescription.JaxbStrangelet;
		}
		
		logger.info("Was looking for - " + clazz.getName() + " - Could not find it so returning undefined." );
		return JaxbObjectDescription.Undefined;
	}
	
	public List<?> convertToJAXBList(List<SpearObject> spearObjectList) throws SpearXMLMapperException
	{
		final List targetList = new ArrayList<>();
		int len = spearObjectList.size();
		
		for (int i = 0; i < len; i++)
		{
			Object spearObject = spearObjectList.get(i);
			SpearObjectDescription spearDescr = getSpearClassDescription(spearObject);
			switch(spearDescr)
			{
				case EffectivelyPrimitive:
					throw new SpearXMLMapperException("Don't believe there are any cases of simple primitives being in a JAXB list - maybe I am wrong, but then if so, you need to fix this for special cases !" );
				case SpearList:
					targetList.add(convertToJAXBList((List<SpearObject>)spearObject));
				case SpearJaxb:
					targetList.add(convertToJAXBObject((SpearObject)spearObject));
					break;
				case Undefined:
					throw new SpearXMLMapperException("Undefined case for the object class - " + spearObject.getClass() + " (" + spearObject + ") - which was for a list" );
				default:
					throw new SpearXMLMapperException("You have not defined the case : " + spearDescr);
			}
		}
		return targetList;
	}
	
	public <T> T convertToJAXBObject(SpearObject spearObject) throws SpearXMLMapperException
	{
		Objects.requireNonNull(spearObject, "You cannot supply a null spearObject to this function !");
		
		Class<?> spearClass = spearObject.$getSpearAppClass();
		if(spearToJaxbClasses.containsKey(spearClass) == false)
		{
			throw new SpearXMLMapperException("Could not find the class : " + spearClass + " in the spearToJaxbClasses registry (of size " +  spearToJaxbClasses.size() + ")" );
		}
		
		Class<?> jaxbTargetClass = spearToJaxbClasses.get(spearClass);
		
		logger.debug("Converting To Jaxb : " + spearClass.getName() + " -> " + jaxbTargetClass.getName() );
		
		Object jaxbInstance;
		try
		{
			jaxbInstance = jaxbTargetClass.getDeclaredConstructor().newInstance();
		} 
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1)
		{
			throw new SpearXMLMapperException("Unable to obtain the default constructor for the JAXB object - " + jaxbTargetClass + " - this is unexpected as JAXB object should always have a default constructor : " + e1, e1);
		}
		
		SpearStruct spStruct = (SpearStruct)spearObject;
		
		final int memberMaxIndex = spStruct.$getMaxIndex();
		
		for (int i = 0; i < memberMaxIndex; i++)
		{
			String memberName = spStruct.$getMemberNameAt(i);
			Optional<Object> spearFieldResultOpt = spStruct.get(i);
			logger.debug("SPR->JAXB | Member Name [" + i + "]: " +  memberName + " | " + spearFieldResultOpt.orElseGet(() -> "(empty)"));

			if(spearFieldResultOpt.isEmpty() == true) continue;
			
			Object spearFieldResult = spearFieldResultOpt.get();
			SpearObjectDescription spearDescr = getSpearClassDescription(spearFieldResult);
			
			Method jaxbSetterOpt = extractSetterFromJAXBObject(jaxbInstance, jaxbInstance.getClass(), memberName).orElseThrow(() -> new SpearXMLMapperException("Could not determine the setter method - " + memberName + " - for the class : " + jaxbInstance.getClass()));
			
			switch(spearDescr)
			{
				case EffectivelyPrimitive:
					try
					{
						jaxbSetterOpt.invoke(jaxbInstance, SpearXMLEffectivelyPrimitives.convertToJAXBPrimitive(spearFieldResult, jaxbSetterOpt.getParameters()[0].getType())  );
					} 
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						throw new SpearXMLMapperException("Unable to invoke - " + jaxbSetterOpt + " - for the case : " + spearDescr + " - encountered the exception : " + e, e);
					}
					break;
				case SpearList:
					try
					{
						jaxbSetterOpt.invoke(jaxbInstance, convertToJAXBList((List)spearFieldResult)  );
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						throw new SpearXMLMapperException("Unable to invoke - " + jaxbSetterOpt + " - for the case : " + spearDescr + " - encountered the exception : " + e, e);
					}
				case SpearJaxb:
					
					Object jaxbCvResult = convertToJAXBObject((SpearObject)spearFieldResult);
					try
					{
						jaxbSetterOpt.invoke(jaxbInstance, jaxbCvResult);	
					}
					catch (ClassCastException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
					{
						throw new SpearXMLMapperException("Unable to invoke - " + jaxbSetterOpt + " - for the case : " + spearDescr + " - encountered the exception : " + e + " | The converted result was : " + jaxbCvResult, e);
					}
					break;
				case Undefined:
					throw new SpearXMLMapperException("Undefined case for the object class - " + spearFieldResult.getClass() + " (" + spearFieldResult + ") - which was for member - " + memberName + " - originating in " + spearObject.$getSpearAppClass() );
				default:
					throw new SpearXMLMapperException("You have not defined the case : " + spearDescr);
			}
		}

		return (T)jaxbInstance;
	}
	
	private Optional<Method> extractSetterFromJAXBObject(Object jaxbObject, Class<?> clazz, String spearMemberName)
	{
		String jaxbMethodName = "set" + spearMemberName;
		Method setterMethod = null;
		try
		{
			Method[] declaredMethods = clazz.getDeclaredMethods();
			for (int i = 0; i < declaredMethods.length; i++)
			{
				if(declaredMethods[i].getName().equals(jaxbMethodName))
				{
					setterMethod = declaredMethods[i];
				}
			}
		} 
		catch (SecurityException e)
		{
			logger.debug("Could not find the method.... - " + e);
		}
		
		if(setterMethod != null)
		{
			return Optional.of(setterMethod);
		}
		
		Class<?> superclass = clazz.getSuperclass();
		if(superclass != null)
		{
			return extractSetterFromJAXBObject(jaxbObject, superclass, spearMemberName);
		}
		return Optional.empty();
	}
	
	/**
	 * Describes a Jaxb Object
	 * @author jsteenkamp
	 *
	 */
	public static enum JaxbObjectDescription
	{
		/**
		 * A primitive object, or something that can easily be converted as such. E.g. a BigDecimal or date time.
		 */
		EffectivelyPrimitive,
		
		/**
		 * A Jaxb Object that can be converted to a spear object.
		 */
		JaxbSpear,
		
		/**
		 * A List of objects
		 */
		JaxbList,
		
		/**
		 * A foreign particle, often requiring special handling.
		 */
		JaxbStrangelet, 
		
		/**
		 * We have no idea what this is, probably a gap of sorts in your logic.
		 */
		Undefined; 
		
	}
	
	/**
	 * Describes a Spear Object
	 * 
	 * @author jsteenkamp
	 *
	 */
	public static enum SpearObjectDescription
	{
		/**
		 * A primitive object, or something that can easily be converted as such. E.g. a double or date time.
		 */
		EffectivelyPrimitive,
		
		/**
		 * A Spear Object that can be converted to a Jaxb object.
		 */
		SpearJaxb,
		
		/**
		 * A List of objects
		 */
		SpearList,
		
		/**
		 * We have no idea what this is, probably a gap of sorts in your logic.
		 */
		Undefined; 
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List convertToSpearObjectList(List<?> jaxbList, IdentityHashMap<Object, Object> existingRefs, int level) throws SpearXMLMapperException
	{
		
		//final List targetList = new SpearList();
		final List targetList = new ArrayList();
		int len = jaxbList.size();
		
		for (int i = 0; i < len; i++)
		{
			Object jaxbObject = jaxbList.get(i);
			JaxbObjectDescription jaxbDescr = getJaxbClassDescription(jaxbObject);
			switch(jaxbDescr)
			{
				case EffectivelyPrimitive:
					targetList.add(SpearXMLEffectivelyPrimitives.convertToSpearPrimitive(jaxbObject));
					break;
				case JaxbList:
					throw new IllegalArgumentException("What is Going on here !? - " + jaxbObject + " List of list : " + jaxbList);
				case JaxbSpear:
					targetList.add(___convertToSpearObject(jaxbObject, existingRefs, (level + 1) ));
					break;
				case JaxbStrangelet:
					targetList.add(__convertStrangeletToSpearObject(jaxbObject));
					break;
				case Undefined:
					throw new SpearXMLMapperException("Undefined case for the object class - " + jaxbObject.getClass() + " (" + jaxbObject + ") - which was for a list" );
				default:
					throw new SpearXMLMapperException("You have not defined the case : " + jaxbDescr);
			}
		}
		return targetList;
	}
	
	private List<XSD.Core.AnyValue> convertToSpearAnyValueList(List<?> jaxbList, IdentityHashMap<Object, Object> existingRefs, int level) throws SpearXMLMapperException
	{
		final List<XSD.Core.AnyValue> targetList = new ArrayList<XSD.Core.AnyValue>();
		int len = jaxbList.size();
		
		for (int i = 0; i < len; i++)
		{
			final Object jaxbObject = jaxbList.get(i);
			final XSD.Core.AnyValue_Builder anyValueB = XSD.Core.AnyValue.Create(); 
			JaxbObjectDescription jaxbDescr = getJaxbClassDescription(jaxbObject);
			
			switch(jaxbDescr)
			{
				case JaxbList:
					throw new IllegalArgumentException("What is Going on here !? - " + jaxbObject + " List of list : " + jaxbList);
			
				case EffectivelyPrimitive:
					SpearXMLEffectivelyPrimitives.convertToSpearPrimitiveAndSet(jaxbObject, anyValueB);
					break;
				case JaxbSpear:
					Object resObject = ___convertToSpearObject(jaxbObject, existingRefs, (level + 1));
					if(resObject.getClass().isEnum() == true)
					{
						anyValueB.setEnumValue((SpearEnum)resObject);
					}
					else
					{
						anyValueB.setSpearObject((SpearObject)resObject);
					}
					break;
				case JaxbStrangelet:
					throw new SpearXMLMapperException("Undefined case for the !Strangelet! object class - " + jaxbObject.getClass() + " (" + jaxbObject + ") - which was for a list" );
				case Undefined:
					throw new SpearXMLMapperException("Undefined case for the object class - " + jaxbObject.getClass() + " (" + jaxbObject + ") - which was for a list" );
				default:
					throw new SpearXMLMapperException("You have not defined the case : " + jaxbDescr);
			}
			targetList.add(anyValueB.build());
		}
		return targetList;
	}
	
	
	public <T extends SpearObject> T convertToSpearFromXML(Path xmlInput) throws SpearXMLMapperException
	{
		try {
			return convertToSpearFromXML(Files.newBufferedReader(xmlInput), xmlInput.getFileName().toString());
		} 
		catch (IOException e) 
		{
			throw new SpearXMLMapperException("Unable to open the path : " + xmlInput.toAbsolutePath() + " : " + e, e); 
		}
	}
	
	public <T extends SpearObject> T convertToSpearFromXML(Reader xmlInput) throws SpearXMLMapperException
	{
		return convertToSpearFromXML(xmlInput, Optional.empty());
	}
	
	public <T extends SpearObject> T convertToSpearFromXML(Reader xmlInput, String fileName) throws SpearXMLMapperException
	{
		return convertToSpearFromXML(xmlInput, Optional.ofNullable(fileName));
	}
	
	public <T extends SpearObject> T convertToSpearFromXML(Reader xmlInput, Optional<String> fileName) throws SpearXMLMapperException
	{
		double start = System.nanoTime();
		Unmarshaller unmarshaller = getUnmarshaller();
		Object jaxbInput;
		try
		{
			jaxbInput = unmarshaller.unmarshal(xmlInput);
			logger.debug("Successfully unmarshalled in " + ((System.nanoTime() - start / 1_000_000.0)) + " milliseconds the file " + fileName.orElse("[File Name not specified] - Convert to Spear Next")) ;
		} 
		catch (JAXBException e)
		{
			String msg = "Unable to convert the Supplied XML (" + fileName.orElse("[File Name not specified]")  + ") - Exception was : " + e;
			throw new SpearXMLMapperException(msg, e); 
		}
		return ___top_convertToSpearObject(jaxbInput);
	}
	
	public <T> T ___top_convertToSpearObject(Object jaxbObject) throws SpearXMLMapperException
	{
		IdentityHashMap<Object, Object> uniqueObjects = new IdentityHashMap<Object, Object>();
		T converted = ___convertToSpearObject(jaxbObject, uniqueObjects, 0);
		logger.debug("UNIQUE Objects Created : " + uniqueObjects.size());
		return converted;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> T ___convertToSpearObject(Object jaxbObject, IdentityHashMap<Object, Object> existingRefs, int level) throws SpearXMLMapperException
	{
		Objects.requireNonNull(jaxbObject, "You cannot supply a null jaxbObject to this function !");

		Class<?> jaxbClass = jaxbObject.getClass();
		if(jaxbClass.equals(JAXBElement.class))
		{
			JAXBElement jaxbEls = (JAXBElement)(jaxbObject);
			if(jaxbEls.getValue() instanceof List)
			{
				return (T)convertToSpearAnyValueList((List)jaxbEls.getValue(), existingRefs, (level+1));
			}
			return ___convertToSpearObject(jaxbEls.getValue(), existingRefs, (level+1));
		}
		
		if(jaxbToSpearClasses.containsKey(jaxbClass) == false)
		{
			throw new SpearXMLMapperException("Could not find the class : " + jaxbObject.getClass() + " (" + jaxbObject + ") in the jaxbObject registry (of size " +  jaxbToSpearClasses.size() + ")" );
		}
		
		Class<?> spearTargetClass = jaxbToSpearClasses.get(jaxbClass);
		logger.debug("Converting To Spear: " + jaxbClass.getName() + " -> " + spearTargetClass.getName() );
		
		if(existingRefs.containsKey(jaxbObject) == true)
		{
			logger.debug("Found existing match ! - " + jaxbObject + " | " + jaxbClass.getName() + " -> " + spearTargetClass.getName() );
			return (T)existingRefs.get(jaxbObject);
		}
		
		if(spearTargetClass.isEnum())
		{
			io.terahelix.spear.lang.spearParseTree.SPT.Enum enumSpt = (io.terahelix.spear.lang.spearParseTree.SPT.Enum)SPTHelpers.getSPT(spearTargetClass);
			List<EnumTag> tags = enumSpt.getTags();
			for(EnumTag tag : tags)
			{
				Optional<List<SpearObject>> annotationListOpt = tag.getAnnotationList();
				if(annotationListOpt.isEmpty()) continue;
				
				List<SpearObject> annotationList = annotationListOpt.get();
				for(SpearObject annot : annotationList)
				{
					if(annot instanceof JAXBEnumConstant == false) continue;
					JAXBEnumConstant enumConstantAnnot = (JAXBEnumConstant)annot;
					String jaxbAnnotationValue = enumConstantAnnot.getConstantExpression();
					jaxbAnnotationValue = jaxbAnnotationValue.substring(jaxbAnnotationValue .lastIndexOf(".") + 1);
					
					if(jaxbAnnotationValue.equals(jaxbObject.toString()))
					{
						T val = (T)Enum.valueOf((Class)spearTargetClass, tag.getValue());
						existingRefs.put(jaxbObject, val);
						return val;
					}
				}
			}
			throw new SpearXMLMapperException("Unable to map the constant '" + jaxbClass.getName() + "." + jaxbObject + "' to any Enum in Class : " + spearTargetClass.getName());
		}
		
		SpearStruct spStruct = (SpearStruct)SpearHelpers.create(spearTargetClass.getName());
		
		existingRefs.put(jaxbObject, spStruct);
		
		StructTD spStructTD = (StructTD)spStruct.getTypeDescriptor();
		
		final int memberMaxIndex = spStruct.$getMaxIndex();
		for (int i = 0; i < memberMaxIndex; i++)
		{
			String memberName = spStruct.$getMemberNameAt(i);
			TypeDescriptor memberTD = extractMemberTD(spStructTD, memberName);
			
			Optional<Object> jaxbFieldResultOpt = extractValueFromFieldInJAXBObject(jaxbObject, spearTargetClass, memberName);
			
			logger.debug("JXB->SPR | LvL=" + level + " | Cls=" + spearTargetClass.getName() + " | Mbr [" + i + "]: " +  memberName + " | " + jaxbFieldResultOpt.orElseGet(() -> "(empty)"));

			if(jaxbFieldResultOpt.isEmpty() == true) continue;
			
			Object jaxbFieldResult = jaxbFieldResultOpt.get();
			JaxbObjectDescription jaxbDescr = getJaxbClassDescription(jaxbFieldResult);
			
			switch(jaxbDescr)
			{
				case EffectivelyPrimitive:
					spStruct.set(i, SpearXMLEffectivelyPrimitives.convertToSpearPrimitive(jaxbFieldResult));
					break;
				case JaxbList:
					logger.debug("Setting the List to : " + memberName + " of Type : " + spStruct.getSpearAppClass());
					
					if(jaxbFieldResult instanceof List == false)
					{
						throw new IllegalArgumentException("What is Going on here !? - " + jaxbFieldResult + " is not an instance of list - yet the descriptor is : " + jaxbDescr);
					}

					boolean isAnySpearList =  isAnySpearList(memberTD);
					List<?> subStrateList = (List<?>)jaxbFieldResult;
					List<?> converted = null;
					try
					{	
						if(isAnySpearList == true)
						{
							converted = convertToSpearAnyValueList(subStrateList, existingRefs, (level+1) );
						}
						else
						{
							converted = convertToSpearObjectList(subStrateList, existingRefs, (level+1) );
						}
						spStruct.set(i, converted );
					} 
					catch (Exception e)
					{
						throw new SpearXMLMapperException("FAILED : Setting the List (isAnySpearList=" + isAnySpearList + ") to Member : " + memberName + " of Type : " + spStruct.getSpearAppClass() + " | Object was : " + jaxbFieldResult + " | Class was : " + jaxbFieldResult.getClass() , e);
					}
					break;
				case JaxbSpear:
					final SpearObject myConverted = ___convertToSpearObject(jaxbFieldResult, existingRefs, (level+1));
					boolean isKeyField = isMemberKeyTDField(spStructTD, memberName) == true; 
					try
					{
						if(isKeyField)
						{
							SpearObjectWithKey spk = (SpearObjectWithKey)myConverted;
							spStruct.set(i, spk.$getKey());
						}
						else
						{
							spStruct.set(i, myConverted);
						}
					}
					catch (Exception e)
					{
						throw new SpearXMLMapperException("FAILED : Setting to Member : " + memberName + " (IsKeyField=" + isKeyField + ") of Type : " + spStruct.getSpearAppClass() + " | Object Class was : " + jaxbFieldResult, e);
					}
					break;
				case JaxbStrangelet:
					spStruct.set(i, __convertStrangeletToSpearObject(jaxbObject) );
					break;
				case Undefined:
					throw new SpearXMLMapperException("Undefined case for the object class - " + jaxbFieldResult.getClass() + 
					                                  " (" + jaxbFieldResult + ") - which was for member - " + memberName + 
					                                  " - originating in " + jaxbObject.getClass() );
				default:
					throw new SpearXMLMapperException("You have not defined the case : " + jaxbDescr);
			}
		}
		
		//spStruct.makeImmutable();
		
		existingRefs.put(jaxbObject, spStruct);
		
		return ((T)spStruct);
	}
	
	private Object __convertStrangeletToSpearObject(Object jaxbObject)
	{
		 //TODO - this can be more elaborate at some point !
		AnyValue_Builder anyVal = AnyValue.Create();
		anyVal.setStringValue(String.valueOf(jaxbObject));
		return anyVal.build();
	}
	
	private boolean isMemberKeyTDField(StructTD structTd, String memberName)
	{
		TypeDescriptor tds = extractMemberTD(structTd, memberName);
		if(tds instanceof OptTD)
		{
			OptTD optTd = (OptTD)tds;
			tds = optTd.getUnder();
		}
		
		return tds instanceof KeyOfTD;
	}
	
	private TypeDescriptor extractMemberTD(StructTD structTd, String memberName)
	{
		Map<String, TypeDescriptor> members = structTd.getMembers();
		if(members.containsKey(memberName))
		{
			return members.get(memberName);
		}
		
		Map<String, TypeDescriptor> valueMembers = ((StructTD)members.get("$value")).getMembers();
		if(valueMembers.containsKey(memberName))
		{
			return valueMembers.get(memberName);
		}
		
		Map<String, TypeDescriptor> keyMembers = ((StructTD)members.get("$key")).getMembers();
		if(keyMembers.containsKey(memberName))
		{
			return keyMembers.get(memberName);
		}
		
		throw new IllegalArgumentException("Really do not know how to handle this item : " + memberName + " |  " + structTd);
	}
	
	private boolean isAnySpearList(TypeDescriptor tdP)
	{
		if(tdP == null)
		{
			return false;
		}
		
		ListTD listTD;
		try
		{
			if(tdP instanceof OptTD)
			{
				listTD = (ListTD)((OptTD)tdP).getUnder();
			}
			else 
			{
				listTD = (ListTD)tdP;
			}
		} 
		catch (Exception e)
		{
			String msg = "Got the exception - " + e + " - from " + tdP + ". Are you sure passed down the correct type descriptor to this function ?";
			throw new HelixRuntimeException(msg, e);
		}
		
		TypeDescriptor efftd = listTD.getEffectiveTD();
		if(efftd instanceof ListTD == false)
		{
			throw new IllegalArgumentException("Unexpected - " + listTD + " - effective TD should be a List TD");
		}
		
		ListTD effectiveListTD = (ListTD)efftd;
		TypeDescriptor tdUnder = effectiveListTD.getUnder();
		if(tdUnder instanceof StructTD)
		{
			StructTD underStruct = (StructTD)tdUnder;
			Class tdClass = underStruct.getTDClass();
			if(tdClass.equals(XSD.Core.AnyValue.class) == true)
			{
				return true;
			}
		}
			
		return false;
	}
	
	
	private Optional<Object> extractValueFromFieldInJAXBObject(Object jaxbObject, Class<?> spearTargetClass, String spearFieldName)
	{
		Class<?> jaxbClass = jaxbObject.getClass();
		final String jaxbMethodName_1 = "get" + spearFieldName;
		final String jaxbMethodName_2 = "is" + spearFieldName;
		
		Optional<Method> declaredMethodOpt = getDeclaredMethod(jaxbClass, jaxbMethodName_1);
		if(declaredMethodOpt.isEmpty())
		{
			declaredMethodOpt = getDeclaredMethod(jaxbClass, jaxbMethodName_2);
		}
		
		Method declaredMethod = declaredMethodOpt.orElseThrow(() -> new SpearXMLMapperException("Spear Class - " + spearTargetClass.getName() + " - mapped to - " + jaxbClass + " - was expecting to find a field corresponding to : " + jaxbMethodName_1 + " or " + jaxbMethodName_2));

		Object jaxbFieldResult = null;
		try
		{
			jaxbFieldResult = declaredMethod.invoke(jaxbObject);
		} 
		catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
		{
			throw new SpearXMLMapperException("Spear Class - " + spearTargetClass.getName() + " - mapped to - " + jaxbClass + " - Field Getter Name - " + declaredMethod.getName() + " - Tried to Access - Got : " + e, e );
		}
		return Optional.ofNullable(jaxbFieldResult);
	}
	
	private Optional<Method> getDeclaredMethod(Class<?> clazz, String jaxbMethodName )
	{
		Method getterMethod = null;
		try
		{
			Method[] allmethods = clazz.getDeclaredMethods();
			for(Method m : allmethods)
			{
				if(m.getName().equalsIgnoreCase(jaxbMethodName))
				{
					getterMethod = m;
					break;
				}
			}
		} 
		catch (Exception e)
		{
			logger.debug("Could not find the method.... - " + e);
		}
		
		if(getterMethod != null)
		{
			return Optional.of(getterMethod);
		}
		
		Class<?> superclass = clazz.getSuperclass();
		if(superclass != null )
		{
			return getDeclaredMethod(superclass, jaxbMethodName);
		}

		return Optional.empty();
	}

	/**
	 * 
	 * @param jaxb
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String convertToJAXBXML(Object jaxb) throws SpearXMLMapperException
	{
		Objects.requireNonNull(jaxb);

		if(jaxbToSpearClasses.containsKey(jaxb.getClass()) == false)
		{
			throw new SpearXMLMapperException("Unable to convert the object of class - " + jaxb.getClass() + " - it is not a known JAXB object !");
		}
		
		try
		{
			JAXBElement<?> jaxWrapper = new JAXBElement(new QName("","root"), jaxb.getClass(), jaxb);
			StringWriter res = new StringWriter();
			getMarshaller().marshal(jaxWrapper, res);
			String xml = res.toString();
			return xml;
		} 
		catch (JAXBException e)
		{
			throw new SpearXMLMapperException("Unable to marshall the the object of class - " + jaxb.getClass() + " - Got Exception : " + e, e);
		}
	}
	
	
	
}






























