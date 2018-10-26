package org.apache.activemq.cli.test;

import java.util.Random;

/**
 * Generate a random string.
 */
class StringGenerator {
    private String letters = "abcdefghijklmnopqrstuvwxyz";

    private String digits = "0123456789";

    private String symbols = "~!@#$%^&*()_+{}|?><,./";

    private String nonLatinLetters = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";

    String generateRandomString(int length) {
        String initialString = letters + letters.toUpperCase() + nonLatinLetters + nonLatinLetters.toUpperCase()
                + symbols + digits;

        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i=0; i < length; i++) {
            result.append(initialString.charAt(random.nextInt(initialString.length())));
        }
        return result.toString();
    }

}
