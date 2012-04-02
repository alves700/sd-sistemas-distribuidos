package Testes;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import Comunicação.Comunicacao;

public class testeCripto {
	public static void main(String[] args) throws Exception {
	    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

	    byte[] input = "khjk".getBytes();
	    Cipher cipher = Cipher.getInstance("RSA");
	    
	    SecureRandom random = new SecureRandom();
	    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

	    generator.initialize(1024, random);

	    KeyPair pair = generator.generateKeyPair();
	    Key pubKey = pair.getPublic();

	    
	    Key privKey = pair.getPrivate();
	    
	    System.out.println(pubKey);
	    
	    
	    cipher.init(Cipher.ENCRYPT_MODE, privKey);
	    byte[] cipherText = cipher.doFinal(input);
	    //System.out.println("cipher: " + new String(cipherText));
	    
	    for ( byte i : cipherText){
	    	System.out.print(i);
	    }
	    KeyFactory fact = null;
	    try {
	    	
			RSAPublicKeySpec pub = null;
			
			fact = KeyFactory.getInstance("RSA");
			pub = fact.getKeySpec(pubKey, RSAPublicKeySpec.class);
				
			
		    BigInteger m = pub.getModulus();
		    BigInteger e = pub.getPublicExponent();
		    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
		    Key pubKey2 = fact.generatePublic(keySpec);
		    //System.out.println(pubKey2);
		  
		    String x = new String(cipherText, "ISO-8859-1");
		    Cipher cipher2 = Cipher.getInstance("RSA");
		    cipher2.init(Cipher.DECRYPT_MODE, pubKey);
		    System.out.println();
		    for ( byte i : x.getBytes("ISO-8859-1")){
		    	System.out.print(i);
		    }
		   
		    byte[] plainText = cipher2.doFinal( x.getBytes("ISO-8859-1"));
		    System.out.println("plain : " + new String(plainText));
		    
	    } catch (Exception e) {
		    throw new RuntimeException("Spurious serialisation error", e);
		}
	}
}
