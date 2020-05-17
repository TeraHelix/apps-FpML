package io.terahelix.apps.FpML.transform;

public class TransformException extends RuntimeException
{

	public TransformException()
	{
		super();
	}

	public TransformException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(	message,
				cause,
				enableSuppression,
				writableStackTrace);
	}

	public TransformException(String message, Throwable cause)
	{
		super(	message,
				cause);
	}

	public TransformException(String message)
	{
		super(message);
	}

	public TransformException(Throwable cause)
	{
		super(cause);
	}

}
