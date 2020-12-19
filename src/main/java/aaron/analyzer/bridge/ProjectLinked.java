package aaron.analyzer.bridge;

import java.util.ArrayList;
import java.util.Collection;

public class ProjectLinked extends Project {
    private final Collection<Integer> requireMe = new ArrayList<>(); // the projects that require this

    public ProjectLinked(Project project) {
        super(project);
    }

    public Collection<Integer> getRequireMe() {
        return requireMe;
    }

    public void addRequireMe(int uid) {
        requireMe.add(uid);
    }
}
