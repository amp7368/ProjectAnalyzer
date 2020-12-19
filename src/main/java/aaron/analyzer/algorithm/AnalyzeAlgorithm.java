package aaron.analyzer.algorithm;

import aaron.analyzer.bridge.Project;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AnalyzeAlgorithm {

    @Nullable
    public static ProjectGroup whichGivenTime(Collection<Project> projects, int timeToSpend, int workersCount) {
        ReturnSingleComplex singleAndComplex = sortQuestsToComplexSingleton(projects);
        List<ProjectLinked> sortedSingletonProjects = singleAndComplex.singletonProjects;
        Map<Integer, ProjectLinked> uidToComplexProjects = singleAndComplex.complexProjects;
        Set<ProjectLinked> allComplexStarterProjects = singleAndComplex.startingComplexProjects;

        // get all the starter complex projects and add one quest at a time
        Set<Set<ProjectLinked>> allProjectLinesCollection = new HashSet<>();
        for (ProjectLinked starter : allComplexStarterProjects) {
            if (isTimeOkay(timeToSpend, Collections.emptyList(), starter))
                allProjectLinesCollection.add(new HashSet<>() {{
                    add(starter);
                }});
        }
        long start = System.currentTimeMillis();
        // add a project at a time and at each step, save that combo
        addQuestGivenTime(allProjectLinesCollection, uidToComplexProjects, timeToSpend, workersCount);
        System.out.println("AddQuestGivenTime: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        Set<ProjectGroup> allProjectLines = fulfillPrereqs(allProjectLinesCollection, uidToComplexProjects, timeToSpend, workersCount);

        // todo idk if this is necessary
        allProjectLines.removeIf(ProjectGroup::isEmpty);

        List<ProjectGroup> allProjectLinesSorted = Sorting.sortQuestCombinationByAPT(allProjectLines);

        Set<ProjectGroup> finalProjectCombinations = new HashSet<>();
        for (ProjectGroup projectLine : allProjectLines) {
            finalProjectCombinations.add(new ProjectGroup(projectLine.getProjects(), workersCount, timeToSpend));
        }
        addProjectGroupGivenTime(finalProjectCombinations, allProjectLinesSorted, timeToSpend, workersCount);
        System.out.println("addProjectGroupGivenTime: " + (System.currentTimeMillis() - start));
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
        System.out.println("addSingletons: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();

        // todo you could sort them and then take the top __% and check parallelization on only those (or limit it to like 100,000 to check)


        List<ProjectGroup> projectsSorted = new ArrayList<>(finalProjectCombinations);
        Sorting.sortProjectGroupsByAmount(projectsSorted);
        System.out.println("finalized: " + (System.currentTimeMillis() - start));

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
                            ProjectGroup newProjectGroup = new ProjectGroup(oldProjectGroup.getProjects(), workersCount, timeToSpend);
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
    private static void addQuestGivenTime(Set<Set<ProjectLinked>> allProjectLines, Map<Integer, ProjectLinked> uidToComplexProjects, int timeToSpend, int workersCount) {
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
                            if (newProjectCombination.add(projectReqMe)) {
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

    /**
     * make sure all the prereqs are filled for all the projectLines
     *
     * @param allProjectLines      the project lines to verify
     * @param uidToComplexProjects the mapping to reference
     * @return the projectGroups that correspond to the given allProjectLines. these groups are verified
     */
    private static Set<ProjectGroup> fulfillPrereqs(Set<Set<ProjectLinked>> allProjectLines, Map<Integer, ProjectLinked> uidToComplexProjects, int timeToSpend, int workersCount) {
        // I just do this because mutable objects inside sets have undefined behavior
        List<Set<ProjectLinked>> projectLinesInList = new ArrayList<>(allProjectLines);
        Set<Set<ProjectLinked>> newProjectLines = new HashSet<>();
        Set<ProjectGroup> projectLinesPassed = new HashSet<>();
        final Object sync1 = new Object();
        final Object sync2 = new Object();
        projectLinesInList.parallelStream().forEach(projectLine -> {
            Map<Integer, ProjectLinked> projects = new HashMap<>() {{
                for (ProjectLinked project : projectLine)
                    put(project.getUid(), project);
            }};
            List<ProjectLinked> projectsToAdd = new ArrayList<>();
            for (ProjectLinked projectToVerify : projectLine) {
                int[] allReqs = projectToVerify.getAllRequirements();
                for (int req : allReqs) {
                    ProjectLinked projectToAdd = uidToComplexProjects.get(req);
                    projects.put(req, projectToAdd);
                    projectsToAdd.add(projectToAdd);
                }
            }
            projectLine.addAll(projectsToAdd);

            // this is less readable than it could be just to make it more efficient
            // it's likely that two threads could try to make the new ProjectGroup at the same time
            // so making that outside of a synchronized block is important
            boolean addMe = false;
            synchronized (sync1) {
                if (newProjectLines.add(projectLine)) {
                    addMe = true;
                }
            }
            if (addMe) {
                ProjectGroup newGroup = null;
                try {
                    newGroup = new ProjectGroup(projectLine, workersCount, timeToSpend);
                } catch (IllegalStateException e) {
                    // this combination is most likely and effectively impossible
                }
                if (newGroup != null) {
                    synchronized (sync2) {
                        projectLinesPassed.add(newGroup);
                    }
                }
            }
        });
        return projectLinesPassed;
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
