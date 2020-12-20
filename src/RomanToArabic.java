import java.util.Map;

public class RomanToArabic {
    public static int romanToInt(String roman) {
        final Map<Character, Integer> map = Map.of('I', 1,
                'V', 5,
                'X', 10,
                'L', 50,
                'C', 100,
                'D', 500,
                'M', 1000
        );

        int result = 0;
        for (int index = 0; index < roman.length(); index++) {
            final int curNumber = map.get(roman.charAt(index));

            if (index > 0 && curNumber > map.get(roman.charAt(index - 1))) {
                // add current number & remove previous one twice:
                // first: we add it before (when it was current number) and removing it for this current number
                // second: for correct conversion to roman numbers
                result += curNumber - 2 * map.get(roman.charAt(index - 1));
            } else {
                result += curNumber;
            }
        }
        System.out.printf("%8s -> %4d\n", roman, result);
        return result;
    }

    public static void main(String[] args) {
        String[] romans = {"I", "II", "III", "V", "X", "XIV", "XVII", "XX", "XXV",
                "XXX", "XXXVIII", "XLIX", "LXIII", "LXXXI", "XCVII", "XCVIII", "XCIX",
                "C", "CI", "CCXLVIII", "CCLIII", "DCCXCIX", "MCCCXXV", "MCM", "MM",
                "MMCDLVI", "MDCCXV"};

        for (String roman : romans) {
            romanToInt(roman);
        }

    }
}