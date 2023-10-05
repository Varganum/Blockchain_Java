package blockchain;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

public class Block {

    private static final int MAXIMUM_ZEROES_NUMBER = 3;
    private static int zeroesNumber = 0;
    private static ArrayList<Transaction> currentBlockData;

    private final long blockId;

    private final Miner creatorMiner;

    private final long minerId;
    private final long timeStamp;

    private final long magicNumber;

    private final String hashPrevious;
    private final String hash;

    private ArrayList<Transaction> blockData;
    private String zeroesNumberInfo;

    private final Instant startGenerating;
    private final Instant finishGenerating;

    private long timeOfBlockGenerationSeconds;
    private long timeOfBlockGenerationNanoSeconds;

    private Block(Blockchain blockchain, Miner miner, long timeStamp, long magicNumber, String newHash, Instant startGenerating) {
        this.creatorMiner = miner;
        this.minerId = miner.getMinerID();
        this.blockId = blockchain.getBlockChainSize() + 1;
        this.timeStamp = timeStamp;
        this.magicNumber = magicNumber;
        this.hashPrevious = blockId == 1 ? "0" : blockchain.getBlock(blockId - 1).getHash();
        this.hash = newHash;
        this.startGenerating = startGenerating;
        this.finishGenerating = Instant.now();
        //System.out.println("New block constructed");
    }

    ///////////////////////////GETTERS////////////////

    public long getBlockId() {
        return blockId;
    }

    public synchronized String getHash() {
        return hash;
    }

    public String getHashPrevious() {
        return hashPrevious;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public ArrayList<Transaction> getBlockData() {
        return blockData;
    }


    ///////////////////////STATIC METHODS/////////////////////////////////

    public static synchronized void setZeroesNumber(int num) {
        zeroesNumber = Math.min(num, Block.MAXIMUM_ZEROES_NUMBER);
    }

    public static synchronized int getZeroesNumber() {
        return zeroesNumber;
    }

    public static synchronized Block getNewBlock(Blockchain blockchain, Miner miner, long timeStamp, long magicNumber, String newHash, Instant startGenerating) {
        Block newBlock = new Block(blockchain, miner, timeStamp, magicNumber, newHash, startGenerating);
        newBlock.setTimeOfBlockGeneration();
        return newBlock;
    }

    static void updateZeroesNumber(Block block) {
        if (block.timeOfBlockGenerationSeconds < 1) {
            Block.setZeroesNumber(getZeroesNumber() + 1);
            block.zeroesNumberInfo = "N was increased to " + getZeroesNumber();
        } else if (block.timeOfBlockGenerationSeconds < 2) {
            block.zeroesNumberInfo = "N stays the same";
        } else {
            Block.setZeroesNumber(getZeroesNumber() - 1);
            block.zeroesNumberInfo = "N was decreased by 1";
        }
        //System.out.println(block.zeroesNumberInfo);
    }

    /////////////////INSTANCE METHODS////////////////////////

    private void setTimeOfBlockGeneration() {
        this.timeOfBlockGenerationSeconds = this.finishGenerating.getEpochSecond() - this.startGenerating.getEpochSecond();
        this.timeOfBlockGenerationNanoSeconds = this.finishGenerating.get(ChronoField.NANO_OF_SECOND) - this.startGenerating.get(ChronoField.NANO_OF_SECOND);
        if (this.timeOfBlockGenerationNanoSeconds < 0) {
            this.timeOfBlockGenerationSeconds = this.timeOfBlockGenerationSeconds - 1;
            this.timeOfBlockGenerationNanoSeconds = 1000000000 + this.timeOfBlockGenerationNanoSeconds;
        }
    }

    public void printInfo() {
        System.out.println("Block:");
        System.out.printf("Created by miner%d\n", this.minerId);
        System.out.printf("miner%d gets %d VC\n", minerId, Blockchain.REWARD_FOR_NEW_BLOCK);
        System.out.printf("Id: %d\n", this.blockId);
        System.out.printf("Timestamp: %d\n", this.timeStamp);
        System.out.printf("Magic number: %d\n", this.magicNumber);
        System.out.printf("Hash of the previous block:\n%s\n", this.hashPrevious);
        System.out.printf("Hash of the block:\n%s\n", this.hash);
        this.printBlockData();
        this.printTimeOfGeneration();
        System.out.println(this.zeroesNumberInfo + "\n");
    }

    private void printBlockData() {
        if (this.blockData.isEmpty()) {
            System.out.println("Block data: no messages");
        } else {
            System.out.println("Block data:");
            for (Transaction transaction : this.blockData) {
                System.out.printf("%s [transaction ID # %d]\n", transaction.getText(), transaction.getTransactionID());
            }
        }
    }

    private void printTimeOfGeneration() {
        //if (this.timeOfBlockGenerationSeconds > 0) {
        System.out.printf("Block was generating for %d seconds\n", this.timeOfBlockGenerationSeconds);
        //} else {
        // System.out.printf("Block was generating for %d nanoseconds\n", this.timeOfBlockGenerationNanoSeconds);
        // }

    }

    public static void setBlockData(ArrayList<Transaction> chatMessages) {
        currentBlockData = chatMessages;
    }

    public void saveCurrentBlockData() {
        this.blockData = (ArrayList<Transaction>) currentBlockData.clone();
        currentBlockData.clear();
    }
}
