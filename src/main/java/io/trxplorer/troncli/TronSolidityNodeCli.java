package io.trxplorer.troncli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.utils.ByteArray;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.Witness;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.typesafe.config.Config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class TronSolidityNodeCli implements ITronNodeCli{
	
	private ManagedChannel channelFull = null;
	private WalletSolidityGrpc.WalletSolidityBlockingStub client = null;
	
	public static final byte ADD_PRE_FIX_BYTE_MAINNET = (byte) 0x41;   //41 + address
	   
	public static final byte ADD_PRE_FIX_BYTE_TESTNET = (byte) 0xa0;   //a0 + address
	
	private static final Logger logger = LoggerFactory.getLogger(TronSolidityNodeCli.class);
	
	@Inject
	public TronSolidityNodeCli(Config config) {
		this(config.getString("tron.soliditynode"),config.getBoolean("tron.mainNet"));
	}

	public TronSolidityNodeCli(String solidityNodeAddress,boolean mainNet) {
		
		if (mainNet) {
			Wallet.setAddressPreFixByte(Constant.ADD_PRE_FIX_BYTE_MAINNET);	
		}
		
		
		channelFull = ManagedChannelBuilder.forTarget(solidityNodeAddress)
	              .usePlaintext(true)
	              .build();
		
		this.client = WalletSolidityGrpc.newBlockingStub(channelFull);
	}

	
	public Account getAccountByAddress(String address) {
		try {
		ByteString addressBS = ByteString.copyFrom(Wallet.decodeFromBase58Check(address));
		
		Account request = Account.newBuilder().setAddress(addressBS).build();
		
		Account account = this.client.getAccount(request);
		
		return account;
		}catch(Exception e) {
			logger.error("Could not get account:"+address, e);
		}
		return null;
	}
	
	public Transaction getTransactionByHash(String hash) {
		
		
		return this.client.getTransactionById(BytesMessage.newBuilder().setValue(ByteString.copyFrom(ByteArray.fromHexString(hash))).build());
	}
	
	
	public Block getBlockByNum(Long blockNum) {

		return this.client.getBlockByNum(NumberMessage.newBuilder().setNum(blockNum).build());
	}

	public List<Block> getBlocksByNums(List<Long> blockNums){

		ArrayList<CompletableFuture> futures = new ArrayList<>();
		
		List<Block> blocks = Collections.synchronizedList(new ArrayList<Block>());
		
		for(Long num:blockNums) {
			
			futures.add(this.getBlockByNumAsync(num).thenAcceptAsync((b)->{
				blocks.add(b);
			}));
			
		}
		
		try {
			
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
		
		} catch (InterruptedException | ExecutionException  e) {
			e.printStackTrace();
		}
		
		
		return blocks;
	}
	
	public CompletableFuture<Block> getBlockByNumAsync(Long blockNum) {
		return CompletableFuture.supplyAsync(()->this.client.getBlockByNum(NumberMessage.newBuilder().setNum(blockNum).build()));
	}
	
	public AssetIssueContract getAssetIssueContractById(String id) {
		
		AssetIssueContract assetIssueContract = this.client.getAssetIssueById(BytesMessage.newBuilder().setValue(ByteString.copyFrom(ByteArray.fromString(id))).build());
		
		return assetIssueContract;
	}
	
	public List<Witness> getAllWitnesses() {

		List<Witness> result = new ArrayList<>();

		WitnessList witnessList = this.client.listWitnesses(EmptyMessage.newBuilder().build());
		
		if (witnessList!=null) {
			result = witnessList.getWitnessesList();
		}

		return result;
	}
	
	public TransactionInfo getTxInfoByHash(String hash) {
		 
		return this.client.getTransactionInfoById(BytesMessage.newBuilder().setValue(ByteString.copyFrom(ByteArray.fromHexString(hash))).build());
		
	}
	
	public CompletableFuture<TransactionInfo> getTxInfoByHashAsync(String hash){
		
		return CompletableFuture.supplyAsync(()->this.getTxInfoByHash(hash));
	}
	
	public Map<String, TransactionInfo> getTxInfosByHash(List<String> hash){
		
		ArrayList<CompletableFuture> futures = new ArrayList<>();
		
		Map<String, TransactionInfo> result = Collections.synchronizedMap(new HashMap<String,TransactionInfo>());
		
		for(String txId:hash) {
			
			futures.add(this.getTxInfoByHashAsync(txId).thenAcceptAsync((info)->{
				
				result.put(txId,info);
				
			}));
			
		}
		try {
			
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
		
		} catch (InterruptedException | ExecutionException  e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	
	public Block getLastBlock() {
		return client.getNowBlock(EmptyMessage.newBuilder().build());
	}

	
	public void shutdown() throws InterruptedException {
		this.channelFull.shutdown();
		this.channelFull.awaitTermination(10, TimeUnit.SECONDS);

	}

	



}
