package aaron.analyzer.user_interface.input.commands;

import aaron.analyzer.user_interface.input.Prompts;

import java.util.Arrays;
import java.util.List;

class HelpCommands {
    static void dealWithHelp(String input) {
        System.out.println(Prompts.SEPARATOR);
        System.out.println("Help:");
        List<String> commands = Arrays.asList(
                "help           == gives this help message",
                "csv            == explains the format of the project file",
                "info algorithm == gives info about the major algorithm",
                "info parallel  == gives info about the parallelism algorithm"
        );
        commands = Prompts.tabify(Prompts.prefixify(commands));
        for (String command : commands)
            System.out.println(command);
        System.out.println(Prompts.SEPARATOR);
    }
}
