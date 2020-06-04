package io.terahelix.apps.fpml.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import XSD.Core.AnyValue_Builder;
import io.terahelix.common.HelixLogger;
import io.terahelix.common.HelixRuntimeException;
import io.terahelix.common.TimeUtils;

/**
 * A utility class that helps the conversion between effectively primitive and spear objects (or at least their equivalent).
 * 
 * @author jsteenkamp
 *
 */
public class SpearXMLEffectivelyPrimitives
{
	private static final HelixLogger logger = HelixLogger.getLogger(SpearXMLEffectivelyPrimitives.class);
	
	private static final Map<Class<?>, Class<?>> jaxbSpearPrimitiveEquivalents;
	
	static
	{
		Map<Class<?>, Class<?>> premap = new ConcurrentHashMap<>();
		premap.put(BigDecimal.class, double.class);
		premap.put(XMLGregorianCalendar.class, LocalDateTime.class);
		premap.put(BigInteger.class, long.class);
		premap.put(double.class, double.class);
		premap.put(Double.class, double.class);
		premap.put(Boolean.class, boolean.class);
		premap.put(boolean.class, boolean.class);
		premap.put(long.class, long.class);
		premap.put(Long.class, long.class);
		premap.put(String.class, String.class);
		jaxbSpearPrimitiveEquivalents = Collections.unmodifiableMap(premap);
	}
	
	/**
	 * Checks whether the given Spear Object is a JAXB Primitive equivalent.
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isSpearJaxbPrimitiveEquivalent(Object obj)
	{
		Objects.requireNonNull(obj);
		
		Class<?> subject = obj.getClass();
		
		if(subject.equals(Double.class) || subject.equals(double.class))
		{
			return true;
		}
		if(subject.equals(Boolean.class) || subject.equals(boolean.class))
		{
			return true;
		}
		if(subject.equals(Long.class) || subject.equals(long.class))
		{
			return true;
		}
		if(subject.equals(Integer.class) || subject.equals(int.class))
		{
			return true;
		}
		if(subject.equals(String.class))
		{
			return true;
		}
		if(subject.equals(LocalDate.class))
		{
			return true;
		}		
		if(subject.equals(LocalDateTime.class))
		{
			return true;
		}
		if(subject.equals(Instant.class))
		{
			return true;
		}		
		return false;
	}
	
	/**
	 * Checks whether the given Jaxb object is a Spear Primitive Equivalent.
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isJaxbSpearPrimitiveEquivalent(Object obj)
	{
		Objects.requireNonNull(obj);
		
		return jaxbSpearPrimitiveEquivalents.containsKey(obj.getClass()) || 
			   obj instanceof XMLGregorianCalendar;
	}
	
	public static Object convertToSpearPrimitive(final Object obj)
	{
		if(obj instanceof JAXBElement == true)
		{
			return convertToSpearPrimitive(((JAXBElement)obj).getValue()); 
		}
		
		if(isJaxbSpearPrimitiveEquivalent(obj) == false)
		{
			throw new IllegalArgumentException("Cannot convert the type - " + obj.getClass() + " - to a primitive - it is not supported !");
		}
		
		Class<?> subject = obj.getClass();
		if(subject.equals(String.class))
		{
			return (String)obj;
		}
		else if(obj instanceof XMLGregorianCalendar)
		{
			XMLGregorianCalendar gCal = (XMLGregorianCalendar)obj;
			try
			{
				GregorianCalendar gregCalendar = gCal.toGregorianCalendar();
				return TimeUtils.convertToLocalDateTime(gregCalendar.toInstant());
			} 
			catch (Throwable e)
			{
				String msg = ("Unable to convert the Gregorian Calendar : " + gCal + " | " + e); 
				throw new HelixRuntimeException(msg, e);
			}
		}
		else if(subject.equals(Boolean.class) || subject.equals(boolean.class))
		{
			return ((Boolean)obj).booleanValue();
		}
		else if(subject.equals(BigDecimal.class) || subject.equals(double.class) || subject.equals(Double.class))
		{
			return ((Number)obj).doubleValue();
		}
		else if(subject.equals(BigInteger.class) || subject.equals(long.class) || subject.equals(Long.class))
		{
			return ((Number)obj).longValue();
		}
		else
		{
			throw new IllegalArgumentException("Do not know how to convert - " + obj.getClass() + " (" + obj + ") - to a Spear Primitive");
		}
		
	}
	
	public static void convertToSpearPrimitiveAndSet(	final Object obj,
														final AnyValue_Builder anyValueB)
	{
		if(obj instanceof JAXBElement == true)
		{
			convertToSpearPrimitiveAndSet(((JAXBElement)obj).getValue(), anyValueB);
			return;
		}
		
		if(isJaxbSpearPrimitiveEquivalent(obj) == false)
		{
			throw new IllegalArgumentException("Cannot convert the type - " + obj.getClass() + " - to a primitive - it is not supported !");
		}
		
		Class<?> subject = obj.getClass();
		if(subject.equals(String.class))
		{
			anyValueB.setStringValue((String)obj);
			return;
		}
		else if(subject.equals(boolean.class) || subject.equals(Boolean.class))
		{
			anyValueB.setBooleanValue((Boolean)obj);
			return;
		}
		else if(obj instanceof XMLGregorianCalendar)
		{
			XMLGregorianCalendar gCal = (XMLGregorianCalendar)obj;
			GregorianCalendar gregCalendar = gCal.toGregorianCalendar();
			anyValueB.setLocalDateTimeValue(TimeUtils.convertToLocalDateTime(gregCalendar.toInstant()));
			return;
		}
		else if(subject.equals(BigDecimal.class) || subject.equals(double.class) || subject.equals(Double.class))
		{
			anyValueB.setDoubleValue(((Number)obj).doubleValue());
			return;
		}
		else if(subject.equals(BigInteger.class) || subject.equals(long.class) || subject.equals(Long.class))
		{
			anyValueB.setLongValue(((Number)obj).longValue());
			return;
		}
		else
		{
			throw new IllegalArgumentException("Do not know how to convert - " + obj.getClass() + " (" + obj + ") - to a Spear Primitive");
		}
		
	}

	/**
	 * Converts the given Spear Object primitive to a type sought for the given class
	 * 
	 * @param spearFieldResult
	 * @param type
	 * @return
	 */
	public static Object convertToJAXBPrimitive(Object spearFieldResult, Class<?> soughtType)
	{
		Objects.requireNonNull(spearFieldResult);
		
		if(soughtType.equals(BigDecimal.class))
		{
			return BigDecimal.valueOf(((Number)spearFieldResult).doubleValue());
		}
		else if(soughtType.equals(BigInteger.class))
		{
			return BigInteger.valueOf(((Number)spearFieldResult).longValue());
		}
		else if(soughtType.equals(String.class))
		{
			return String.valueOf(spearFieldResult);
		}
		else
		{
			throw new IllegalArgumentException("Unable to convert the value - " + spearFieldResult.getClass() + " (" + spearFieldResult + ") - to the desired type of : " + soughtType);
		}
		
	}

	

	
	
	

}









































