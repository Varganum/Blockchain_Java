package blockchain;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

public class Miner extends Thread implements Runnable {

    private static int minersCounter = 0;

    private final Blockchain blockchain;
    private final long minerID;
    private final BlockchainUser minerBlockchainUser;

    private Instant startGenerating;
    private boolean isBlockActual;
    private boolean isThereSmthToMine;


    Miner(GenerateKeys gk, Blockchain blockchain) {
        minersCounter++;
        minerID = minersCounter;
        minerBlockchainUser = BlockchainUser.getNewUser(gk, blockchain);
        this.blockchain = blockchain;
    }


    @Override
    public void run() {

        long blockID;
        long timeStamp;
        String hashPrevious;
        isThereSmthToMine = true;
        int currentZeroesNumber;

        while (isThereSmthToMine) {

            isBlockActual = true;
            blockID = blockchain.getBlockChainSize() + 1;
            timeStamp = new Date().getTime();
            if (blockID == 1) {
                hashPrevious = "0";
            } else {
                hashPrevious = blockchain.getBlock(blockID - 1).getHash();
            }
            currentZeroesNumber = Block.getZeroesNumber();

            startGenerating = Instant.now();

            try {
                //System.out.printf("Miner %d starts finding magic number\n", this.minerID);
                this.findMagicNumberAndHash(blockID, timeStamp, hashPrevious, currentZeroesNumber);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    ////////////////////GETTERS/////////////////////////////////

    public BlockchainUser getMinerBlockchainUser() {
        return this.minerBlockchainUser;
    }

    public long getMinerID() {
        return this.minerID;
    }


    ////////////////////SETTERS//////////////////////////////////

    public void setBlockActual(boolean blockActual) {
        isBlockActual = blockActual;
    }

    public void setIsThereSmthToMine(boolean state) {
        isThereSmthToMine = state;
    }


    ////////////////////OPERATING METHODS///////////////////////////////////////

    private void findMagicNumberAndHash(long blockID, long timeStamp, String hashPrevious, int zeroesNumber) throws InterruptedException {

        Random random = new Random(timeStamp + minerID);
        long magicNumber;
        String newHash;
        String hashBeginning = this.getHashBeginning(zeroesNumber);
        boolean isMagicNumberFound = false;

        do {
            //make 100 attempts to find magic number
            for (int i = 0; i < 100; i++) {
                magicNumber = Math.abs(random.nextLong());
                newHash = StringUtil.applySha256(StringUtil.getFieldsString(blockID, timeStamp, magicNumber, hashPrevious));
                if (newHash.substring(0, zeroesNumber).equals(hashBeginning)) {
                    isMagicNumberFound = true;
                    Main.createBlock(this, timeStamp, magicNumber, newHash, startGenerating);
                    break;
                }
            }

            //find out if this block is still actual
            if (blockID != blockchain.getBlockChainSize() + 1) {
                break;
            }

        } while (!isMagicNumberFound && isBlockActual);

    }

    private String getHashBeginning(int zeroesNumber) {
        String hashBeginning = "";
        for (int i = 0; i < zeroesNumber; i++) {
            hashBeginning = hashBeginning.concat("0");
        }
        return hashBeginning;
    }

}
