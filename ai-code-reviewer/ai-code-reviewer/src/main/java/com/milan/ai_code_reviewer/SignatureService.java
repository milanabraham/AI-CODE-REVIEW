package com.milan.ai_code_reviewer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class SignatureService {

    @Value("${github.webhook.secret}")
    private String webhookSecret;

    public boolean verifySignature(String signatureHeader, String payload) {
        try {
            String algorithm = "HmacSHA256";

            Mac hmac = Mac.getInstance(algorithm);
            hmac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), algorithm));

            byte[] computedHash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder("sha256=");
            for (byte b : computedHash) {
                sb.append(String.format("%02x", b));
            }

            String computedSignature = sb.toString();

            return computedSignature.equals(signatureHeader);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
