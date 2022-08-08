package nl.andrewl.aos2_client.util;

public class StringUtils {
	public static String format(float f, int n, StringBuffer sb) {
		int factor = 1;
		for (int i = 0; i < n; i++) factor *= 10;
		int digits = Math.round(f * factor);
		sb.delete(0, sb.length());
		// Special case for f == 0.
		if (digits == 0) {
			sb.append('0');
			if (n > 0) {
				sb.append('.');
				sb.append("0".repeat(n));
			}
			return sb.toString();
		}
		boolean negated = digits < 0;
		if (negated) digits *= -1;
		int idx = 0;
		int digits2 = digits;
		while (idx < n) {
			sb.append(digits2 % 10);
			digits2 /= 10;
			idx++;
			if (idx == n) {
				sb.append('.');
				if (digits2 == 0) sb.append(0);
			}
		}
		int whole = digits / factor;
		while (whole > 0) {
			sb.append(whole % 10);
			whole /= 10;
		}
		if (negated) sb.append('-');
		sb.reverse();
		return sb.toString();
	}

	private static final StringBuffer buffer = new StringBuffer(32);

	public static String format(float f, int n) {
		return format(f, n, buffer);
	}
}
