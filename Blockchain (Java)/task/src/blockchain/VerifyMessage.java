package blockchain;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;


/* This Class was taken from

https://mkyong.com/java/java-digital-signatures-example/

and modified for Blockchain project.
 */

public class VerifyMessage {
    private List<byte[]> list;

    /* Constructor from initial version of the Class

    @SuppressWarnings("unchecked")
    //The constructor of VerifyMessage class retrieves the byte arrays from the File
    //and prints the message only if the signature is verified.
    public VerifyMessage(String filename, String keyFile) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
        this.list = (List<byte[]>) in.readObject();
        in.close();

        System.out.println(verifySignature(list.get(0), list.get(1), keyFile) ? "VERIFIED MESSAGE" +
                "\n----------------\n" + new String(list.get(0)) : "Could not verify the signature.");
    }

     */

    public VerifyMessage() {
        //System.out.println("Verifier initialized.");
    }

    //Method for signature verification that initializes with the Public Key,
    //updates the data to be verified and then verifies them using the signature
    private boolean verifySignature(byte[] data, byte[] signature, String keyFile) throws Exception {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(getPublic(keyFile));
        sig.update(data);

        return sig.verify(signature);
    }

    public boolean verifySignature(Transaction message) throws Exception {
        byte[] data = (new String(message.getList().get(0)) + message.getTransactionID()).getBytes();
        byte[] signature = message.getList().get(1);
        String keyFile = "KeyPair/publicKey" + message.getUserID();
        return verifySignature(data, signature, keyFile);
    }

    //Method to retrieve the Public Key from a file
    public PublicKey getPublic(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /* Example of class usage
    public static void main(String[] args) throws Exception{
        new VerifyMessage("MyData/SignedData.txt", "MyKeys/publicKey");
    }
     */
}
