package aaron.analyzer.algorithm;

import aaron.analyzer.bridge.Project;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;

import java.util.*;

public class AnalyzeAlgorithm {

    public static ProjectGroup whichGivenTime(Collection<Project> projects, long timeToSpend) {
        ReturnSingleComplex singleAndComplex = sortQuestsToComplexSingleton(projects);
        List<ProjectLinked> sortedSingletonProjects = singleAndComplex.singletonProjects;
        Map<Integer, ProjectLinked> uidToComplexProjects = singleAndComplex.complexProjects;
        Set<ProjectLinked> allComplexStarterProjects = singleAndComplex.startingComplexProjects;

        // get all the starter complex projects and add one quest at a time
        Set<Set<ProjectLinked>> allProjectLines = new HashSet<>();
        for (ProjectLinked starter : allComplexStarterProjects) {
            if (isTimeOkay(timeToSpend, Collections.emptyList(), starter))
                allProjectLines.add(new HashSet<>() {{
                    add(starter);
                }});
        }
        // add a project at a time and at each step, save that combo
        addQuestGivenTime(allProjectLines, uidToComplexProjects, timeToSpend);

        // todo idk if this is necessary
        allProjectLines.removeIf(Set::isEmpty);

        List<Set<ProjectLinked>> allProjectLinesSorted = Sorting.sortQuestCombinationByAPT(allProjectLines);

        Set<ProjectGroup> finalProjectCombinations = new HashSet<>();
        for (Set<ProjectLinked> projectLine : allProjectLines) {
            finalProjectCombinations.add(new ProjectGroup(projectLine));
        }
        addProjectGroupGivenTime(finalProjectCombinations, allProjectLinesSorted, timeToSpend);
        finalProjectCombinations.add(new ProjectGroup()); // for no complex projects in the group

        // add singletons to fill up every questline to as many as it can hold to stay within the time constraint
        for (ProjectGroup projectGroup : finalProjectCombinations) {
            for (ProjectLinked singleton : sortedSingletonProjects) {
                if (projectGroup.isTimeOkay(timeToSpend, singleton)) {
                    projectGroup.addProject(singleton);
                }
            }
        }
        List<ProjectGroup> projectsSorted = new ArrayList<>(finalProjectCombinations);
        Sorting.sortProjectGroupsByAmount(projectsSorted);
        return projectsSorted.get(0);
    }

    /**
     * recursively add a single projectGroup to the projectLines and save at each step
     *
     * @param finalProjectCombinations the final set of combinations of projects that we add to
     * @param allProjectLines          the complex projectLines that we make a recursive cross product of
     * @param timeToSpend              the time that we have to spend doing projects
     */
    private static void addProjectGroupGivenTime(Set<ProjectGroup> finalProjectCombinations, List<Set<ProjectLinked>> allProjectLines, long timeToSpend) {
        final Object sync = new Object();
        Set<ProjectGroup> groupsToAdd = new HashSet<>();
        finalProjectCombinations.parallelStream().forEach(
                oldProjectGroup -> {
                    // for every project in the old project group, add all projectLines that make a difference
                    for (Set<ProjectLinked> newProjectLine : allProjectLines) {
                        ProjectGroup newProjectGroup = new ProjectGroup(oldProjectGroup.getProjects());
                        // if it makes a difference, keep it
                        if (newProjectGroup.addProjectGroup(newProjectLine)) {
                            if (newProjectGroup.isTimeOkay(timeToSpend)) {
                                synchronized (sync) {
                                    groupsToAdd.add(newProjectGroup);
                                }
                            }
                            // we're stopping here because this somewhat greedy algorithm
                            // allows us to not try every combination while minimizing the loss of precision
                            break;
                        }
                    }
                }
        );
        if (!finalProjectCombinations.containsAll(groupsToAdd)) {
            addProjectGroupGivenTime(groupsToAdd, allProjectLines, timeToSpend);
            finalProjectCombinations.addAll(groupsToAdd);
        }


    }

    /**
     * recursively add a single quest to quest lines (recursively meaning we do it until it's unnecessary to do anymore)
     *
     * @param allProjectLines      the final set of combinations of projects that we add to
     * @param uidToComplexProjects the map of projectUid to the projectLinked that the uid refers to
     * @param timeToSpend          the time that we have to spend doing projects
     */
    private static void addQuestGivenTime(Set<Set<ProjectLinked>> allProjectLines, Map<Integer, ProjectLinked> uidToComplexProjects, long timeToSpend) {
        final Object sync = new Object();
        final Set<Set<ProjectLinked>> projectsToAdd = new HashSet<>();

        // for every combination we have, try to add one new entry, and add that combination as a clone
        allProjectLines.parallelStream().forEach(
                projectLine -> {
                    // for every project in the projectLine, add all the reqMe's
                    for (ProjectLinked projectInOldLine : projectLine) {
                        for (Integer reqMe : projectInOldLine.getRequireMe()) {
                            ProjectLinked projectReqMe = uidToComplexProjects.get(reqMe);
                            if (projectLine.contains(projectReqMe)) continue;
                            // add this corresponding projectReqMe
                            Set<ProjectLinked> newProjectCombination = new HashSet<>(projectLine);
                            newProjectCombination.add(projectReqMe);
                            if (isTimeOkay(timeToSpend, newProjectCombination) && !allProjectLines.contains(newProjectCombination)) {
                                synchronized (sync) {
                                    projectsToAdd.add(newProjectCombination);
                                }
                            }
                        }
                    }
                }
        );
        if (!allProjectLines.containsAll(projectsToAdd)) {
            addQuestGivenTime(projectsToAdd, uidToComplexProjects, timeToSpend);
            allProjectLines.addAll(projectsToAdd);
        }
    }

    private static boolean isTimeOkay(long timeToSpend, Collection<ProjectLinked> combination, Project... additional) {
        long timeSpent = 0;
        for (ProjectLinked project : combination) {
            timeSpent += project.getTime();
        }
        for (Project project : additional) {
            timeSpent += project.getTime();
        }
        return timeToSpend >= timeSpent;
    }

    /**
     * sort the projects into two categories. complex, and singleton
     * Extend the information of each complex project to include what requires them
     *
     * @param projects the projects to sort into complex and singleton
     * @return the split projects
     */
    private static ReturnSingleComplex sortQuestsToComplexSingleton(Collection<Project> projects) {
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

    private static class ReturnSingleComplex {
        private final List<ProjectLinked> singletonProjects;
        private final Map<Integer, ProjectLinked> complexProjects;
        private final Set<ProjectLinked> startingComplexProjects;

        public ReturnSingleComplex(List<ProjectLinked> singletonProjects, Map<Integer, ProjectLinked> complexProjects, Set<ProjectLinked> startingComplexProjects) {
            this.singletonProjects = singletonProjects;
            this.complexProjects = complexProjects;
            this.startingComplexProjects = startingComplexProjects;
        }
    }
}
