package io.trxplorer.troncli;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.core.Wallet;
import org.tron.protos.Contract.AssetIssueContract;


@Ignore
public class TronFullNodeCliTest {

	private static TronFullNodeCli cli = new TronFullNodeCli("grpc.trongrid.io:50051",true);
	
	private static final Logger logger = LoggerFactory.getLogger(AbiUtil.class);
	
	@Test
	public void testGetBlocksByNums() {
		
		
		Assert.assertEquals(7, cli.getBlocksByNums(Arrays.asList(1l,2l,3l,101l,102l,201l,202l)).size());
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
	
	@Ignore
	@Test
	public void testTriggerContract() throws InterruptedException, ExecutionException {
		
		
//		cli.triggerContract(Wallet.decodeFromBase58Check(""),
//				"balanceOf(address)",
//				"",
//				false,
//				w.getAddress(),
//				w.getEcKey().getPrivKeyBytes());
		
		String balanceCheck = "\"" + "TUNAxU8aNoY5uTbdAytQ8P3eQ8ScXHGpYo"+ "\"";
		
		System.out.println(cli.triggerContract(Wallet.decodeFromBase58Check("TCMjU3taxp19xNWMFQdQw45CYwQcqrsYqA"), 
				"supply()","#", false, 0, 0, Wallet.decodeFromBase58Check("TEG22eLj9dTPyBCFA3X9oQbmy8mmdCT2NF"), "03c544f59f0fb28309c69562f8bc252cddbcf3086fd12ad7274f8130dbacfd4a"));
		
	
		
	}
	
	public static void main(String[] args) {
		
		System.out.println(Integer.toHexString(1));
		
	}
	
	@Test
	public void testGetAssetById() {
		
		AssetIssueContract contract = this.cli.getAssetIssueContractById("1002000");
		System.out.println(contract.getName());
		//Assert.assertEquals(expected, actual);
		
	}
	
}
