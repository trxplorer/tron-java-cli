package io.trxplorer.troncli;

import java.util.List;

import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;

public interface ITronNodeCli {

	public List<Block> getBlocksByNums(List<Long> blockNums);
	
	public Block getLastBlock();
	
	public Account getAccountByAddress(String address);
	
}
