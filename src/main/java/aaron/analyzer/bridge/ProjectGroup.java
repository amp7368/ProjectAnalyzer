package aaron.analyzer.bridge;

import java.util.HashSet;
import java.util.Set;

public class ProjectGroup {
    private final Set<ProjectLinked> projects;
    private long effectiveTimeCalculated = 0;
    private long timeCalculated = 0;

    public ProjectGroup(Set<ProjectLinked> projects) {
        this.projects = new HashSet<>(projects);
        for (ProjectLinked project : projects) {
            timeCalculated += project.getTime();
            effectiveTimeCalculated += project.getUserEffectiveTime();
        }
    }

    public ProjectGroup() {
        this.projects = new HashSet<>();
    }

    public boolean addProjectGroup(Set<ProjectLinked> otherProjects) {
        for (ProjectLinked project : otherProjects) {
            timeCalculated += project.getTime();
            effectiveTimeCalculated += project.getUserEffectiveTime();
        }
        return projects.addAll(otherProjects);
    }

    public boolean addProject(ProjectLinked singleton) {
        return projects.add(singleton);
    }

    public long worth() {
        long worth = 0;
        for (ProjectLinked project : projects) {
            worth += project.getWorth();
        }
        return worth;
    }

    public long effectiveTime() {
        return effectiveTimeCalculated;
    }

    private long time() {
        return timeCalculated;
    }

    public Set<ProjectLinked> getProjects() {
        return projects;
    }

    public boolean isTimeOkay(long timeToSpend, ProjectLinked... otherProjects) {
        long time = time();
        for (ProjectLinked project : otherProjects) {
            time += project.getUserEffectiveTime();
        }
        return time <= timeToSpend;
    }

    @Override
    public int hashCode() {
        return projects.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProjectGroup && projects.equals(((ProjectGroup) obj).projects);
    }
}
