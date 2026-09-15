package com.dunamis.world.lspalm.util;

import java.util.Random;

/**
 * 字符串处理工具
 */
public final class StringUtils {

    public static final String EMPTY = "";
    private static final Random RANDOM = new Random();

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(final CharSequence cs) {
        return null == cs || cs.length() == 0;
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * 判断字符串是否为空或为空格字符串
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (null == cs || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 去掉字符串首尾空格
     */
    public static String trim(final String str) {
        return null == str ? null : str.trim();
    }

    /**
     * 如果字符串为null则默认返回空字符串
     */
    public static String defaultString(final String str) {
        return null == str ? EMPTY : str;
    }

    /**
     * 如果字符串为null则返回默认字符串
     */
    public static String defaultString(final String str, final String defaultStr) {
        return null == str ? defaultStr : str;
    }

    /**
     * 获取随机字符串
     *
     * @param count   字符数量
     * @param letters 是否包含字母
     * @param numbers 是否包含数字
     */
    public static String random(int count, final boolean letters, final boolean numbers) {
        if (count == 0) {
            return EMPTY;
        } else if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }

        int start = 0;
        int end;
        if (!letters && !numbers) {
            end = Integer.MAX_VALUE;
        } else {
            end = 'z' + 1;
            start = ' ';
        }

        final char[] buffer = new char[count];
        final int gap = end - start;

        while (count-- != 0) {
            char ch = (char) (RANDOM.nextInt(gap) + start);
            if (letters && Character.isLetter(ch)
                    || numbers && Character.isDigit(ch)
                    || !letters && !numbers) {
                if (ch >= 56320 && ch <= 57343) {
                    if (count == 0) {
                        count++;
                    } else {
                        // low surrogate, insert high surrogate after putting it in
                        buffer[count] = ch;
                        count--;
                        buffer[count] = (char) (55296 + RANDOM.nextInt(128));
                    }
                } else if (ch >= 55296 && ch <= 56191) {
                    if (count == 0) {
                        count++;
                    } else {
                        // high surrogate, insert low surrogate before putting it in
                        buffer[count] = (char) (56320 + RANDOM.nextInt(128));
                        count--;
                        buffer[count] = ch;
                    }
                } else if (ch >= 56192 && ch <= 56319) {
                    // private high surrogate, no effing clue, so skip it
                    count++;
                } else {
                    buffer[count] = ch;
                }
            } else {
                count++;
            }
        }
        return new String(buffer);
    }

    /**
     * 以指定的分隔符来进行字符串元素连接
     * <p>
     * 例如有字符串数组array和连接符为逗号(,)
     * <code>
     * String[] array = new String[] { "hello", "world", "qiniu", "cloud","storage" };
     * </code>
     * 那么得到的结果是:
     * <code>
     * hello,world,qiniu,cloud,storage
     * </code>
     * </p>
     *
     * @param array 需要连接的字符串数组
     * @param sep   元素连接之间的分隔符
     * @return 连接好的新字符串
     */
    public static String join(String[] array, String sep) {
        if (array == null) {
            return null;
        }

        int arraySize = array.length;
        int sepSize = 0;
        if (sep != null && !"".equals(sep)) {
            sepSize = sep.length();
        }

        int bufSize = (arraySize == 0 ? 0 : ((array[0] == null ? 16 : array[0].length()) + sepSize) * arraySize);
        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(sep);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    private StringUtils() {
    }
}
