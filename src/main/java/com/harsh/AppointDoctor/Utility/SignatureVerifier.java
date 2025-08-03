package com.harsh.AppointDoctor.Utility;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class SignatureVerifier {
    public static boolean verify(String payload, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] dig = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generated = new BigInteger(1, dig).toString(16);
            return generated.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}

