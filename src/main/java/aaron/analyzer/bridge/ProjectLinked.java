package aaron.analyzer.bridge;

import java.util.ArrayList;
import java.util.Collection;

public class ProjectLinked extends Project {
    private final Collection<Integer> allRequirements = new ArrayList<>();
    private final int[] immediateRequirements;
    private final Collection<Integer> requireMe = new ArrayList<>(); // the projects that require this

    public ProjectLinked(Project project) {
        super(project);
        for (Integer questReq : project.getAllRequirements()) {
            this.allRequirements.add(questReq);
        }
        this.immediateRequirements = project.getAllRequirements();
    }

    public boolean isImmediateRequirementsEmpty() {
        return immediateRequirements.length == 0;
    }

    public Collection<Integer> getRequireMe() {
        return requireMe;
    }

    public void addRequireMe(int uid) {
        requireMe.add(uid);
    }
}
