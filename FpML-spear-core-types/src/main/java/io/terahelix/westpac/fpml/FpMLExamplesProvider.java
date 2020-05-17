package io.terahelix.westpac.fpml;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.terahelix.common.ClasspathResourceUtils;

/**
 * A utility object that is able to provide some examples from the XML
 * 
 * @author jsteenkamp
 *
 */
public class FpMLExamplesProvider
{
	//s/FpML/confirmation/FpML-Resources-Index.txt
	public static final String INDEX_FILE_RESOURCE_PATTERN = "/FpML/%s/FpML-Resources-Index.txt";
	
	public static String getResourceIndexFilePath(FpMLExamplesType exampleType)
	{
		return String.format(INDEX_FILE_RESOURCE_PATTERN, exampleType.getResourcesRootPath());
	}
 
	public static <T extends FpMLExamplesType> List<FpMLExample<T>> getExamples(T exampleType)
	{
		String leadingFilter = "/" + exampleType.getPath() + "/";
		if(leadingFilter.equals("//"))
		{
			leadingFilter = "/";
		}
		return ___getExamples(exampleType, leadingFilter);
	}
 
	private static <T extends FpMLExamplesType> List<FpMLExample<T>> ___getExamples(final T examplesType, final String leadingFilter)
	{
		try
		{
			String fullIndex = ClasspathResourceUtils.readFullyFromClasspath(FpMLExamplesProvider.class, getResourceIndexFilePath(examplesType));
			String[] lines = fullIndex.split("\\r?\\n");
			List examples = new ArrayList(); 
			
			for (int i = 0; i < lines.length; i++)
			{
				String line = lines[i];
				if(!(line.startsWith(leadingFilter) && line.endsWith(".xml")))
				{
					continue;
				}
				
				String name = line.substring(line.lastIndexOf("/") + 1);
				String fullPath = "/FpML/" + examplesType.getResourcesRootPath()  + line;
				examples.add(new FpMLExample(examplesType, name, fullPath));
			}
			return Collections.unmodifiableList(examples);
		}
		catch(Exception e)
		{
			throw new RuntimeException("Unable to read the index file resource : " + getResourceIndexFilePath(examplesType) + " - are you sure everything has been set up correctly ? " + e, e  );
		}
	}

	public static String getExample(FpMLExample<? extends FpMLExamplesType> example)
	{
		try
		{
			return ClasspathResourceUtils.readFullyFromClasspath(FpMLExamplesProvider.class, example.getFullPath());
		}
		catch(Exception e)
		{
			throw new RuntimeException("Unable to read the index file resource : " + example + " - are you sure everything has been set up correctly ? " + e, e  );
		}
	}
	
	public static Reader readExample(FpMLExample example)
	{
		return new StringReader(getExample(example));
	}

}






















