package aaron.analyzer;

import aaron.analyzer.algorithm.AnalyzeAlgorithm;
import aaron.analyzer.bridge.AllQuests;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;

import java.io.IOException;

public class AnalyzeMain {
    public static void main(String[] args) throws IOException {
        AllQuests.initialize("test.csv");
        ProjectGroup answer = AnalyzeAlgorithm.whichGivenTime(AllQuests.ALL_PROJECTS, 30, 30);
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
    }
}
