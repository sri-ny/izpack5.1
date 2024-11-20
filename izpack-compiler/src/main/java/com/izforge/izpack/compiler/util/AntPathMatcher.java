package com.izforge.izpack.compiler.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PathMatcher implementation for Ant-style path patterns. Examples are provided below.
 *
 * <p>Part of this mapping code has been kindly borrowed from <a href="http://ant.apache.org">Apache Ant</a>.
 *
 * <p>The mapping matches URLs using the following rules:<br> <ul> <li>? matches one character</li> <li>* matches zero
 * or more characters</li> <li>** matches zero or more 'directories' in a path</li> </ul>
 *
 * <p>Some examples:<br>
 * <ul>
 * <li><code>bin/t?st.exe</code> - matches <code>bin/test.exe</code> but also <code>bin/tast.exe</code>
 *     or <code>bin/txst.exe</code></li>
 * <li><code>bin/*.exe</code> - matches all <code>.exe</code> files in the <code>bin</code> directory</li>
 * <li><code>bin/&#42;&#42;/test.exe</code> - matches all <code>test.exe</code> files underneath the <code>bin</code> path</li>
 * </ul>
 */
public class AntPathMatcher {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^/]+?)\\}");

    /**
     * Match the given <code>path</code> against the given <code>pattern</code>,
     * according to this PathMatcher's matching strategy.
     * @param pattern the pattern to match against
     * @param path the path String to test
     * @param caseSensitive whether the test should be case-sensitive
     * @return <code>true</code> if the supplied <code>path</code> matched,
     * <code>false</code> if it didn't
     */
    public boolean match(String pattern, String path, boolean caseSensitive) {

        pattern = pattern.replaceAll("\\\\", "/");
        pattern = pattern.replaceAll("\\.", "\\\\.");
        pattern = pattern.replaceAll("\\*", "[^/]*");
        pattern = pattern.replaceAll("(\\[\\^/\\]\\*){2}", ".*");
        pattern = pattern.replaceAll("/\\.\\*", "(/.*)*");
        pattern = unifyVarReferences(pattern);
        pattern = pattern.replaceAll("\\$", "\\\\\\$");

        path = unifyVarReferences(path);

        int flags = 0;
        if (!caseSensitive)
        {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        Pattern p = Pattern.compile(pattern, flags);

        return p.matcher(path).matches();
    }

    /**
     * This function unifies variable references by replacing references in the form of ${...} with $...
     *
     * @param input the string that may contain variable references to be replaced
     * @return the input string after unifying the variable references
     */
    private String unifyVarReferences(String input) {
      Matcher m = VAR_PATTERN.matcher(input);
      StringBuffer s = new StringBuffer();
      while (m.find()) {
        m.appendReplacement(s, "\\$"+m.group(1));
      }
      m.appendTail(s);
      return s.toString();
    }
}
