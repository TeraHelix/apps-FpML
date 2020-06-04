package io.terahelix.apps.fpml.mappers;

import java.io.Reader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.fpml.fpml_5.confirmation.RequestConfirmation;
import org.junit.Test;

import io.terahelix.apps.FpML.FpMLView;
import io.terahelix.apps.FpML.types.FpMLConfirmationExamplesType;
import io.terahelix.apps.FpML.types.FpMLExample;
import io.terahelix.apps.FpML.types.FpMLExamplesProvider;
import io.terahelix.common.HelixLogger;

public class FxForwardReadTest
{
	private static final HelixLogger logger = HelixLogger.getLogger(FxForwardReadTest.class);
	public static final String FX_FORWARD_NAME = "fx-ex03-fx-fwd.xml";
	
	@Test
	public void testXMLReadingOfDerivatives() throws Exception
	{
		List<FpMLExample<FpMLConfirmationExamplesType>> examples = FpMLExamplesProvider.getExamples(FpMLConfirmationExamplesType.FX_DERIVATIVES);
		FpMLExample<FpMLConfirmationExamplesType> example = examples.stream().filter(ex -> ex.getName().equals(FX_FORWARD_NAME)).findAny().get();
		
		final JAXBContext jaxbContext = JAXBContext.newInstance(FpMLView.getJAXBContext(FpMLView.Confirmation));
		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		Reader bufReader = FpMLExamplesProvider.readExample(example);
		Object unmarshalled = unmarshaller.unmarshal(bufReader);
		
		logger.info("Unmarshalled : " + unmarshalled);
		
		JAXBElement el = (JAXBElement)unmarshalled;
		
		RequestConfirmation requestConfirm = (RequestConfirmation)el.getValue();
		
		List<JAXBElement<?>> allElements = requestConfirm.getRest();
		
		for (int i = 0; i < allElements.size(); i++)
		{
			JAXBElement<?> jaxbElement = allElements.get(i);
			Object jaxbVal = jaxbElement.getValue();
			logger.info("Inner Element (" + i + ") - " + jaxbVal);
		}
		
		
				
		logger.info("Unmarshalled (Value) : " + el.getValue());
		
		
		
		
	}
	
	
}

















