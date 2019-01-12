package io.trxplorer.troncli;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.services.http.JsonFormat;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.TransactionInfo.Log;




public class TronSolidityNodeCliTest {

	private static TronSolidityNodeCli cli = new TronSolidityNodeCli("localhost:50051",true);
	
	
	
	@Test
	public void testGetBlocksByNums() {
		System.out.println(cli.getBlocksByNums(Arrays.asList(4842687l)));
		

	}
	
	@Ignore
	@Test
	public void testGetTransactionInfo() {
		
		while(true) {
			
			Block b = cli.getLastBlock();
		
			for(Transaction t:b.getTransactionsList()) {
				
			//	System.out.println(JsonFormat.printToString(t));
				
				String txHash = Sha256Hash.of(t.getRawData().toByteArray()).toString();
				int txCount = cli.getTxInfoByHash(txHash).getLogList().size();
				if (txCount>0) {
//					System.out.println("tx=>"+txHash);
//					System.out.println("count=>"+txCount);
					TransactionInfo txInfo = cli.getTxInfoByHash(txHash);
					
					String str = ByteArray.toStr(ByteArray
					        .fromHexString(ByteArray.toHexString(txInfo.getContractResult(0).toByteArray())));
					System.out.println(str);
//					for(ByteString results:txInfo.getContractResultList()) {
//						System.out.println(results.toString());
//					}
					
//					for(Log log:txInfo.getLogList()) {
//						
//						System.out.println(Hex.toHexString(log.getTopics(0).toByteArray()));	
//					}
					
					break;
				}
				
				
				
			}
			
		}
		
		
		

	}
	
}
