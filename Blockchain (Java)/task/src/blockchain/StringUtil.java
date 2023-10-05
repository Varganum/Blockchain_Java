package blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

public class StringUtil {

    private static final String[] USER_NAMES = {"Alladin", "Blade", "Clemens", "Don Donn", "Frank", "Mikola", "Jipara", "Tatiana",
            "Zuzana", "Ying", "Smith", "Bayana", "Ivka", "Karyin", "Ruben", "Martha", "Jonny", "Dominique", "Franz", "Aron", "Erkin",
            "Wolf", "Donald", "Nick", "Alex", "Sofia", "Pizza Shop", "Car Shop", "KFC", "Amazon", "GLOBUS", "H&M", "IKEA"};

    private static final Random RANDOM = new Random();

    /* Applies Sha256 to a string and returns a hash. */

    public static synchronized String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            /* Applies sha256 to our input */
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte elem : hash) {
                String hex = Integer.toHexString(0xff & elem);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateName() {
        return USER_NAMES[RANDOM.nextInt(USER_NAMES.length)];
    }

    public static String getFieldsString(long blockID, long timeStamp, long magicNumber, String hashPrevious) {
        return Long.toString(blockID).concat(Long.toString(timeStamp)).concat(Long.toString(magicNumber)).concat(hashPrevious);
    }

}
