package com.API.Documents_Management.Utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(randomIndex);
    }
}
