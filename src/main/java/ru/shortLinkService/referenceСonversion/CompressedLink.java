package ru.shortLinkService.referenceСonversion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.shortLinkService.exceptions.CompressedException;

import java.net.URI;
import java.util.Locale;
import java.util.Random;

@Component
public final class CompressedLink implements CompressibleLink {
    private final String[] arrayOfLetters;
    private final int lengthUrl;

    public CompressedLink(@Value("${length.url}")int lengthUrl) throws CompressedException {
        if (lengthUrl < 4)
            throw new CompressedException("The minimum path length is 4.");
        arrayOfLetters = new String[52];
        this.lengthUrl = lengthUrl;
        init();
    }

    private void init() {
        int j = 0;
        for (char i = 'A'; i <= 'Z'; ++i) {
            arrayOfLetters[j] = String.valueOf(i);
            arrayOfLetters[j + 1] = String.valueOf(i).toLowerCase(Locale.ROOT);
            if (j + 2 < arrayOfLetters.length)
                j += 2;
        }
    }

    private String getLetter(int randomValue) {
        return arrayOfLetters[randomValue];
    }

    @Override
    public URI getShortUrl(URI uri)  {
        int randomValue;
        Random random = new Random();

        StringBuilder path = new StringBuilder("/");
        for (int i = 0; i < lengthUrl - 2; ++i) {
            randomValue = random.nextInt(0, arrayOfLetters.length - 1);
            path.append(getLetter(randomValue));
        }

        for(int i = 0; i < 2; ++i) {
            int indexRandom = random.nextInt(2, lengthUrl - 1);
            path.insert(indexRandom, random.nextInt(1, 9));
        }

        return uri.resolve(path.toString());
    }
}
