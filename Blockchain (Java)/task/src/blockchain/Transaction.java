package blockchain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/* This Class was taken from

https://mkyong.com/java/java-digital-signatures-example/

and modified for Blockchain project.
 */

public class Transaction {
    private final List<byte[]> list;

    private final long userID;

    private final int transactionID;

    public Transaction(long userID, int transactionID, String data, String keyFile) throws Exception {
        list = new ArrayList<>();
        list.add(data.getBytes());
        list.add(sign(data + transactionID, keyFile));
        this.userID = userID;
        this.transactionID = transactionID;
    }

    //The method that signs the data using the private key that is stored in keyFile path
    private byte[] sign(String data, String keyFile) throws Exception{
        Signature rsa = Signature.getInstance("SHA1withRSA");
        rsa.initSign(getPrivate(keyFile));
        rsa.update(data.getBytes());
        return rsa.sign();
    }

    //Method to retrieve the Private Key from a file
    private PrivateKey getPrivate(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public String getText() {
        return new String(this.list.get(0));
    }

    public List<byte[]> getList() {
        return this.list;
    }

    public long getUserID() {
        return this.userID;
    }

    public int getTransactionID() {
        return this.transactionID;
    }

    //Method to write the List of byte[] to a file
    @SuppressWarnings("SameParameterValue")
    private void writeToFile(String filename) throws IOException {
        File f = new File(filename);
        f.getParentFile().mkdirs();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(list);
        out.close();
        System.out.println("Your file is ready.");
    }

    public synchronized void transact(BlockchainUser sender, BlockchainUser recipient, int sumVC) {
        sender.reduceAmountVC(sumVC);
        recipient.increaseAmountVC(sumVC);
    }

    /* Example of using Message class

    public static void main(String[] args) throws Exception{
        String data = JOptionPane.showInputDialog("Type your message here");

        new Message(0, 0, data, "KeyPair/privateKey").writeToFile("MyData/SignedData.txt");
    }
    */
}
