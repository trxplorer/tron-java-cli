package io.trxplorer.troncli;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.tron.common.logsfilter.ContractEventParser;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.services.http.JsonFormat;
import org.tron.protos.Contract.TriggerSmartContract;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.SmartContract.ABI.Entry;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.TransactionInfo.Log;

import com.google.protobuf.InvalidProtocolBufferException;



@Ignore
public class TronSolidityNodeCliTest {

	private static TronSolidityNodeCli cli = new TronSolidityNodeCli("localhost:50051",true);
	
	
	
	@Test
	public void testGetBlocksByNums() throws InvalidProtocolBufferException {
		//System.out.println(cli.getBlocksByNums(Arrays.asList(4842687l)));
		//System.out.println(cli.getTxInfoByHash("99b42bdd2640e06b7f850791d1bc4ce40d2feb275bacaf71869aa8651e98d591"));
		
		
		TronFullNodeCli fcli = new TronFullNodeCli("grpc.trongrid.io:50051",true);
		
		SmartContract contract = fcli.getContractByAddress("TCMjU3taxp19xNWMFQdQw45CYwQcqrsYqA");
		
		TriggerSmartContract c = TriggerSmartContract.parseFrom(cli.getTransactionByHash("0251290787f1bb559a5288e84f56ec0196c50c7a24c80acb36f648077b9c8910").getRawData().getContract(0).getParameter().toByteArray());
		System.out.println(JsonFormat.printToString(c));

		
		TransactionInfo txInfo = cli.getTxInfoByHash("0251290787f1bb559a5288e84f56ec0196c50c7a24c80acb36f648077b9c8910");
		Log log = txInfo.getLog(0);
		
		
		

		
		List<byte[]> topics = log.getTopicsList().stream().map((t)->t.toByteArray()).collect(Collectors.toList());
		System.out.println(contract.getAbi().getEntrys(0));
		
		
		
		for(Entry e : contract.getAbi().getEntrysList()) {
		
			System.out.println(e.getName()+"=>"+ContractEventParser.parseEventData(log.getData().toByteArray(), topics, e));;
			System.out.println(ContractEventParser.parseTopics(topics, e));
			
			System.out.println("=========");
			
					
		}
		
		
		
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
