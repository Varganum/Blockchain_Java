package blockchain;

import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Main {

    private static final int MINERS_NUMBER = 5;
    private static final int SIMPLE_USER_NUMBER = 5;
    private static final int BLOCKS_AMOUNT = 15;
    private static final Random RANDOM = new Random();

    private static List<Miner> miners;
    private static List<BlockchainUser> blockchainUsers;

    static GenerateKeys generateKeys;

    static ExecutorService executorService;

    static Blockchain blockchain;




    public static void main(String[] args) {

        blockchain = Blockchain.getBlockchain();


        try {
            generateKeys = new GenerateKeys(512);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.err.println(e.getMessage());
        }

        //creating a pool of miners
        executorService = Executors.newFixedThreadPool(2 * MINERS_NUMBER + SIMPLE_USER_NUMBER);
        miners = new ArrayList<>();
        blockchainUsers = new ArrayList<>();

        for (int i = 0; i < MINERS_NUMBER; i++) {
            Miner newMiner = new Miner(generateKeys, blockchain);
            newMiner.getMinerBlockchainUser().setParentMiner(newMiner);
            miners.add(newMiner);
            blockchainUsers.add(newMiner.getMinerBlockchainUser());
        }

        for (int i = 0; i < SIMPLE_USER_NUMBER; i++) {
            BlockchainUser blockchainUser = BlockchainUser.getNewUser(generateKeys, blockchain);
            blockchainUsers.add(blockchainUser);
        }

        for (int i = 0; i < SIMPLE_USER_NUMBER + MINERS_NUMBER; i++) {
            executorService.execute(blockchainUsers.get(i));
        }

        for (int i = 0; i < MINERS_NUMBER; i++) {
            executorService.execute(miners.get(i));
        }

        //System.out.println("I'm here");
        //make the main thread waiting for all necessary blocks are generated

        while (blockchain.getBlockChainSize() < BLOCKS_AMOUNT) {
            try {
                //System.out.println("I went to sleep a second");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        blockchain.printAllBlocks();
        //System.out.print("Blockchain validation status: " + blockchain.validateBlockchain());


    }

    public static void stopAllBlockchainUsers() {
        for (Miner miner : miners) {
            miner.setBlockActual(false);
            miner.setIsThereSmthToMine(false);
        }
        for (BlockchainUser blockchainUser : blockchainUsers) {
            blockchainUser.setKeepLiving(false);
        }
        executorService.shutdown();

    }

    //Method creates Block, validate it and add to blockchain if it is valid.
    //Then blockchain send reward to miner who generated this block. And finally save transactions data.
    public static synchronized void createBlock(Miner miner, long timeStamp, long magicNumber, String newHash, Instant startGenerating) {

        if (blockchain.getBlockChainSize() < BLOCKS_AMOUNT) {
            //create new block
            Block nextBlock = Block.getNewBlock(blockchain, miner, timeStamp, magicNumber, newHash, startGenerating);

            //validate block
            if (blockchain.validateNewBlockData(nextBlock)) {

                //update zeroes number for the next block
                Block.updateZeroesNumber(nextBlock);

                //add block to blockchain
                blockchain.addBlock(nextBlock);

                miner.getMinerBlockchainUser().acceptReward(Blockchain.REWARD_FOR_NEW_BLOCK);

                //save transactions data
                nextBlock.saveCurrentBlockData();

                //System.out.println(">>>>>>NEW BLOCK ADDED<<<<<<");

            }
        }

        if (blockchain.getBlockChainSize() >= BLOCKS_AMOUNT)  {
            stopAllBlockchainUsers();
        }
    }

    public static synchronized BlockchainUser selectUser(BlockchainUser requester) {
        BlockchainUser otherUser = blockchainUsers.get(RANDOM.nextInt(blockchainUsers.size()));
        while (requester.equals(otherUser)) {
            otherUser = blockchainUsers.get(RANDOM.nextInt(blockchainUsers.size()));
        }
        return otherUser;
    }
}