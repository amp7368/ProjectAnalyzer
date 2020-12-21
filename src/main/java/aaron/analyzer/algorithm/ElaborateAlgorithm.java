package aaron.analyzer.algorithm;

import aaron.analyzer.bridge.Project;
import aaron.analyzer.bridge.ProjectLinked;

import java.util.*;

public class ElaborateAlgorithm {
    /**
     * sort the projects into two categories. complex, and singleton
     * Extend the information of each complex project to include what requires them
     *
     * @param projects the projects to sort into complex and singleton
     * @return the split projects
     */
    public static ReturnSingleComplex sortQuestsToComplexSingleton(Collection<Project> projects) {
        Map<Integer, ProjectLinked> allProjects = new HashMap<>();
        for (Project project : projects) allProjects.put(project.getUid(), new ProjectLinked(project));

        for (Project project : projects) {
            for (Integer req : project.getImmediateRequirements()) {
                ProjectLinked complexProject = allProjects.get(req);
                complexProject.addRequireMe(project.getUid());
            }
        }
        // sort between complex and singleton
        Map<Integer, ProjectLinked> complexProjects = new HashMap<>();
        List<ProjectLinked> singletonProjects = new ArrayList<>();
        for (ProjectLinked project : allProjects.values()) {
            if (project.getImmediateRequirements().length == 0 && project.getRequireMe().isEmpty())
                singletonProjects.add(project);
            else
                complexProjects.put(project.getUid(), project);
        }

        // find the startingComplexProjects
        Set<ProjectLinked> startingComplexProjects = new HashSet<>();
        for (ProjectLinked project : complexProjects.values()) {
            if (project.isImmediateRequirementsEmpty() && !project.getRequireMe().isEmpty()) {
                startingComplexProjects.add(project); // this is a starter complexProject
            }
        }

        Sorting.sortQuestsByAPT(singletonProjects);

        return new ReturnSingleComplex(singletonProjects, complexProjects, startingComplexProjects);
    }

    public static class ReturnSingleComplex {
        public final List<ProjectLinked> singletonProjects;
        public final Map<Integer, ProjectLinked> complexProjects;
        public final Set<ProjectLinked> startingComplexProjects;

        private ReturnSingleComplex(List<ProjectLinked> singletonProjects, Map<Integer, ProjectLinked> complexProjects, Set<ProjectLinked> startingComplexProjects) {
            this.singletonProjects = singletonProjects;
            this.complexProjects = complexProjects;
            this.startingComplexProjects = startingComplexProjects;
        }
    }
}
