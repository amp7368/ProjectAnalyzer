package aaron.analyzer.user_interface.input.commands;

import aaron.analyzer.user_interface.input.Prompts;

class ExplainCsv {
    static void dealWithCommand(String input) {
        System.out.println(Prompts.SEPARATOR);
        System.out.println("The projects must not contain any infinite project loops where A requires B and B requires A");
        System.out.println("The .csv file should be formatted with the headers:");
        System.out.println("name,duration,value,requirements,workers");
        System.out.println("Such that the following is true:");
        System.out.println("'name' is the arbitrary name of the project.");
        System.out.println("'duration' is how long the project is expected to take given that all workers are working on it.");
        System.out.println("'value' is the value that the project provides.");
        System.out.println("'requirements' is a comma separated list of all the prerequisites for this project.\n" +
                Prompts.TAB + "Note: These prerequisites must be spelled exactly the same as the 'name' of the project it is referencing. Commas in the name will obviously break things.");
        System.out.println("'workers' is the number of workers the project requires.");
        System.out.println(Prompts.SEPARATOR);
    }
}
