package test.analyzer.bridge;

import aaron.analyzer.bridge.AllQuests;
import aaron.analyzer.bridge.Project;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TestHelper {
    @NotNull
    public static Map<String, Project> getProjectsRaw() {
        List<Project> projects = new ArrayList<>();
        projects.add(new Project(new AllQuests.SimpleProject(
                "SingletonShort",
                1,
                2,
                "",
                2)
        ));
        projects.add(new Project(new AllQuests.SimpleProject(
                "SingletonLong",
                3,
                2,
                "",
                1)
        ));
        projects.add(new Project(new AllQuests.SimpleProject(
                "ComplexStarter",
                1,
                1,
                "",
                1)
        ));
        projects.add(new Project(new AllQuests.SimpleProject(
                "ComplexA",
                1,
                2,
                "ComplexStarter",
                1)
        ));
        projects.add(new Project(new AllQuests.SimpleProject(
                "ComplexB",
                1,
                2,
                "ComplexStarter",
                1)
        ));
        projects.add(new Project(new AllQuests.SimpleProject(
                "ComplexLast",
                1,
                4,
                "ComplexA,ComplexB",
                1)
        ));
        return new HashMap<>() {
            {
                for (Project p : projects) put(p.getName(), p);
            }
        };
    }
    public static void isSameCollection(Collection<ProjectLinked> myProjects, ProjectGroup projectGroup) {
        assert projectGroup.getProjects().containsAll(myProjects);
        assert myProjects.containsAll(projectGroup.getProjects());
    }
}
