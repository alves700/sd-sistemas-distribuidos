package Testes;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
public class testeCripto {
	public static void main(String[] args) throws Exception {
	    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

	    byte[] input = "abc aad123123".getBytes();
	    Cipher cipher = Cipher.getInstance("RSA", "BC");
	    SecureRandom random = new SecureRandom();
	    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

	    generator.initialize(1024, random);

	    KeyPair pair = generator.generateKeyPair();
	    Key pubKey = pair.getPublic();

	    
	    Key privKey = pair.getPrivate();
	    
	    System.out.println(pubKey.toString()+ "  " + pubKey.getFormat());
	    
	    cipher.init(Cipher.ENCRYPT_MODE, pubKey);
	    byte[] cipherText = cipher.doFinal(input);
	    System.out.println("cipher: " + new String(cipherText));

	    cipher.init(Cipher.DECRYPT_MODE, privKey);
	    byte[] plainText = cipher.doFinal(cipherText);
	    System.out.println("plain : " + new String(plainText));
	  }
}
