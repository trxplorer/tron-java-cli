package io.trxplorer.troncli;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;



public class TronSolidityNodeCliTest {

	private static TronSolidityNodeCli cli = new TronSolidityNodeCli("grpc.trongrid.io:50052",true);
	
	
	
	@Test
	public void testGetBlocksByNums() {
		System.out.println(cli.getBlocksByNums(Arrays.asList(4842687l)));
		

	}
	
	
	@Test
	public void testGetTransactionInfo() {
		System.out.println(cli.getTxInfoByHash("78ed7f8a6e504f2fa821ba7e1c721dca5d0cf6a888dae7a83f67c49c32aaa84f").getFee());
	}
	
}
