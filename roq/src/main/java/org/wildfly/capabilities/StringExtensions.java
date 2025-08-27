package org.wildfly.capabilities;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension(namespace = "str")
public class StringExtensions {
    public static String formatCapability(String text) {
        StringBuilder sb = new StringBuilder();
        if (text != null) {
            boolean inPre = false;
            for (var line : text.split("\n")) {
                if (!inPre) {
                    sb.append("<p>");
                }
                if (line.startsWith(" ")) {
                    if (!inPre) {
                        sb.append("<pre>");
                        inPre = true;
                    }
                    sb.append(line).append("\n");
                } else {
                    if (inPre) {
                        sb.append("</pre>");
                        inPre = false;
                    }
                    sb.append(line);
                }

                if (!inPre) {
                    sb.append("</p>");
                }
            }
        }

        return sb.toString();
    }
}
