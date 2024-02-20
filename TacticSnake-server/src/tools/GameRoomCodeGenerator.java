package tools;

import java.security.SecureRandom;

public class GameRoomCodeGenerator {
    public static String generateHexString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length / 2];
        random.nextBytes(bytes);
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString().toUpperCase();
    }
}
