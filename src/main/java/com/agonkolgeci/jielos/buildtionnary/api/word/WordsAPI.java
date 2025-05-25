package com.agonkolgeci.jielos.buildtionnary.api.word;

import com.agonkolgeci.jielos.buildtionnary.Buildtionnary;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.Collator;
import java.util.List;
import java.util.Objects;

public class WordsAPI {

    public static final List<String> WORDS;
    public static final Collator COLLATOR;

    static {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(Buildtionnary.class.getResourceAsStream("/words.json"), "Words file cannot be found."))) {
            WORDS = Buildtionnary.GSON.fromJson(reader, new TypeToken<List<String>>() {}.getType());

            if(WORDS.isEmpty()) throw new RuntimeException("Words are empty.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        COLLATOR = Collator.getInstance();
        COLLATOR.setStrength(Collator.NO_DECOMPOSITION);
    }

    public static String retrieveRandomWord() {
        return StringUtils.capitalize(WORDS.get(Buildtionnary.SPLITTABLE_RANDOM.nextInt(WORDS.size())));
    }

    public static boolean test(String word, String entry) {
        if(COLLATOR.compare(word, entry) == 0) return true;
        if(COLLATOR.compare(word, entry + "s") == 0) return true;
        if(COLLATOR.compare(word, entry + "x") == 0) return true;

        return false;
    }

}
