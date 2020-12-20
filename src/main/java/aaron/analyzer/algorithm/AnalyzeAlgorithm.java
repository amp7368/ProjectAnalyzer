package aaron.analyzer.algorithm;

import aaron.analyzer.bridge.Project;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnalyzeAlgorithm {
    private static final int CHECKPOINTS_COUNT = 4;
    private static int currentCheckpoint = 0;

    @Nullable
    public static ProjectGroup whichGivenTime(Collection<Project> projects, int timeToSpend, int workersCount) {
        long start, absoluteStart;
        absoluteStart = start = System.currentTimeMillis();

        ElaborateAlgorithm.ReturnSingleComplex singleAndComplex = ElaborateAlgorithm.sortQuestsToComplexSingleton(projects);
        List<ProjectLinked> sortedSingletonProjects = singleAndComplex.singletonProjects;
        Map<Integer, ProjectLinked> uidToComplexProjects = singleAndComplex.complexProjects;
        Set<ProjectLinked> allComplexStarterProjects = singleAndComplex.startingComplexProjects;

        // get all the starter complex projects and add one quest at a time
        Set<ProjectGroup> allProjectLines = new HashSet<>();
        for (ProjectLinked starter : allComplexStarterProjects) {
            if (isTimeOkay(timeToSpend, Collections.emptyList(), starter))
                allProjectLines.add(new ProjectGroup(Collections.singletonList(starter), workersCount, timeToSpend));
        }
        // add a project at a time and at each step, save that combo
        addQuestGivenTime(allProjectLines, uidToComplexProjects, timeToSpend, workersCount);

        System.out.printf("Made project chains -- Checkpoint: %d/%d -- lapTime: %d -- totalTime: %d\n", ++currentCheckpoint, CHECKPOINTS_COUNT, System.currentTimeMillis() - start, System.currentTimeMillis() - absoluteStart);
        start = System.currentTimeMillis();

        List<ProjectGroup> allProjectLinesSorted = Sorting.sortQuestCombinationByAPT(allProjectLines);

        Set<ProjectGroup> finalProjectCombinations = new HashSet<>();
        for (ProjectGroup projectLine : allProjectLines) {
            finalProjectCombinations.add(new ProjectGroup(projectLine.getProjects(), workersCount, timeToSpend));
        }
        addProjectGroupGivenTime(finalProjectCombinations, allProjectLinesSorted, timeToSpend, workersCount);

        System.out.printf("Combos of project chains -- Checkpoint: %d/%d -- lapTime: %d -- totalTime: %d\n", ++currentCheckpoint, CHECKPOINTS_COUNT, System.currentTimeMillis() - start, System.currentTimeMillis() - absoluteStart);
        start = System.currentTimeMillis();

        finalProjectCombinations.add(new ProjectGroup(workersCount, timeToSpend)); // for no complex projects in the group

        // add singletons to fill up every questline to as many as it can hold to stay within the time constraint
        for (ProjectGroup projectGroup : finalProjectCombinations) {
            for (ProjectLinked singleton : sortedSingletonProjects) {
                if (!projectGroup.addProject(singleton, null)) {
                    // we failed to add the project, so continue with the outer loop
                    break;
                }
            }
        }
        System.out.printf("Added singletons -- Checkpoint: %d/%d -- lapTime: %d -- totalTime: %d\n", ++currentCheckpoint, CHECKPOINTS_COUNT, System.currentTimeMillis() - start, System.currentTimeMillis() - absoluteStart);
        start = System.currentTimeMillis();

        List<ProjectGroup> projectsSorted = new ArrayList<>(finalProjectCombinations);
        Sorting.sortProjectGroupsByAmount(projectsSorted);
        System.out.printf("Finished -- Checkpoint: %d/%d -- lapTime: %d -- totalTime: %d\n", ++currentCheckpoint, CHECKPOINTS_COUNT, System.currentTimeMillis() - start, System.currentTimeMillis() - absoluteStart);


        return projectsSorted.isEmpty() ? null : projectsSorted.get(0);

    }


    /**
     * recursively add a single projectGroup to the projectLines and save at each step
     *
     * @param finalProjectCombinations the final set of combinations of projects that we add to
     * @param allProjectLines          the complex projectLines that we make a recursive cross product of
     * @param timeToSpend              the time that we have to spend doing projects
     */
    private static void addProjectGroupGivenTime(Set<ProjectGroup> finalProjectCombinations, List<ProjectGroup> allProjectLines, int timeToSpend, int workersCount) {
        final Object sync = new Object();
        Set<ProjectGroup> groupsToAdd = new HashSet<>();
        finalProjectCombinations.parallelStream().forEach(
                oldProjectGroup -> {
                    // for every project in the old project group, add all projectLines that make a difference
                    for (ProjectGroup newProjectLine : allProjectLines) {
                        // if it makes a difference, keep it
                        if (!oldProjectGroup.containsAll(newProjectLine.getProjects())) {
                            ProjectGroup newProjectGroup = new ProjectGroup(oldProjectGroup);
                            // if it is possible, keep it
                            if (newProjectGroup.addProjects(newProjectLine.getProjects())) {
                                synchronized (sync) {
                                    groupsToAdd.add(newProjectGroup);
                                }
                                // we're stopping here because this somewhat greedy algorithm
                                // allows us to not try every combination while minimizing the loss of precision
                                break;
                            }
                        }
                    }
                }
        );
        if (!finalProjectCombinations.containsAll(groupsToAdd)) {
            addProjectGroupGivenTime(groupsToAdd, allProjectLines, timeToSpend, workersCount);
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
    private static void addQuestGivenTime(Set<ProjectGroup> allProjectLines, Map<Integer, ProjectLinked> uidToComplexProjects, int timeToSpend, int workersCount) {
        final Object sync = new Object();
        final Set<ProjectGroup> projectsToAdd = new HashSet<>();

        // for every combination we have, try to add one new entry, and add that combination as a clone
        allProjectLines.parallelStream().forEach(
                projectLine -> {
                    // for every project in the projectLine, add all the reqMe's
                    for (ProjectLinked projectInOldLine : projectLine.getProjects()) {
                        for (Integer reqMe : projectInOldLine.getRequireMe()) {
                            ProjectLinked projectReqMe = uidToComplexProjects.get(reqMe);
                            if (projectLine.getProjects().contains(projectReqMe)) continue;
                            // add this corresponding projectReqMe
                            ProjectGroup newProjectCombination = new ProjectGroup(projectLine);
                            Set<ProjectLinked> reqAndPrereq = new HashSet<>();
                            ProjectLinked project = uidToComplexProjects.get(reqMe);
                            reqAndPrereq.add(project);
                            for (int req : project.getAllRequirements())
                                reqAndPrereq.add(uidToComplexProjects.get(req));
                            reqAndPrereq.removeAll(newProjectCombination.getProjects());
                            if (newProjectCombination.addProjects(reqAndPrereq)) {
                                if (!allProjectLines.contains(newProjectCombination)) {
                                    synchronized (sync) {
                                        projectsToAdd.add(newProjectCombination);
                                    }
                                }
                            }
                        }
                    }
                }
        );
        if (!allProjectLines.containsAll(projectsToAdd)) {
            addQuestGivenTime(projectsToAdd, uidToComplexProjects, timeToSpend, workersCount);
            allProjectLines.addAll(projectsToAdd);
        }
    }

    private static boolean isTimeOkay(int timeToSpend, Collection<ProjectLinked> combination, Project... additional) {
        int timeSpent = 0;
        for (ProjectLinked project : combination) {
            timeSpent += project.getTime();
        }
        for (Project project : additional) {
            timeSpent += project.getTime();
        }
        return timeToSpend >= timeSpent;
    }

}
