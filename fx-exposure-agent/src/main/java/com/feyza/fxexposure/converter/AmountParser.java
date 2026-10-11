package com.feyza.fxexposure.converter;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extrait un montant d'une réponse textuelle ("223.49", "223,49 euros", "1 234,56", "1.234,56"...). */
public final class AmountParser {

    private static final Pattern NUMBER = Pattern.compile("\\d[\\d\\s\\u00A0\\u202F.,]*");

    private AmountParser() {}

    public static BigDecimal parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Réponse vide");
        }
        Matcher m = NUMBER.matcher(text);
        if (!m.find()) {
            throw new IllegalArgumentException("Aucun montant trouvé dans la réponse : " + text);
        }
        String raw = m.group().replaceAll("[\\s\\u00A0\\u202F]", "").replaceAll("[.,]+$", "");

        int lastDot = raw.lastIndexOf('.');
        int lastComma = raw.lastIndexOf(',');
        if (lastDot >= 0 && lastComma >= 0) {
            boolean dotIsDecimal = lastDot > lastComma;
            raw = raw.replace(dotIsDecimal ? "," : ".", "");
            if (!dotIsDecimal) {
                raw = raw.replace(',', '.');
            }
        } else if (lastComma >= 0) {
            raw = raw.chars().filter(c -> c == ',').count() == 1 ? raw.replace(',', '.') : raw.replace(",", "");
        } else if (raw.chars().filter(c -> c == '.').count() > 1) {
            raw = raw.replace(".", "");
        }
        return new BigDecimal(raw);
    }
}
