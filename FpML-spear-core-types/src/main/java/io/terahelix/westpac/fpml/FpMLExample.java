package io.terahelix.westpac.fpml;

/**
 * A object representing an FPML example
 * 
 * @author jsteenkamp
 *
 */
public class FpMLExample<T extends FpMLExamplesType>
{
	private final T exampleType;
	private final String name;
	private final String fullPath;
	
	public FpMLExample(T exampleType, String name, String fullPath)
	{
		this.exampleType = exampleType;
		this.name = name;
		this.fullPath = fullPath;
	}

	public T getExampleType()
	{
		return exampleType;
	}

	public String getName()
	{
		return name;
	}

	public String getFullPath()
	{
		return fullPath;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exampleType == null) ? 0 : exampleType.hashCode());
		result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FpMLExample other = (FpMLExample) obj;
		if (exampleType == null)
		{
			if (other.exampleType != null)
				return false;
		} else if (!exampleType.equals(other.exampleType))
			return false;
		if (fullPath == null)
		{
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "FpMLExample [exampleType=" + exampleType + ", name=" + name + ", fullPath=" + fullPath + "]";
	}


	
}
