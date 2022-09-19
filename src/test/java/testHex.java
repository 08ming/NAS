import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * ClassName testHex
 * Description
 * Author Ka1HuangZhe
 * Date  10/23/2020
 */
public class testHex {
    public static void main(String[] args) throws NoSuchAlgorithmException{

        String md5Str = new BigInteger(1, "-232048L".getBytes(StandardCharsets.UTF_8)).toString(16);
        System.out.println(md5Str);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(("" + (-232048L)).getBytes());
        byte[] hash = md.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            if ((0xff & b) < 0x10) {
                hexString.append("0").append(Integer.toHexString((0xFF & b)));
            } else {
                hexString.append(Integer.toHexString(0xFF & b));
            }
        }
        System.out.println(hexString.toString());
    }
}
