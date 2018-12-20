package io.trxplorer.troncli;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Exchange;



public class TronFullNodeCliTest {

	private static TronFullNodeCli cli = new TronFullNodeCli("grpc.trongrid.io:50051",true);
	
	
	
	@Test
	public void testGetBlocksByNums() {
		
		
		Assert.assertEquals(7, cli.getBlockByNums(Arrays.asList(1l,2l,3l,101l,102l,201l,202l)).size());
	}
	
	
	@Test
	public void getGetMoreThan100Blocks() {
		
		Assert.assertTrue(cli.getBlocks(1,200 ).size()>100);

		
	}
	
	@Test
	public void getMultipleAccounts() {
		
		HashSet<String> addresses = new HashSet<>();
		
		addresses.add("TXNERnuY9udaV3Vs5wp6WuHCTG8zBp2R93");
		addresses.add("TUTroYrK8Dgg5SbqpEod1doEnEbFB31zWs");
		addresses.add("TUTroYrK8Dgg5SbqpEod1doEnEbFB31zWs");
		addresses.add("TXNERnuY9udaV3Vs5wp6WuHCTG8zBp2R93");
		addresses.add("TUTroYrK8Dgg5SbqpEod1doEnEbFB31zWs");
		addresses.add("TUTroYrK8Dgg5SbqpEod1doEnEbFB31zWs");
		addresses.add("TUTroYrK8Dgg5SbqpEod1doEnEbFB31zWs");
		addresses.add("TXNERnuY9udaV3Vs5wp6WuHCTG8zBp2R93");
		addresses.add("TUTroYrK8Dgg5SbqpEod1doEnEbFB31zWs");
		addresses.add("TUTroYrK8Dgg5SbqpEod1doEnEbFB31zWs");
		
		cli.getAccountsByAddresses(addresses);
		//Assert.assertEquals(2, cli.getAccountsByAddresses(addresses).size());

		
	}
	

}
