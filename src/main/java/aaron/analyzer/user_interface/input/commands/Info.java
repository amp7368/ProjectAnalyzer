package aaron.analyzer.user_interface.input.commands;

import static aaron.analyzer.user_interface.input.Prompts.SEPARATOR;
import static aaron.analyzer.user_interface.input.Prompts.TAB;

public class Info {
    public static void algorithm(String input) {
        System.out.println("I attempt to maximize the total worth given a time constraint and the number of workers provided");
        System.out.println("1. Split the singleton projects from the complex projects");
        System.out.println("2. Find the starter projects from the complex projects");
        System.out.println("3. Add one complex projects to each of the starter projects from the projects the require the current project recursively");
        System.out.println(TAB + "If B requires A, and C requires A, then I start with A, then add B, then add C, then add B and C. So I end with A, AB, AC, ABC");
        System.out.println(TAB + TAB + "It's actually a little more complicated because I add the prerequisites of the new project as well, and I only continue if it's possible for the combination to exist.");
        System.out.println("4. Try different combinations of the complex project chains.");
        System.out.println(TAB + "The time complexity for this would be factorial, so this is a a greedy algorithm");
        System.out.println(TAB + TAB + "I just add the greatest worth/time complex projectLines");
        System.out.println(TAB + TAB + "This isn't that terrible because the best solution is probably going to maximize worth/time for all subcomponents.");
        System.out.println(TAB + TAB + "Also, the only solutions that would be better than a solution that maximizes worth/time for subcomponents would be a project");
        System.out.println(TAB + TAB + "that makes more use of the time provided. This is actually a worse solution in practice even though it gives a better result");
        System.out.println(TAB + TAB + "If there was any delay or quicker projects, then you're making the most of the time with my algorithm because your amount/time is likely to be greater than the other solution that uses all possible time");
        System.out.println(TAB + TAB + "But at any rate, the difference between the two algorithms are so incredibly minute, it really doesn't matter in the slightest.");
        System.out.println("5. Add the singleton projects to these groups in order of amount/time. I can make the same argument I made before for the same greedy algorithm.");
        System.out.println("6. Sort the groups to find the combination that maximizes worth");
    }

    public static void parallelism(String input) {
        System.out.println("I make it so multiple projects can be completed at the same time because of multiple workers");
        System.out.println("This details how a ProjectGroup works and how projects are added.");
        System.out.println("This provides an imperfect solution I believe, but it's works surprisingly well.");
        System.out.println("To save space in memory, I only record the 'mold' between the clean time and the previously filled");
        System.out.println("Assumptions: Any filled space has all filled spaces to the left. Any not filled space has no filled spaces to the right.");
        System.out.println("example: (the space between the lines is what I store in memory at any given time");
        System.out.println("Add project E which has 2 duration and 1 requiredWorker.");
        System.out.println(SEPARATOR);
        System.out.println("Worker1: A A|A B|empty");
        System.out.println("Worker2: A A|A  |empty");
        System.out.println("Worker3: C D|D  |empty");
        System.out.println(SEPARATOR);
        System.out.println("1. Find the earliest spot that has at least the number of free workers required by the project.");
        System.out.println(TAB + "To add project E, we see that there the mold has enough space for 1 worker");
        System.out.println(TAB + "To add project E, we add another col of the mold to support the timeframe of the project.");
        System.out.println(TAB + "To add project E, we would also check that all prerequisites have been met by the time we add the project.");
        System.out.println("2. If it is impossible to place the new project in the current situation, 'rehash' the group to make sure it's impossible");
        System.out.println("3. To rehash, remove all projects from the group, and add the biggest projects one at a time (biggest meaning most amount of workers first)");
        System.out.println("4. To retrieve the full timeline, add the projects one at a time in the order they were put in, and this will give you a full timeline if you take snapshots of the mold");
    }
}
