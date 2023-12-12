package pl.put.poznan.transformer.logic.transform;

import org.jetbrains.annotations.NotNull;
import pl.put.poznan.transformer.logic.TextTransformer;
import pl.put.poznan.transformer.logic.TextTransformerDecorator;
import pl.put.poznan.transformer.util.DictionaryBuilder;
import pl.put.poznan.transformer.util.PluralVariant;
import pl.put.poznan.transformer.util.PolishPluralVariant;

import java.util.Map;

public class ExpNum extends TextTransformerDecorator {

    private static final Map<Integer, String> DIGITS
            = new DictionaryBuilder<Integer, String>(10).with(0, "zero").with(1, "jeden").with(2, "dwa").with(3, "trzy")
                                                        .with(4, "cztery").with(5, "pięć").with(6, "sześć").with(7, "siedem")
                                                        .with(8, "osiem").with(9, "dziewięć").build();
    private static final Map<Integer, String> TEENS
            = new DictionaryBuilder<Integer, String>(10).with(0, "dziesięć").with(1, "jedenaście").with(2, "dwanaście")
                                                        .with(3, "trzynaście").with(4, "czternaście").with(5, "piętnaście")
                                                        .with(6, "szesnaście").with(7, "siedemnaście").with(8, "osiemnaście")
                                                        .with(9, "dziewiętnaście").build();
    private static final Map<Integer, String> TENS
            = new DictionaryBuilder<Integer, String>(8).with(2, "dwadzieścia").with(3, "trzydzieści").with(4, "czterdzieści")
                                                       .with(5, "pięćdziesiąt").with(6, "sześćdziesiąt").with(7, "siedemdziesiąt")
                                                       .with(8, "osiemdziesiąt").with(9, "dziewięćdziesiąt").build();
    private static final Map<Integer, String> HUNDREDS
            = new DictionaryBuilder<Integer, String>(9).with(1, "sto").with(2, "dwieście").with(3, "trzysta")
                                                       .with(4, "czterysta").with(5, "pięćset").with(6, "sześćset")
                                                       .with(7, "siedemset").with(8, "osiemset").with(9, "dziewięćset")
                                                       .build();
    private static final PluralVariant thousands = new PolishPluralVariant("tysiąc", "tysięcy", "tysiące", "tysiąca");
    private static final PluralVariant millions = new PolishPluralVariant("milion", "milionów", "miliony", "miliona");
    private static final PluralVariant billions = new PolishPluralVariant("miliard", "miliardów", "miliardy", "miliarda");
    private static final PluralVariant trillions = new PolishPluralVariant("bilion", "bilionów", "biliony", "biliona");
    private static final PluralVariant quadrillions = new PolishPluralVariant("biliard", "biliardów", "biliardy", "biliarda");
    private static final PluralVariant quintillions = new PolishPluralVariant("trylion", "trylionów", "tryliony", "tryliona");

    private final boolean numberExpandAllowed;

    public ExpNum(@NotNull TextTransformer textToTransform, boolean numberExpandAllowed) {
        super(textToTransform);
        this.numberExpandAllowed = numberExpandAllowed;
    }

    private static @NotNull String numberInWords(long number) {

        final StringBuilder result = new StringBuilder();

        if (number < 0) {
            result.append("minus ");
            number = -number;
        }

        if (number >= 1_000_000_000_000_000_000L) {
            final long quintillion = number / 1_000_000_000_000_000_000L;
            result.append(quintillions.getQuantityName(quintillion, ExpNum::numberInWords)).append(" ");
            number %= 1_000_000_000_000_000_000L;
        }
        if (number >= 1_000_000_000_000_000L) {
            final long quadrillion = number / 1_000_000_000_000_000L;
            result.append(quadrillions.getQuantityName(quadrillion, ExpNum::numberInWords)).append(" ");
            number %= 1_000_000_000_000_000L;
        }
        if (number >= 1_000_000_000_000L) {
            final long trillion = number / 1_000_000_000_000L;
            result.append(trillions.getQuantityName(trillion, ExpNum::numberInWords)).append(" ");
            number %= 1_000_000_000_000L;
        }
        if (number >= 1_000_000_000L) {
            final long billion = number / 1_000_000_000L;
            result.append(billions.getQuantityName(billion, ExpNum::numberInWords)).append(" ");
            number %= 1_000_000_000L;
        }
        if (number >= 1_000_000L) {
            final long million = number / 1_000_000L;
            result.append(millions.getQuantityName(million, ExpNum::numberInWords)).append(" ");
            number %= 1_000_000L;
        }
        if (number >= 1_000L) {
            final long thousand = number / 1_000L;
            result.append(thousands.getQuantityName(thousand, ExpNum::numberInWords)).append(" ");
            number %= 1_000L;
        }

        final int hundreds = (int) (number / 100);
        if (hundreds > 0) result.append(HUNDREDS.get(hundreds));

        final int remainder = (int) (number % 100);
        if (remainder > 0) {
            if (result.length() > 0) result.append(" ");
            if (remainder < 10) result.append(DIGITS.get(remainder));
            else if (remainder < 20) result.append(TEENS.get(remainder - 10));
            else {
                final int tens = remainder / 10;
                final int units = remainder % 10;
                result.append(TENS.get(tens));
                if (units > 0) result.append(" ").append(DIGITS.get(units));
            }
        }

        return result.toString();
    }

    @Override
    public @NotNull String transform() {
        return function(textToTransform.transform());
    }

    public @NotNull String function(@NotNull String s) {
        if (numberExpandAllowed) {
            final StringBuilder result = new StringBuilder();
            for (String word : s.split(" ")) {
                try {
                    final long number = Long.parseLong(word);
                    result.append(numberInWords(number)).append(" ");
                } catch (NumberFormatException e) {
                    result.append(word).append(" ");
                }
            }

            return result.toString().trim();
        } else {
            return s;
        }
    }
}
