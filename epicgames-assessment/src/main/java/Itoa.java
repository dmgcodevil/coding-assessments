public class Itoa {

    private Itoa() { }


    public static String itoa(int value, int base) {
        if (value == 0) return "0";
        StringBuilder res = new StringBuilder();
        boolean negative = value < 0;
        if (negative) value = -value;

        while (value > 0) {
            int rem = value % base;
            if (rem > 9) {
                res.append((char) (rem - 10 + 'a')); //  base 16
            } else {
                res.append(rem);
            }
            value = value / base;
        }
        if (negative) res.append("-");
        return res.reverse().toString();
    }
}
