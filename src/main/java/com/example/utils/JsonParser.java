package com.example.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    public static class Token {
        String type;
        String value;

        Token(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String toString() {
            return type + ": " + value;
        }
    }
    static boolean isBooleanTrue(String value) {
        return "true".equals(value);
    }

    static boolean isBooleanFalse(String value) {
        return "false".equals(value);
    }

    static boolean isNull(String value) {
        return "null".equals(value);
    }

    static boolean isNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static List<Token> getTokens(String input) {
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (ch == '{') {
                tokens.add(new Token("CurlyOpen", "{"));
            } else if (ch == '}') {
                tokens.add(new Token("CurlyClose", "}"));
            } else if (ch == '[') {
                tokens.add(new Token("SquareOpen", "["));
            } else if (ch == ']') {
                tokens.add(new Token("SquareClose", "]"));
            } else if (ch == ':') {
                tokens.add(new Token("Colon", ":"));
            } else if (ch == ',') {
                tokens.add(new Token("Comma", ","));
            } else if (ch == '"') {
                StringBuilder value = new StringBuilder();
                value.append(ch);
                i++;
                while (i < input.length() && input.charAt(i) != '"') {
                    value.append(input.charAt(i));
                    i++;
                }
                value.append('"');
                tokens.add(new Token("String", value.toString()));
            } else if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '+') {
                StringBuilder value = new StringBuilder();
                while (i < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(i)) ||
                         input.charAt(i) == '.' ||
                         input.charAt(i) == '-' ||
                         input.charAt(i) == '+' ||
                         input.charAt(i) == 'e' || input.charAt(i) == 'E')) {
                    value.append(input.charAt(i));
                    i++;
                }
                i--; // Step back
                String valStr = value.toString();
                if (isNumber(valStr)) tokens.add(new Token("Number", valStr));
                else if (isBooleanTrue(valStr)) tokens.add(new Token("True", valStr));
                else if (isBooleanFalse(valStr)) tokens.add(new Token("False", valStr));
                else if (isNull(valStr)) tokens.add(new Token("Null", valStr));
                else throw new RuntimeException("Unexpected value: " + valStr);
            } else if (Character.isWhitespace(ch)) {
                continue;
            }
        }
        return tokens;
    }
    public static class Parser {
        List<Token> tokens;
        int current;

        public Parser(List<Token> tokens) {
            this.tokens = tokens;
            this.current = 0;
        }

        public Object parse() {
            return walk();
        }

        Object walk() {
            Token token = tokens.get(current);

            switch (token.type) {
                case "String":
                    current++;
                    return token.value.substring(1, token.value.length() - 1);

                case "Number":
                    current++;
                    return Double.parseDouble(token.value);

                case "True":
                    current++;
                    return true;

                case "False":
                    current++;
                    return false;

                case "Null":
                    current++;
                    return null;

                case "SquareOpen": {
                    current++; // skip '['
                    List<Object> arr = new ArrayList<>();
                    while (!tokens.get(current).type.equals("SquareClose")) {
                        arr.add(walk());
                        if (current < tokens.size() && tokens.get(current).type.equals("Comma")) {
                            current++; // skip comma
                        }
                    }
                    current++; // skip ']'
                    return arr;
                }

                case "CurlyOpen": {
                    current++; // skip '{'
                    Map<String, Object> obj = new LinkedHashMap<>();

                    while (!tokens.get(current).type.equals("CurlyClose")) {
                        Token keyToken = tokens.get(current++);
                        String key = keyToken.value.substring(1, keyToken.value.length() - 1);

                        if (!tokens.get(current).type.equals("Colon")) {
                            throw new RuntimeException("Expected colon after key");
                        }
                        current++; // skip ':'

                        Object value = walk();
                        obj.put(key, value);

                        if (current < tokens.size() && tokens.get(current).type.equals("Comma")) {
                            current++; // skip comma
                        }
                    }
                    current++; // skip '}'
                    return obj;
                }

                default:
                    throw new RuntimeException("Unexpected token: " + token);
            }
        }
    }
}
