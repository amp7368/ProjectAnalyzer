package aaron.analyzer;

import aaron.analyzer.algorithm.AnalyzeAlgorithm;
import aaron.analyzer.bridge.AllProjects;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;
import aaron.analyzer.user_interface.output.OutputCsv;

import java.io.IOException;

public class AnalyzeMain {
    public static void main(String[] args) throws IOException {
        AllProjects.initialize("test.csv");
        ProjectGroup answer = AnalyzeAlgorithm.whichGivenTime(AllProjects.ALL_PROJECTS, 30, 15);
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
        OutputCsv.out(answer, timeline);
    }
}
