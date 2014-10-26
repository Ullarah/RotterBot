package com.ullarah.rotterbot.modules;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.ullarah.rotterbot.Messages.botReply;

public class Privilege {

    private static final List<String> triggerWords = Arrays.asList(
            "ableism", "abusive relationship", "ageism", "alcoholism", "amputation", "animal abuse", "animal death",
            "animal violence", "bestiality", "blood", "bodies", "body horror", "bones", "branding", "bullying",
            "cannibalism", "car accident", "child abuse", "childbirth", "classism", "cyberbullying", "death",
            "decapitation", "dental trauma", "domestic abuse", "drinking", "drugs", "eating disorder", "fatphobia",
            "forced captivity", "graphic sex", "guns", "holocaust", "homophobia", "hospitalisation", "hostages",
            "hunting", "insects", "incest", "kidnapping", "medical procedures", "murder", "nazi", "needles",
            "overdose", "pedophilia", "poisoning", "pregnancy", "prostitution", "PTSD", "racism", "rape",
            "ritualistic", "scarification", "self-harm", "serious injury", "sexism", "sexual abuse", "skeletons",
            "skulls", "slavery", "slurs", "smoking", "snakes", "spiders", "suicide", "suicidal", "swearing",
            "terminal illness", "terrorism", "torture", "transphobia", "violence", "vomit", "warfare", "weapons"
    );

    public static void checkYour(String user, String chan, String said) throws IOException {

        int privIteration = 0;

        for (String word : said.split(" "))
            if (triggerWords.contains(word) && privIteration == 0) {
                privIteration = 1;
                botReply(user + ", " + Colour.RED + Colour.BOLD + "Check your privilege!", chan);
            }

    }

}