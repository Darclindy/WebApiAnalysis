package edu.osu.sec.vsa.utility;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import soot.Body;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.CompleteBlockGraph;

public class BlockGenerator {
	static BlockGenerator bg = new BlockGenerator();
	static int removeCount = 0;
	public static BlockGenerator getInstance() {
		return bg;
	}

	private BlockGenerator() {
	}

	Hashtable<Body, CompleteBlockGraph> ht = new Hashtable<Body, CompleteBlockGraph>();

	public CompleteBlockGraph generate(Body b) {
		if (!ht.containsKey(b)) {
			ht.put(b, new CompleteBlockGraph(b));
		}
		return ht.get(b);
	}

	public static boolean isCircle(Block b, Block current, CompleteBlockGraph cbg, HashSet<Block> history) {
		if (history.contains(current)) {
			return false;
		}
		boolean isc = false;
		//自己加的
		if(removeCount >= 20000000){
			return false;
		}
		//自己加的
		history.add(current);
		for (Block blk : cbg.getPredsOf(current)) {		//遍历前继，如果有前继等于它的后继，说明是一个循环块
			if (b == blk)
				isc = true;
			else
				isc |= isCircle(b, blk, cbg, history);
			if (isc)
				return isc;
		}
		history.remove(current);
		//自己加的
		removeCount += 1;
		//自己加的
		return isc;
	}

	public static void removeCircleBlocks(List<Block> bs, Block current, CompleteBlockGraph cbg) {
		HashSet<Block> rem = new HashSet<Block>();

		for (Block blk : bs) {//遍历所有前继
			if (isCircle(current, blk, cbg, new HashSet<Block>())) {//当前块，遍历的前继
				rem.add(blk); //
			}
		}
		for (Block blk : rem) {
			bs.remove(blk);
		}

	}
}
