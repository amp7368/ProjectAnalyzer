package aaron.analyzer.user_interface.input;

import java.util.ArrayList;
import java.util.List;

import static aaron.analyzer.user_interface.input.UserInterfaceMain.PREFIX;

public class Prompts {
    public static final String SEPARATOR = "-------------------";
    public static final String TAB = "   ";

    public static String tabify(String s) {
        return TAB + s;
    }

    public static String prefixify(String s) {
        return PREFIX + s;
    }

    public static List<String> prefixify(List<String> ss) {
        List<String> out = new ArrayList<>(ss.size());
        for (String s : ss) {
            out.add(PREFIX + s);
        }
        return out;
    }

    public static List<String> tabify(List<String> ss) {
        List<String> out = new ArrayList<>(ss.size());
        for (String s : ss) {
            out.add(TAB + s);
        }
        return out;
    }
}
