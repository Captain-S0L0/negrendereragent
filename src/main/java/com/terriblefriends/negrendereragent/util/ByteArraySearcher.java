package com.terriblefriends.negrendereragent.util;

public class ByteArraySearcher {
    public static boolean contains(byte[] source,  byte[] target) {
        if (target.length > source.length) {
            return false;
        }
        if (target.length == 0) {
            return true;
        }

        byte first = target[0];
        int max = source.length - target.length;

        for (int i = 0; i <= max; i++) {
            /* Look for first byte. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }

            /* Found first byte, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + target.length - 1;
                for (int k = 1; j < end && source[j]
                        == target[k]; j++, k++);

                if (j == end) {
                    /* Found whole array. */
                    return true;
                }
            }
        }
        return false;
    }
}
