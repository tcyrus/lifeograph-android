package net.sourceforge.lifeograph;

import android.util.Log;

import java.nio.charset.StandardCharsets

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by cyrust on 3/13/2018.
 */

public class LifeocryptHelper {

    private static Key expandKeyHelper(String passphrase, byte[] salt) throws NoSuchAlgorithmException {
        // OPEN MESSAGE DIGEST ALGORITHM
        MessageDigest hash = MessageDigest.getInstance(cHASH_ALGORITHM);

        // ADD SALT TO HASH
        hash.update(salt, 0, cSALT_SIZE);

        // ADD PASSPHRASE TO HASH
        hash.update(pass.getBytes(StandardCharsets.UTF_8));

        // ALLOCATE MEMORY FOR KEY
        byte[] hashresult = new byte[cKEY_SIZE];
        
        // FETCH DIGEST (THE EXPANDED KEY)
        hash.digest(hashresult, 0, cKEY_SIZE);

        return new SecretKeySpec(hashresult, cHASH_ALGORITHM);
    }

    private static byte[] encryptBufferHelper(byte[] buffer, int size, Key key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance(cCIPHER_TRANSFORM);

        // SET KEY + INITILIZING VECTOR (IV)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        // ENCRYPT (AND RETURN) BUFFER
        return cipher.doFinal(buffer, 0, size);
    }

    private static byte[] decryptBufferHelper(byte[] buffer, int size, Key key, IvParameterSpec iv) {
        byte[] out = new byte[size];

        try {
            Cipher cipher = Cipher.getInstance(cCIPHER_TRANSFORM);

            // SET KEY + INITILIZING VECTOR (IV)
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            // DECRYPT BUFFER
            out = cipher.doFinal(buffer, 0, size);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }

    public static String decryptBuffer(String passphrase, byte[] salt, byte[] buffer, int size, byte[] iv) {
        byte[] out = new byte[size];

        try {
            Key key = expandKeyHelper(passphrase, salt);
            out = decryptBufferHelper(buffer, size, key, new IvParameterSpec(iv));
        } catch (Exception e) {
            e.printStackTrace();
        }

        int size_dec_buf = 0;

        // EOF DETECTION: RATHER UGLY CODE
        for (int round = 0; size_dec_buf < size - 1; size_dec_buf++) {
            if (out[size_dec_buf] == '\n' && out[size_dec_buf + 1] == '\n') {
                if (round > 0 && size_dec_buf < size - 3 &&
                    (out[size_dec_buf + 2] != 'I' || out[size_dec_buf + 3] != 'D')) {
                    size_dec_buf += 2;
                    break;
                } else {
                    round++;
                }
            }
        }

        byte[] dec_buf = Arrays.copyOf(out, size_dec_buf);
        dec_buf[size_dec_buf - 1] = 0; // terminating zero

        String output = "XX";
        // cannot check the '\n' due to multi-byte char case
        if (dec_buf[0] == passphrase.codePointAt(0)) // && buffer[ 1 ] == '\n')
            output = new String(bytes, StandardCharsets.UTF_8);

        return output;
    }

    public static byte[] encryptBuffer(String passphrase, byte[] buffer, int size) {
        SecureRandom random = new SecureRandom();

        byte[] iv = new byte[cIV_SIZE];
        random.nextBytes(iv);
        byte[] salt = new byte[cSALT_SIZE];
        random.nextBytes(salt);

        byte[] out = new byte[size];

        try {
            Key key = expandKeyHelper(passphrase, salt);
            out = encryptBufferHelper(buffer, size, key, new IvParameterSpec(iv));
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] buffer_out = new byte[cSALT_SIZE + cIV_SIZE + size];
        System.arraycopy(salt, 0, buffer_out, 0, cSALT_SIZE);
        System.arraycopy(iv, 0, buffer_out, cSALT_SIZE, cIV_SIZE);
        System.arraycopy(out, 0, buffer_out, cSALT_SIZE + cIV_SIZE, size);

        return buffer_out;
    }

    private static final String cCIPHER_TRANSFORM = "AES/CFB/NoPadding"; // AES/CFB is actually AES_256/CFB

    private static final int cIV_SIZE = 16; // = 128 bits
    private static final int cSALT_SIZE = 16; // = 128 bits
    private static final int cKEY_SIZE = 32; // = 256 bits

    private static final String cHASH_ALGORITHM = "SHA-256";
}
