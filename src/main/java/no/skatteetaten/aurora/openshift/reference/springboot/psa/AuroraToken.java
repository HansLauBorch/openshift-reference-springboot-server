package no.skatteetaten.aurora.openshift.reference.springboot.psa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuroraToken {
    private static final Pattern AURORA_TOKEN_PATTERN = Pattern.compile("aurora-token (\\p{javaLetterOrDigit}+)");

    private final String token;

    public AuroraToken(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        Iterator<String> i = lines.iterator();
        if (i.hasNext()) {
            token = i.next().trim();
        } else {
            throw new IllegalArgumentException("Could not find token in file " + path);
        }
    }

    public boolean matchesScheme(String header)  {
        return AURORA_TOKEN_PATTERN.matcher(header).matches();
    }

    public boolean tokenMatches(String header){
        Matcher m = AURORA_TOKEN_PATTERN.matcher(header);
        return m.matches() && token.equals(m.group(1));
    }

}

