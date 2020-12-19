package aaron.analyzer.bridge;


import java.util.*;

public class ProjectGroup {
    private final Map<Integer, ProjectLinked> projects;
    private List<ProjectLinked> projectsAddedOrdering = new ArrayList<>();
    private Map<Integer, List<ProjectLinked>> realTimelineOrdering = new HashMap<>();

    private ProjectLinked lastProjectAdded = null;

    // parallel project timeline
    private List<boolean[]> mold = new ArrayList<>(); // the inside array is always of size workersCount
    private final int workersCount;
    private int simpleTimeLeft;
    private final int originalTimeToSpend;


    public ProjectGroup(Collection<ProjectLinked> projects, int workersCount, int timeToSpend) {
        this.projects = new HashMap<>() {{
            for (ProjectLinked project : projects) put(project.getUid(), project);
        }};
        this.workersCount = workersCount;
        this.simpleTimeLeft = this.originalTimeToSpend = timeToSpend;
        Map<Integer, ProjectLinked> projectsLeftNotAvailable = new HashMap<>(this.projects);
        List<ProjectLinked> projectsLeftAvailable = new ArrayList<>();
        fillStarters(projectsLeftAvailable, projectsLeftNotAvailable);
        while (!projectsLeftAvailable.isEmpty()) {
            if (!fitBest(projectsLeftAvailable, null))
                throw new IllegalStateException("This combination of projects is most likely impossible given the constraints");
            fillAvailable(projectsLeftAvailable, projectsLeftNotAvailable);
            verifyMold();
        }
    }


    public ProjectGroup(int workersCount, int timeToSpend) {
        this.projects = new HashMap<>();
        this.workersCount = workersCount;
        this.simpleTimeLeft = this.originalTimeToSpend = timeToSpend;
    }

    public ProjectGroup(ProjectGroup other) {
        this.projects = new HashMap<>(other.projects);
        this.projectsAddedOrdering = new ArrayList<>(other.projectsAddedOrdering);
        this.realTimelineOrdering = new HashMap<>(other.realTimelineOrdering);
        this.lastProjectAdded = other.lastProjectAdded;
        this.mold = new ArrayList<>();
        for (boolean[] arr : other.mold) this.mold.add(Arrays.copyOf(arr, arr.length));
        this.workersCount = other.workersCount;
        this.simpleTimeLeft = other.simpleTimeLeft;
        this.originalTimeToSpend = other.originalTimeToSpend;
    }

    /**
     * attempt to redefine the timeline to make more space for new projects
     *
     * @return true if the new timeline is possible given the constraints, otherwise false
     */
    private boolean redefineTimeline() {
        this.mold = new ArrayList<>();
        this.projectsAddedOrdering = new ArrayList<>();
        this.simpleTimeLeft = this.originalTimeToSpend;
        Map<Integer, ProjectLinked> projectsLeftNotAvailable = new HashMap<>(this.projects);
        List<ProjectLinked> projectsLeftAvailable = new ArrayList<>();
        fillStarters(projectsLeftAvailable, projectsLeftNotAvailable);
        while (!projectsLeftAvailable.isEmpty()) {
            if (!fitBest(projectsLeftAvailable, null)) {
                return false;
            }
            fillAvailable(projectsLeftAvailable, projectsLeftNotAvailable);
            verifyMold();
        }
        if (!projectsLeftNotAvailable.isEmpty()) {
            throw new IllegalStateException("ProjectsLeftNotAvailable is not empty which means that the given projects are impossible");
        }
        return true;
    }

    private void fillStarters(List<ProjectLinked> projectsLeftAvailable, Map<Integer, ProjectLinked> projectsLeftNotAvailable) {
        Iterator<ProjectLinked> projectsNotAvailableIterator = projectsLeftNotAvailable.values().iterator();
        while (projectsNotAvailableIterator.hasNext()) {
            ProjectLinked project = projectsNotAvailableIterator.next();
            if (project.getImmediateRequirements().length == 0) {
                projectsNotAvailableIterator.remove();
                projectsLeftAvailable.add(project);
            }
        }
    }

    private void fillAvailable(List<ProjectLinked> projectsLeftAvailable, Map<Integer, ProjectLinked> projectsLeftNotAvailable) {
        if (this.lastProjectAdded == null) return;
        Collection<Integer> reqMes = this.lastProjectAdded.getRequireMe();
        for (int reqMe : reqMes) {
            ProjectLinked projectToCheck = projectsLeftNotAvailable.get(reqMe);
            if (projectToCheck != null) {
                int[] myReqs = projectToCheck.getImmediateRequirements();
                boolean isOkay = true;
                for (int myReq : myReqs) {
                    if (!this.projects.containsKey(myReq)) {
                        isOkay = false;
                        break;
                    }
                }
                if (isOkay) {
                    projectsLeftNotAvailable.remove(projectToCheck.getUid());
                    projectsLeftAvailable.add(projectToCheck);
                }
            }
        }
    }

    private void verifyMold() {
        boolean[] fillWithTrues = new boolean[this.workersCount];
        int moldLength = this.mold.size();
        for (int i = moldLength - 1; i >= 0; i--) {
            boolean[] col = this.mold.get(i);
            for (int j = 0; j < col.length; j++) {
                if (fillWithTrues[j]) {
                    col[j] = true;
                } else if (col[j]) {
                    fillWithTrues[j] = true;
                }
            }
        }
        while (moldLength != 0) {
            for (boolean b : this.mold.get(0)) {
                // if !b, we can assume that the rest to the right is not filled with trues because of what we just verified
                if (!b) return;
            }
            // if we made it here, we can remove this first col
            this.mold.remove(0);
            moldLength--;
        }
    }

    /**
     * fits the best guess for adding one from the projects given
     *
     * @param projectsLeft    the projects to take one from
     * @param projectTimeline nullable the timeline to fill out if provided
     * @return true if placing a project will not violate our time constraint otherwise false
     * @throws IndexOutOfBoundsException if projectsLeft is empty
     */
    private boolean fitBest(List<ProjectLinked> projectsLeft, int[][] projectTimeline) {
        projectsLeft.sort((p1, p2) -> {
            int difference = p2.getWorkersCount() - p1.getWorkersCount();
            if (difference == 0) {
                return p2.getTime() - p1.getTime();
            } else {
                return difference;
            }
        });
        int moldLength = this.mold.size();
        for (int repeat = 0; repeat != 2; repeat++) {
            for (int col = 0; col < moldLength; col++) {
                boolean[] colOfMold = this.mold.get(col);
                for (ProjectLinked project : projectsLeft) {
                    int requiredWorkers = project.getWorkersCount();
                    // find consecutive false's in the mold that can fit requiredWorkers
                    int length = 0;
                    int currentIndex = 0;
                    for (int indexOfChecking = 0; indexOfChecking < colOfMold.length; indexOfChecking++) {
                        if (!colOfMold[indexOfChecking]) {
                            length++;
                        } else {
                            // there is a true at indexOfChecking. meaning this spot is filled
                            if (length >= requiredWorkers) {
                                if (!isPlaceOkayPrereqs(project, col + originalTimeToSpend - simpleTimeLeft - mold.size())) {

                                    // if this is the first pass, and the prereqs aren't met for this project when putting
                                    // this snugly against the mold, then try a different project.
                                    // Otherwise, this is the second pass, and nothing fits snugly, so try to fit it less snugly
                                    //
                                    // we could do continue, for both, but that will provide worse results because
                                    // it will put projects that don't fit as snugly into our mold
                                    if (repeat == 0)
                                        break;
                                    else
                                        continue;
                                }

                                // we can fit it at "currentIndex"
                                // fill the mold with appropriate true's so that the project is considered
                                return placeProject(projectsLeft, project, col, currentIndex, projectTimeline);
                            } else {
                                currentIndex = indexOfChecking + 1;
                                length = 0;
                            }
                        }
                    }
                    // add the project at currentIndex if it's possible
                    if (length >= requiredWorkers) {
                        // we can fit it at "currentIndex"
                        // fill the mold with appropriate true's so that the project is considered
                        return placeProject(projectsLeft, project, col, currentIndex, projectTimeline);
                    }
                }
            }
        }
        // take out of the clean rest of the project time
        // we need to add as many cols as there is time of this project
        return placeProject(projectsLeft, projectsLeft.get(0), moldLength, 0, projectTimeline);
    }

    private boolean isPlaceOkayPrereqs(ProjectLinked project, int timeToPlace) {
        Set<Integer> immediates = new HashSet<>() {{
            for (int i : project.getImmediateRequirements()) add(i);
        }};
        List<Integer> times = new ArrayList<>(realTimelineOrdering.keySet());
        times.sort(Integer::compare);
        for (int time : times) {
            if (time >= timeToPlace) break;
            List<ProjectLinked> projects = realTimelineOrdering.get(time);
            for (ProjectLinked projectBefore : projects) {
                immediates.remove(projectBefore.getUid());
                if (immediates.isEmpty()) break;
            }
        }
        return immediates.isEmpty();
    }

    /**
     * add the project if it does not violate the time constraint
     *
     * @param projectsLeft      the projects that we remove from if we succeed
     * @param project           the project to place
     * @param col               the column we're in inside the mold (the length of the mold if we don't include the mold at all)
     * @param currentInnerIndex the inner (y) index that we're placing the project at inside of the mold
     * @param projectTimeline   nullable the timeline to fill out if provided
     * @return true if placing the project will not violate our time constraint otherwise false
     */
    private boolean placeProject(Collection<ProjectLinked> projectsLeft, ProjectLinked project, int col, int currentInnerIndex, int[][] projectTimeline) {
        int projectTime = project.getTime();

        // if we'll fail at adding this, fail completely and do nothing
        int jUpper = projectTime + col;
        if (this.simpleTimeLeft + mold.size() < jUpper) {
            return false;
        }

        int iUpper = project.getWorkersCount() + currentInnerIndex;
        for (int i = currentInnerIndex; i < iUpper; i++) {
            for (int j = col; j < jUpper; j++) {
                if (j == this.mold.size()) {
                    // add another col to the mold
                    this.mold.add(new boolean[this.workersCount]);
                    this.simpleTimeLeft--;
                }
                this.mold.get(j)[i] = true;
                if (projectTimeline != null) {
                    projectTimeline[i][j + originalTimeToSpend - simpleTimeLeft - mold.size()] = Integer.parseInt(project.getName());
                }
            }
        }
        projectsLeft.remove(project);
        projectsAddedOrdering.add(project);
        int timeIndex = col + originalTimeToSpend - simpleTimeLeft - mold.size();
        // make this as small of a list as possible because there will be a lot of lists of size 1
        realTimelineOrdering.putIfAbsent(timeIndex, new ArrayList<>(1));
        realTimelineOrdering.get(timeIndex).add(project);
        lastProjectAdded = project;
        return true;
    }

    /**
     * add the projects provided to the current projectGroup
     * "this" has undefined behavior if the new set of projects is impossible given the time constraint
     *
     * @param otherProjects the other projects to add to the current set
     * @return true if placement does not violate the time constraint. otherwise false
     */
    public boolean addProjects(Collection<ProjectLinked> otherProjects) {
        for (ProjectLinked project : otherProjects)
            this.projects.put(project.getUid(), project);
        List<ProjectLinked> projectsLeft = new ArrayList<>(otherProjects);
        while (!projectsLeft.isEmpty()) {
            if (!fitBest(projectsLeft, null)) {
                return redefineTimeline();
                // redefine timeline verifies the mold for us
            } else {
                verifyMold();
            }
        }
        return true;
    }

    /**
     * add the project provided to the current projectGroup
     * "this" remains intact and nothing changed if false is returned
     *
     * @param singleton       the other project to add to the current set
     * @param projectTimeline nullable the timeline to fill out if provided
     * @return true if the placement does not violate the time constraint. otherwise false
     */
    public boolean addProject(ProjectLinked singleton, int[][] projectTimeline) {
        this.projects.put(singleton.getUid(), singleton);
        if (!fitBest(new ArrayList<>(1) {{
            add(singleton);
        }}, projectTimeline)) {
            int currentSimpleTimeLeft = this.simpleTimeLeft;
            List<boolean[]> currentMold = this.mold;
            List<ProjectLinked> currentProjectOrdering = this.projectsAddedOrdering;
            if (!redefineTimeline()) {
                // revert to what there was before
                this.projects.remove(singleton.getUid());
                this.simpleTimeLeft = currentSimpleTimeLeft;
                this.mold = currentMold;
                this.projectsAddedOrdering = currentProjectOrdering;
                return false;
            }
            return true;
        }
        verifyMold();
        return true;
    }

    public long worth() {
        long worth = 0;
        for (ProjectLinked project : projects.values()) {
            worth += project.getWorth();
        }
        return worth;
    }

    public Collection<ProjectLinked> getProjects() {
        return projects.values();
    }

    @Override
    public int hashCode() {
        return projects.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProjectGroup && projects.equals(((ProjectGroup) obj).projects);
    }

    public boolean isEmpty() {
        return projects.isEmpty();
    }

    public boolean containsAll(Collection<ProjectLinked> projects) {
        return this.projects.values().containsAll(projects);
    }

    public boolean containsProjectId(int req) {
        return projects.containsKey(req);
    }

    public int time() {
        return originalTimeToSpend - simpleTimeLeft;
    }

    public int[][] getProjectTimeline() {
        // check to see if redefining the timeline does any better
        int oldTime = time();
        int currentSimpleTimeLeft = this.simpleTimeLeft;
        List<boolean[]> currentMold = this.mold;
        List<ProjectLinked> currentProjectOrdering = this.projectsAddedOrdering;
        if (!redefineTimeline() || oldTime < time()) {
            // revert to what there was before
            this.simpleTimeLeft = currentSimpleTimeLeft;
            this.mold = currentMold;
            this.projectsAddedOrdering = currentProjectOrdering;
        }

        List<ProjectLinked> ordering = this.projectsAddedOrdering;
        int[][] projectTimeline = new int[workersCount][time()];
        for (int[] e : projectTimeline) {
            Arrays.fill(e, -1);
        }
        // clear everything
        this.projects.clear();
        this.projectsAddedOrdering = new ArrayList<>();
        this.mold = new ArrayList<>();
        this.simpleTimeLeft = originalTimeToSpend;

        for (ProjectLinked project : ordering) {
            if (!addProject(project, projectTimeline)) {
                System.err.println("getting the timeline failed after determining that the timeline was possible. This should be impossible.");
            }
        }
        return projectTimeline;
    }
}
