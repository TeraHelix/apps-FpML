package io.terahelix.apps.fpml.mapper;

import java.util.Collection;

import io.terahelix.common.HelixRuntimeException;

/**
 * A mapper exception, with some additional details - if appropriate.
 * 
 * @author jsteenkamp
 *
 */
public class SpearXMLMapperException extends HelixRuntimeException
{

	public SpearXMLMapperException()
	{
		super();
	}

	public SpearXMLMapperException(Collection<String> messages)
	{
		super(messages);
	}

	public SpearXMLMapperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(	message,
				cause,
				enableSuppression,
				writableStackTrace);
	}

	public SpearXMLMapperException(String message, Throwable cause)
	{
		super(	message,
				cause);
	}

	public SpearXMLMapperException(String message)
	{
		super(message);
	}

	public SpearXMLMapperException(Throwable cause)
	{
		super(cause);
	}

}
