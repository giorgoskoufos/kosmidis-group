package com.kosmidis.jarvis.utils;

public class TextNormalizer {

    public static String normalizeJarvisName(String text) {
        if (text == null) return "";

        return text
                // English / Latin
                .replaceAll("(?iu)\\bjarvis\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bjavis\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bjervis\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bjarv[iy]s\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bjarves\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bjarvice\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bjarvish\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bj arv is\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bj a r v i s\\b", "J.A.R.V.I.S.")

                // Greek correct / common
                .replaceAll("(?iu)\\bτζαρβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάρβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζαρβής\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάρβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζαρβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάρβι\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζαρβι\\b", "J.A.R.V.I.S.")

                // Without τ
                .replaceAll("(?iu)\\bζαρβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζάρβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζαρβής\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζάρβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζαρβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζάρβι\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζαρβι\\b", "J.A.R.V.I.S.")

                // With υ / ει / η mistakes
                .replaceAll("(?iu)\\bτζαρβυς\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάρβυς\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζαρβεις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάρβεις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζαρβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάρβης\\b", "J.A.R.V.I.S.")

                // Missing / changed letters
                .replaceAll("(?iu)\\bτζαβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζαβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζαβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζάβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζαβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζάβης\\b", "J.A.R.V.I.S.")

                // βρ / ρβ confusion
                .replaceAll("(?iu)\\bτζαβρις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάβρις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζαβρης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάβρης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζαβρις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζάβρις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζαβρης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζάβρης\\b", "J.A.R.V.I.S.")

                // μπ / β mistakes
                .replaceAll("(?iu)\\bτζαμπις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάμπις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζαμπης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάμπης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζαμπις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζάμπις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζαμπης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bζάμπης\\b", "J.A.R.V.I.S.")

                // Android / Google STT weird outputs
                .replaceAll("(?iu)\\bντερμις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bντέρμις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζερβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζέρβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζερβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζέρβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζορβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζόρβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζορβης\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζόρβης\\b", "J.A.R.V.I.S.")

                // Split words
                .replaceAll("(?iu)\\bτζαρ βισ\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάρ βισ\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζαρ βις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζάρ βις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτζα ρβις\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bτ ζαρβις\\b", "J.A.R.V.I.S.")

                .replaceAll("(?iu)^\\s*ζητάς\\s*[,.!?;:]*\\s*", "J.A.R.V.I.S. ")
                .replaceAll("(?iu)^\\s*ζητας\\s*[,.!?;:]*\\s*", "J.A.R.V.I.S. ")
                .replaceAll("(?iu)^\\s*ζήτας\\s*[,.!?;:]*\\s*", "J.A.R.V.I.S. ")

                // Already written with dots/spaces
                .replaceAll("(?iu)\\bj\\.a\\.r\\.v\\.i\\.s\\.\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bj a r v i s\\b", "J.A.R.V.I.S.")
                .replaceAll("(?iu)\\bj\\. a\\. r\\. v\\. i\\. s\\.\\b", "J.A.R.V.I.S.");
    }
}