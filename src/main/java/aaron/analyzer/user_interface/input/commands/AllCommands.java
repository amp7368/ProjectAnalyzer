package aaron.analyzer.user_interface.input.commands;

import aaron.analyzer.user_interface.input.UserInterfaceMain;

import java.util.function.Consumer;

public enum AllCommands {
    HELP("help", HelpCommands::dealWithHelp),
    CSV("csv", ExplainCsv::dealWithCommand),
    INFO_ALGORITHM("info algorithm", Info::algorithm),
    INFO_PARALLELISM("info parallel", Info::parallelism);


    private final Consumer<String> method;
    private final String name;

    AllCommands(String name, Consumer<String> dealWithCommand) {
        this.method = dealWithCommand;
        this.name = name;
    }

    public boolean isCommand(String input) {
        return input.startsWith(UserInterfaceMain.PREFIX + name);
    }

    public void dealWithCommand(String input) {
        method.accept(input);
    }
}
