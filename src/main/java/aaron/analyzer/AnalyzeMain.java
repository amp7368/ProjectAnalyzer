package aaron.analyzer;

import aaron.analyzer.algorithm.AnalyzeAlgorithm;
import aaron.analyzer.bridge.AllQuests;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;

import java.io.IOException;

public class AnalyzeMain {
    public static void main(String[] args) throws IOException {
        AllQuests.initialize("test.csv");
        ProjectGroup answer = AnalyzeAlgorithm.whichGivenTime(AllQuests.ALL_PROJECTS, 200);
        System.out.println("Worth gained = " + answer.worth());
        for (ProjectLinked project : answer.getProjects()) {
            System.out.print(project.getName() + ", ");
        }
    }
}
