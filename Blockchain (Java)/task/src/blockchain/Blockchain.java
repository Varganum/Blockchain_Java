package blockchain;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class Blockchain {

    private volatile ArrayDeque<Transaction> transactions = new ArrayDeque<>();

    private static final Blockchain BLOCKCHAIN = new Blockchain();
    private static final ArrayList<Block> BLOCKS = new ArrayList<>();

    private static final VerifyMessage VERIFY_MESSAGE = new VerifyMessage();

    private static volatile int nextTransactionID = 0;

    public static final int REWARD_FOR_NEW_BLOCK = 100;
    private static int currentMaxAcceptedMessageID = 0;

    private Blockchain() {
    }


    ///////////////////////GETTERS///////////////////////

    public synchronized long getBlockChainSize() {
        return BLOCKS.size();
    }

    public Block getBlock(long id) {
        return BLOCKS.get((int) id - 1);
    }

    public synchronized void addBlock(Block block) {
        BLOCKS.add(block);
        Block.setBlockData(this.getTransactions());
    }

    private synchronized ArrayList<Transaction> getTransactions() {
        ArrayList<Transaction> result = new ArrayList<>();
        while (!transactions.isEmpty()) {
            result.add(transactions.pollFirst());
        }
        return result;
    }

    public boolean validateNewBlockData(Block block) {
        boolean result = true;
        String hashBeginning = "";
        int zeroNumbers = Block.getZeroesNumber();
        for (int i = 0; i < zeroNumbers; i++) {
            hashBeginning = hashBeginning.concat("0");
        }

        if (!block.getHash().substring(0, zeroNumbers).equals(hashBeginning)) {
            result = false;
        } else if (block.getBlockId() > 1 && !this.getBlock(block.getBlockId() - 1).getHash().equals(block.getHashPrevious())) {
            result = false;
        } else if (block.getBlockId() > 1 && block.getTimeStamp() <= this.getBlock(block.getBlockId() - 1).getTimeStamp()) {
            result = false;
        }

        return result;
    }

    public static Blockchain getBlockchain() {
        return BLOCKCHAIN;
    }

    public void printAllBlocks() {
        for (Block block : BLOCKS) {
            block.printInfo();
        }
    }


    public synchronized int getNextTransactionID() {
        nextTransactionID++;
        return nextTransactionID;
    }

    public synchronized void makeTransaction(BlockchainUser sender, BlockchainUser recipient, int sumVC) {
        try {
            Transaction transaction = new Transaction(sender.getUserID(), this.getNextTransactionID(),
                    sender.getUserName() + " sent " + sumVC + " VC to " + recipient.getUserName(),
                    "KeyPair/privateKey" + sender.getUserID());
            transaction.transact(sender, recipient, sumVC);

            if (VERIFY_MESSAGE.verifySignature(transaction)) {
                if (transaction.getTransactionID() > currentMaxAcceptedMessageID) {
                    if (this.transactions.add(transaction)) {
                        //System.out.printf("Message ID # %d is added. \n", transaction.getMessageID());
                        currentMaxAcceptedMessageID = transaction.getTransactionID();
                    }
                } else {
                    System.out.printf("Hacker attempt detected: transaction ID # %d is not valid. Current MAX messageID is %d.\n", transaction.getTransactionID(), currentMaxAcceptedMessageID);
                }
            } else {
                System.out.println("Could not verify the signature.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean validateBlockchain() {
        boolean result = true;
        String hashPrevious = "0";
        String currentHash;

        for (Block block : BLOCKS) {

            //check previous hash
            if (!block.getHashPrevious().equals(hashPrevious)) {
                result = false;
                System.out.println("Previous hash is not valid");
                break;
            }


            //generate independently hash code of the current block and compare it with hash saved in block
            //if saved hash is valid then save current hash to hashPrevious field for next block check
            currentHash = StringUtil.applySha256(StringUtil.getFieldsString(block.getBlockId(), block.getTimeStamp(), block.getMagicNumber(), hashPrevious));
            if (!block.getHash().equals(currentHash)) {
                result = false;
                System.out.printf("Hash of block ID # %d is not valid.\n", block.getBlockId());
                break;
            } else {
                hashPrevious = currentHash;
            }

            //check block data - chat messages

            int controlMaxMessageID = 0;

            for (Transaction message : block.getBlockData()) {

                try {
                    if (VERIFY_MESSAGE.verifySignature(message)) {
                        if (message.getTransactionID() > controlMaxMessageID) {
                            controlMaxMessageID = message.getTransactionID();
                        } else {
                            System.out.printf("Hacker invasion detected: message ID # %d is not valid. Current MAX messageID is %d.\n", message.getTransactionID(), controlMaxMessageID);
                        }
                    } else {
                        System.out.printf("Hacker invasion detected: could not verify the signature of message ID # %d.\n", message.getTransactionID());
                        result = false;
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }

        return result;
    }

}
