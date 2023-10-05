package blockchain;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;
import java.util.Random;

public class BlockchainUser extends Thread implements Runnable {

    private static long userCounter = 0;
    private static final Random RANDOM = new Random();

    Blockchain blockchain;
    private final long userID;

    private final String userName;
    private Miner miner;

    private int amountVC;

    private boolean keepLiving;

    private BlockchainUser(GenerateKeys gk, Blockchain blockchain) {
        this.userID = userCounter;
        this.userName = StringUtil.generateName();
        this.amountVC = 0;
        this.keepLiving = true;
        gk.createKeys();
        PrivateKey privateKey = gk.getPrivateKey();
        PublicKey publicKey = gk.getPublicKey();
        try {
            gk.writeToFile("KeyPair/publicKey" + userID, publicKey.getEncoded());
            gk.writeToFile("KeyPair/privateKey" + userID, privateKey.getEncoded());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        this.blockchain = blockchain;
    }

    public static BlockchainUser getNewUser(GenerateKeys gk, Blockchain blockchain) {
        userCounter++;
        return new BlockchainUser(gk, blockchain);
    }

    @Override
    public void run() {
        while (keepLiving) {
            if (!this.isAccountEmpty()) {
                blockchain.makeTransaction(this, Main.selectUser(this), this.getRandomSum());
            }
            try {
                Thread.sleep(50);
                //System.out.printf("User %d slept a little and is back again.\n", this.getUserID());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }



    ///////////////////////////////GETTERS//////////////////////

    public long getUserID() {
        return this.userID;
    }

    public String getUserName() {
        return Objects.isNull(this.miner) ? this.userName : "miner" + this.miner.getMinerID();
    }


    /////////////////////////////SETTERS//////////////////////////////

    public synchronized void setAmountVC(int amountVC) {
        this.amountVC = amountVC;
    }

    public void setKeepLiving(boolean keepLiving) {
        this.keepLiving = keepLiving;
    }

    public void setParentMiner(Miner miner) {
        this.miner = miner;
    }

    /////////////////////OPERATIONAL METHODS/////////////////

    //Method accepts reward for generated new block from blockchain.
    public void acceptReward(int rewardForNewBlock) {
        this.setAmountVC(this.amountVC + rewardForNewBlock);
    }

    public void reduceAmountVC(int minus) {
        this.setAmountVC(this.amountVC - minus);
    }

    public void increaseAmountVC(int plus) {
        this.setAmountVC(this.amountVC + plus);
    }

    public boolean isAccountEmpty() {
        return this.amountVC == 0;
    }

    public int getRandomSum() {
        return RANDOM.nextInt(amountVC) / 2 + 1;
    }

}
