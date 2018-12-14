package io.trxplorer.troncli;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;



public class TronSolidityNodeCliTest {

	private static TronSolidityNodeCli cli = new TronSolidityNodeCli("grpc.trongrid.io:50052",true);
	
	
	
	@Test
	public void testGetBlocksByNums() {
		System.out.println(cli.getBlocksByNums(Arrays.asList(4842687l)));
		
		//Assert.assertEquals(7, cli.getBlocksByNums(Arrays.asList(1l,2l,3l,101l,102l,201l,202l)).size());
	}
	
}
