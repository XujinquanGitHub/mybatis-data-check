package github.datacheck.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: 许金泉
 **/
public class StringUtil {


    public static String underlineToHump(String str) {
        StringBuilder result = new StringBuilder();
        String a[] = str.split("_");
        for (String s : a) {
            if (!str.contains("_")) {
                result.append(s);
                continue;
            }
            if (result.length() == 0) {
                result.append(s.toLowerCase());
            } else {
                result.append(s.substring(0, 1).toUpperCase());
                result.append(s.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }


    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * @param str 字符串
     * 驼峰转下划线
     */
    public static String humpToLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
