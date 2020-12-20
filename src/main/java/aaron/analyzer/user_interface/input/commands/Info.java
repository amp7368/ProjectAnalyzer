package aaron.analyzer.user_interface.input.commands;

import aaron.analyzer.user_interface.input.Prompts;

public class Info {
    public static void algorithm(String input) {
        System.out.println("I attempt to maximize the total worth given a time constraint and the number of workers provided");
        System.out.println("1. Split the singleton projects from the complex projects");
        System.out.println("2. Find the starter projects from the complex projects");
        System.out.println("3. Add one complex projects to each of the starter projects from the projects the require the current project recursively");
        System.out.println(Prompts.TAB + "If B requires A, and C requires A, then I start with A, then add B, then add C, then add B and C. So I end with A, AB, AC, ABC");
        System.out.println(Prompts.TAB + Prompts.TAB + "It's actually a little more complicated because I add the prerequisites of the new project as well, and I only continue if it's possible for the combination to exist.");
        System.out.println("4. Try different combinations of the complex project chains.");
        System.out.println(Prompts.TAB + "The time complexity for this would be factorial, so this is a a greedy algorithm");
        System.out.println(Prompts.TAB + Prompts.TAB + "I just add the greatest worth/time complex projectLines");
        System.out.println(Prompts.TAB + Prompts.TAB + "This isn't that terrible because the best solution is probably going to maximize worth/time for all subcomponents.");
        System.out.println(Prompts.TAB + Prompts.TAB + "Also, the only solutions that would be better than a solution that maximizes worth/time for subcomponents would be a project");
        System.out.println(Prompts.TAB + Prompts.TAB + "that makes more use of the time provided. This is actually a worse solution in practice even though it gives a better result");
        System.out.println(Prompts.TAB + Prompts.TAB + "If there was any delay or quicker projects, then you're making the most of the time with my algorithm because your amount/time is likely to be greater than the other solution that uses all possible time");
        System.out.println(Prompts.TAB + Prompts.TAB + "But at any rate, the difference between the two algorithms are so incredibly minute, it really doesn't matter in the slightest.");
        System.out.println("5. Add the singleton projects to these groups in order of amount/time. I can make the same argument I made before for the same greedy algorithm.");
        System.out.println("6. Sort the groups to find the combination that maximizes worth");
    }
}
