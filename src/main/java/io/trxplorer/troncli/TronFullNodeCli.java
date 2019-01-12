package io.trxplorer.troncli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.BlockLimit;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.api.GrpcAPI.Node;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.GrpcAPI.PaginatedMessage;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletGrpc.WalletFutureStub;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Exchange;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.Witness;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.typesafe.config.Config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.netty.util.concurrent.CompleteFuture;
import io.trxplorer.troncli.wallet.BroadcastResult;

public class TronFullNodeCli {
	
	private ManagedChannel channelFull = null;
	private WalletGrpc.WalletBlockingStub client = null;
	private WalletFutureStub fclient = null;
	public static final byte ADD_PRE_FIX_BYTE_MAINNET = (byte) 0x41;   //41 + address
	   
	public static final byte ADD_PRE_FIX_BYTE_TESTNET = (byte) 0xa0;   //a0 + address
	
	private static final Logger logger = LoggerFactory.getLogger(TronFullNodeCli.class);
	
	@Inject
	public TronFullNodeCli(Config config) {
		this(config.getString("tron.fullnode"),config.getBoolean("tron.mainNet"));
	}

	public TronFullNodeCli(String fullNodeAddress,boolean mainNet) {
		
		if (mainNet) {
			Wallet.setAddressPreFixByte(Constant.ADD_PRE_FIX_BYTE_MAINNET);	
		}
		
		
		channelFull = ManagedChannelBuilder.forTarget(fullNodeAddress)
	              .usePlaintext()
	              .build();
		this.fclient = WalletGrpc.newFutureStub(channelFull);
		this.client = WalletGrpc.newBlockingStub(channelFull);
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
	
	public Map<String,Account> getAccountsByAddresses(Set<String> addresses){
		
		ArrayList<CompletableFuture> futures = new ArrayList<>();
		
		Map<String,Account> accounts = Collections.synchronizedMap(new HashMap<>());
		
		for(String address:addresses) {
			
			futures.add(this.getAccountByAddressAsync(address).thenAcceptAsync((account)->{
				accounts.put(address, account);
			}));
			
		}
		
		
		try {
			
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
		
		} catch (InterruptedException | ExecutionException  e) {
			e.printStackTrace();
		}


		return accounts;
	}
	
	public CompletableFuture<Account> getAccountByAddressAsync(String address){
		return CompletableFuture.supplyAsync(()->getAccountByAddress(address));
	}
	
	public CompletableFuture<Map<String,Account>> getAccountsByAddressAsync(Set<String> addresses){
		
		return CompletableFuture.supplyAsync(()->{
			List<CompletableFuture> futures = new ArrayList<>();
			HashMap<String,Account> accounts = new HashMap<>();
			for(String address:addresses) {
				
				futures.add(this.getAccountByAddressAsync(address).thenAccept((account)->{
					accounts.put(address,account);
				}));
			}
			
			try {

				CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
				
				return accounts;
			} catch (InterruptedException | ExecutionException  e) {

				e.printStackTrace();
			}
			
			return accounts;
		});
		
	}

	
	public Block getBlockByNum(Long blockNum) {
		return this.client.getBlockByNum(NumberMessage.newBuilder().setNum(blockNum).build());
	}
	
	public CompletableFuture<Block> getBlockByNumAsync(Long blockNum) {
		return CompletableFuture.supplyAsync(()->this.client.getBlockByNum(NumberMessage.newBuilder().setNum(blockNum).build()));
	}
	
	public List<Block> getBlocks(long start,long stop) {
		
		if (stop-start<=100) {
			BlockList blockByLimitNext = this.client.getBlockByLimitNext(BlockLimit.newBuilder().setStartNum(start).setEndNum(stop).build());
			
			return blockByLimitNext.getBlockList();			
		}else {
			
			ArrayList<Long> nums = new ArrayList<>();
			for(long i=start;i<=stop;i++) {
				
				nums.add(i);
			}
			
			return getBlockByNums(nums);
			
		}
		

	}

	
	public List<Block> getBlockByNums(List<Long> blockNums) {
		
		Collections.sort(blockNums);
		
		HashMap<String,List<Long>> groups = new HashMap<>();
		
		List<Block> blocks = Collections.synchronizedList(new ArrayList<Block>());
		
		for (int i = 0; i < blockNums.size(); i++) {
			long blockNum = blockNums.get(i);
			
			blockNum = blockNum==0 ? 1 : blockNum;

			int min = (int) (blockNum/100)*100;
			int max = min + 100;
		
			String key = min+"-"+max;
			List<Long> group = groups.get(key);
			
			if (group==null) {
				group = new ArrayList<>();
				groups.put(key, group);
			}
			
			group.add(blockNum);
			
			
			
		}
		
		ArrayList<CompletableFuture> futures = new ArrayList<>();
		
		for(String group:groups.keySet()) {
			
			List<Long> nums = groups.get(group);
			
			Collections.sort(nums);
			
			long start = nums.get(0);
			long stop = nums.get(nums.size()-1);
			
			
			stop = stop + 1;
			

			
			futures.add(this.getBlocksAsync(start, stop).thenAcceptAsync((rblocks)->{
				if (rblocks!=null) {
					blocks.addAll(rblocks);					
				}
			}));
				
			
			
			
		}

		try {
			
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
		
		} catch (InterruptedException | ExecutionException  e) {
			e.printStackTrace();
		}
		
		return blocks;
	}


	
	public CompletableFuture<List<Block>> getBlocksAsync(long start,long stop) {
		return CompletableFuture.supplyAsync(()->client.getBlockByLimitNext(BlockLimit.newBuilder().setStartNum(start).setEndNum(stop).build()).getBlockList())
				.handleAsync((blocks,e)->{
					if (e!=null) {
						return null; 
					}else {
						return blocks;
					}
				});
	}	
	
	public List<Witness> getAllWitnesses() {

		List<Witness> result = new ArrayList<>();

		WitnessList witnessList = this.client.listWitnesses(EmptyMessage.newBuilder().build());
		
		if (witnessList!=null) {
			result = witnessList.getWitnessesList();
		}

		return result;
	}
	
	public Transaction getTxByHash(String hash) {
		
		return this.client.getTransactionById(BytesMessage.newBuilder().setValue(ByteString.copyFrom(ByteArray.fromHexString(hash))).build());
		
	}
	
	public TransactionInfo getTxInfoByHash(String hash) {
		return this.client.getTransactionInfoById(BytesMessage.newBuilder().setValue(ByteString.copyFrom(ByteArray.fromHexString(hash))).build());
	}

	
	public List<Node> getAllNodes() {
		
		List<Node> result = new ArrayList<>();

		NodeList nodeList = this.client.listNodes(EmptyMessage.newBuilder().build());
		
		if (nodeList!=null) {
			result = nodeList.getNodesList();
		}
		
		return result;
	}
	
	public long getNextMaintenanceTime() {
		
		NumberMessage res = this.client.getNextMaintenanceTime(EmptyMessage.newBuilder().build());

		if (res==null) {
			return -1;
		}
		
		return res.getNum();
	}
	
	public Block getLastBlock() {
		return client.getNowBlock(EmptyMessage.newBuilder().build());
	}
	
	public ExchangeList getExchangesList() {
		
		return this.client.getPaginatedExchangeList(PaginatedMessage.newBuilder().setLimit(100l).setOffset(0).build());
		
	}
	
	public AccountResourceMessage getAccountRessourceByAddress(String address) {
		
		try {
			ByteString addressBS = ByteString.copyFrom(Wallet.decodeFromBase58Check(address));
			
			Account request = Account.newBuilder().setAddress(addressBS).build();
			
			AccountResourceMessage result = this.client.getAccountResource(request);
	
			return result;
		
		}catch(Exception e) {
			logger.error("Could not get account:"+address, e);
		}
		return null;
	}
	
	public CompletableFuture<AccountResourceMessage> getAccountRessourceByAddressAsync(String address){
		
		return CompletableFuture.supplyAsync(()->this.getAccountRessourceByAddress(address));
	}
	
	public Exchange getExchangeById(Long id) {
		
		return this.client.getExchangeById(BytesMessage.newBuilder().setValue(ByteString.copyFrom(ByteArray.fromLong(id))).build());
		
	}

	public BroadcastResult broadcastTransaction(byte[] bytes) {
		
		BroadcastResult result = new BroadcastResult();		
		try {

			Transaction transaction = Transaction.parseFrom(bytes);
			
			Return bReturn = this.client.broadcastTransaction(transaction);
			
			result.setSuccess(bReturn.getResult());
			result.setErrorMsg(bReturn.getMessage().toStringUtf8());
			result.setCode(bReturn.getCode().getNumber());
			result.setTxId(Sha256Hash.of(transaction.getRawData().toByteArray()).toString());
			
			
			return result;
			
		} catch (InvalidProtocolBufferException e) {
			result.setSuccess(false);
			result.setErrorMsg("Could not parse transaction");
		}

		
		
		return result;
	}
	
	public void shutdown() throws InterruptedException {
		this.channelFull.shutdown();
		this.channelFull.awaitTermination(10, TimeUnit.SECONDS);

	}



}
