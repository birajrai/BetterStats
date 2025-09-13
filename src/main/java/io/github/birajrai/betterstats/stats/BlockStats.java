package io.github.birajrai.betterstats.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.Document;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class BlockStats {
	
	private HashMap<String, Block> blocks;
	private long blocksDestroyed,
		 		 blocksPlaced,
		 		 minedBlocks,
		 		 redstoneUsed;

	public BlockStats() {
		blocksDestroyed = 0;
		blocksPlaced = 0;
		redstoneUsed = 0;
		minedBlocks = 0;
		blocks = new HashMap<>();
	}
	
	public BlockStats(long blocksDestroyed, long blocksPlaced, long redstoneUsed, long minedBlocks, HashMap<String, Block> blocks) {
		this.blocksDestroyed = blocksDestroyed;
		this.blocksPlaced = blocksPlaced;
		this.redstoneUsed = redstoneUsed;
		this.minedBlocks = minedBlocks;
		this.blocks = blocks;
	}
	
	protected HashMap<String, Block> getBlockStats() {
		return blocks;
	}
	
	public Block getBlockStatsByName(String blockName) {
		return blocks.get(blockName);
	}
	
	public List<Document> getBlockStatsList() {
		List<Document> blockDocs = new ArrayList<Document>(blocks.size());
		
		for(Block block : blocks.values())
			blockDocs.add(block.createBlockDocument());
		
		return blockDocs;
	}
	
	public long getBlocksDestroyed() {
		return blocksDestroyed;
	}
	
	public long getBlocksPlaced() {
		return blocksPlaced;
	}
	
	public long getRedstoneUsed() {
		return redstoneUsed;
	}
	
	public long getMinedBlocks() {
		return minedBlocks;
	}
	
	public long breakBlock(String blockName) {
		Block blockDestroyed = getBlockStatsByName(blockName);
		
		if(blockDestroyed == null)
			blockDestroyed = new Block(blockName);	
		 
		blockDestroyed.incNumBlocksDestroyed();
		
		blocks.put(blockName, blockDestroyed);	
		
		return blocksDestroyed++;
	}
	
	public long placeBlock(String blockName) {
		Block blockPlaced = getBlockStatsByName(blockName);
		
		if(blockPlaced == null)
			blockPlaced = new Block(blockName);	
		 
		blockPlaced.incNumBlocksPlaced();
		
		blocks.put(blockName, blockPlaced);
		
		return blocksPlaced++;
	}
	
	public long useRedstone() {
		return redstoneUsed++;
	}
	
	public long mineBlock() {
		return minedBlocks++;
	}
	
	public void setBlocksDestroyed(long blocksNum) {
		blocksDestroyed = blocksNum;
	}
	
	public void setBlocksPlaced(long blocksNum) {
		blocksPlaced = blocksNum;
	}
	
	public void setRedstoneUsed(long redstoneNum) {
		redstoneUsed = redstoneNum;
	}
	
	public void setMinedBlocks(long minedBlocksNum) {
		minedBlocks = minedBlocksNum;
	}
}
