package io.terahelix.xsd.spear;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

import XSDModel.ComplexType;
import XSDModel.GroupMember;
import XSDModel.Member;
import XSDModel.XSDEnum;
import XSDModel.XSDEnumMember;
import io.terahelix.common.HelixLogger;
import io.terahelix.common.HelixRuntimeException;
import io.terahelix.xsd.model.JAXB_XSDGroupsEnums_Model;
import io.terahelix.xsd.model.JAXB_XSDInstances_Model;
import io.terahelix.xsd.model.JAXB_XSD_ToModelConverter;

/**
 * An example invoking the Spear Converter directly for the FpML project (for debugging purposes);
 * 
 * @author jsteenkamp
 *
 */
public class JAXBToSpearConverter_Runner
{
	private static final HelixLogger logger = HelixLogger.getLogger(JAXBToSpearConverter.class);	

	public static final String BASE_DOCS_URL = "https://westpac.th-labs.online/source/xref/";
	
	public static void main(String... args) throws Exception 
	{
		//final String PROJECT_PATH = "confirmation";
		final String PROJECT_PATH = "codelist";
		
		final String sourcePathStr = "../westpac-model/" + PROJECT_PATH + "/fpml/src/main/java/org";
		final String xsdPathStr = "../westpac-model/" + PROJECT_PATH + "/fpml/src/main/resources/FpML";
		
		final Path sourcePath = Paths.get(sourcePathStr);
		
		logger.info("Source Path : " + sourcePath.toAbsolutePath());
		logger.info("XSD Path String : " + Paths.get(xsdPathStr).toAbsolutePath());
		
		logger.info("Finding out the Top Level Classes");
		
		Set<Class<?>> topLevelClassesConf = CodeUtils.findTopLevelAbstractClasses(sourcePath);
		
		HashSet<Class<?>> topLevelClasses = new HashSet<>();
		topLevelClasses.addAll(topLevelClassesConf);
		
		logger.info("Located a total of " + topLevelClasses.size() + " top level classes");
		
		logger.info("Creating the JAXB_XSD Model - Project Path : " + PROJECT_PATH);
		
		
		JAXB_XSDGroupsEnums_Model jaxbGroupModel = JAXB_XSD_ToModelConverter.convertModel(Paths.get(xsdPathStr));
		JAXB_XSDInstances_Model jaxbModel = JAXB_XSD_ToModelConverter.convertModel(jaxbGroupModel, Paths.get(xsdPathStr));
		
		JAXBToSpearConverter converter = new JAXBToSpearConverter(jaxbModel, 
		                                                          "org",
		                                                          sourcePath,
																  new ExamplePackageMapper(),
		                                                          topLevelClasses);
		converter.generateSpear();
	}	
	
	
}

























