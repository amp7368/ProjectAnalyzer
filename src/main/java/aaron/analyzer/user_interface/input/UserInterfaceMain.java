package aaron.analyzer.user_interface.input;

import aaron.analyzer.algorithm.AnalyzeAlgorithm;
import aaron.analyzer.bridge.AllQuests;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;
import aaron.analyzer.user_interface.input.commands.AllCommands;
import aaron.analyzer.user_interface.output.OutputCsv;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class UserInterfaceMain {

    public static final String PREFIX = "$";

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);

        // Give an introduction to how to get help
        System.out.println(Prompts.SEPARATOR);
        System.out.println(Prompts.SEPARATOR);
        System.out.println(Prompts.SEPARATOR);

        System.out.println("Introduction");
        System.out.println("Type \"$help\" at any point for a list of commands.");
        System.out.println(PREFIX + " is the prefix for all commands");

        System.out.println(Prompts.SEPARATOR);

        String relativePath = getFilePath(in, "What is the relative/absolute path to the csv file that holds all the projects? \"" + PREFIX + "csv\" to learn about how this should be formatted.");
        AllQuests.initialize(relativePath);

        int timeToSpend = getInt(in, "How many time units are there to spend working in total?");

        int workersCount = getInt(in, "How many workers are there working on these projects at any given time?");

        System.out.println("Thank you. I'll start working on this now. Time units of updates are made in milliseconds.");
        System.out.println(Prompts.SEPARATOR);
        System.out.println(Prompts.SEPARATOR);
        System.out.println(Prompts.SEPARATOR);

        ProjectGroup answer = AnalyzeAlgorithm.whichGivenTime(AllQuests.ALL_PROJECTS, timeToSpend, workersCount);
        if (answer == null) {
            System.out.println("answer is null");
            return;
        }
        System.out.println("Worth gained = " + answer.worth());
        System.out.println("Time spent = " + answer.time());
        System.out.println("# of projects = " + answer.getProjects().size());
        for (ProjectLinked project : answer.getProjects()) {
            System.out.print(project.getName() + ", ");
        }
        System.out.println();
        int[][] timeline = answer.getProjectTimeline();
        for (int[] workerTimeline : timeline) {
            for (int day : workerTimeline) {
                System.out.printf(" %3d ", day);
            }
            System.out.println();
        }
        OutputCsv.out(answer,timeline);
    }

    private static int getInt(Scanner in, String prompt) {
        int result;
        while (true) {
            String next = dealWithInput(in, prompt);
            try {
                result = Integer.parseInt(next);
                break;
            } catch (NumberFormatException e) {
                System.out.printf("'%s' doesn't seem to be an integer.\n", next);
            }
        }
        return result;
    }

    private static String getFilePath(Scanner in, String prompt) {
        String relativePath;
        while (true) {
            relativePath = dealWithInput(in, prompt);
            if (new File(relativePath).isFile())
                break;
            else
                System.out.printf("'%s' doesn't seem to be a file path.\n", relativePath);
        }
        return relativePath;
    }

    private static String dealWithInput(Scanner in, String prompt) {
        while (true) {
            System.out.println(prompt);
            String next = in.nextLine();
            System.out.println("'" + next + "'");
            if (next.toLowerCase().startsWith(PREFIX)) {
                dealWithCommand(next);
            } else {
                return next;
            }
        }
    }

    private static void dealWithCommand(String input) {
        String lowercase = input.toLowerCase();
        for (AllCommands command : AllCommands.values()) {
            if (command.isCommand(lowercase)) {
                command.dealWithCommand(input);
                return;
            }
        }
        System.out.println("There are no commands that match what you said. Please refer to " + PREFIX + "help if you need help.");
    }
}
