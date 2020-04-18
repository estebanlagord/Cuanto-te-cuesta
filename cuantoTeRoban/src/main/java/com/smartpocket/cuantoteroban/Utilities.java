package com.smartpocket.cuantoteroban;

import android.content.Context;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class Utilities {

    public static final int FRACTION_DIGITS = 2;

    /**
     * this is needed because DecimalFormat.setRoundingMode() was introduced in Android API level 9
     *
     * @param number
     * @param numDigitsToShow
     * @return
     */
    public static double round(double number, int numDigitsToShow) {
        double result;

        if (Double.isNaN(number) || Double.isInfinite(number))
            number = 0;

        BigDecimal bigDecimal = new BigDecimal(number);
        BigDecimal roundedBigDecimal = bigDecimal.setScale(numDigitsToShow, RoundingMode.HALF_UP);
        result = roundedBigDecimal.doubleValue();

        return result;
    }

    public static String removeAccentsAndMakeLowercase(String str) {
        if (str == null)
            return null;

        String result = str.trim().toLowerCase(Locale.US);

        result = result.replaceAll("[áäâàã]", "a");
        result = result.replaceAll("[éëêè]", "e");
        result = result.replaceAll("[íïîì]", "i");
        result = result.replaceAll("[óöôò]", "o");
        result = result.replaceAll("[úüûù]", "u");
        result = result.replaceAll("[ñ]", "n");

        return result;
    }

    @Deprecated
    public static void showToast(String text) {
        Context context = MyApplication.Companion.applicationContext();
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static DecimalFormat getCurrencyFormat() {
        DecimalFormat result = (DecimalFormat) DecimalFormat.getInstance();
        result.setMinimumFractionDigits(FRACTION_DIGITS);
        result.setMaximumFractionDigits(FRACTION_DIGITS);
        result.setPositivePrefix("$ ");
        return result;
    }

    public static SimpleDateFormat getDateFormat() {
        SimpleDateFormat result = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        result.setTimeZone(TimeZone.getTimeZone("UTC"));
        return result;
    }
}
