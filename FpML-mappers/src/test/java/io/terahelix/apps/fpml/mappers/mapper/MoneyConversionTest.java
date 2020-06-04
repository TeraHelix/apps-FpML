package io.terahelix.apps.fpml.mappers.mapper;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.fpml.fpml_5.confirmation.Currency;
import org.fpml.fpml_5.confirmation.Money;
import org.junit.Assert;
import org.junit.Test;

import io.terahelix.apps.fpml.mapper.SpearXMLMapperService;
import io.terahelix.common.HelixLogger;

public class MoneyConversionTest
{
	private static final HelixLogger logger = HelixLogger.getLogger(MoneyConversionTest.class);
	
	@Test
	public void roundTripMoney() throws Exception
	{
		SpearXMLMapperService xmlService = SpearXMLMapperService.getInstance();
		Marshaller marshaller = xmlService.getMarshaller();
		Unmarshaller unmarshaller = xmlService.getUnmarshaller();
		
		Money money = getMoneyObject();

		JAXBElement<Money> moneyMarshaller = new JAXBElement<Money>(new QName("","root"), Money.class, money);
		
		StringWriter res = new StringWriter();
		marshaller.marshal(moneyMarshaller, res);
		
		String xml = res.toString();
		
		logger.info("\n" + xml);
		
		JAXBElement<Money> moneyUnmarshaller = (JAXBElement<Money>)unmarshaller.unmarshal(new StreamSource( new StringReader(xml) ), Money.class);
		
		Money money2 = moneyUnmarshaller.getValue();
		
		logger.info("Result: \n" + money2 );
		
		Assert.assertEquals(money.getId(), money2.getId());
	}
	
	public static Money getMoneyObject()
	{
		Money mm = new Money();
		mm.setAmount(BigDecimal.valueOf(666.0));
		mm.setId("USD");
		Currency currency = new Currency();
		currency.setCurrencyScheme("Scheme");
		currency.setValue("USD");
		mm.setCurrency(currency);
		return mm;
	}
	
}
