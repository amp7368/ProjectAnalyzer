package test.analyzer.bridge;

import aaron.analyzer.bridge.AllProjects;
import aaron.analyzer.bridge.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestAllProjectsCreation {
    private final Map<String, Project> projectsRaw = new HashMap<>();
    private final Map<Integer, Project> uidToProjects = new HashMap<>();

    @BeforeEach
    public void setup() {
        List<Project> projects = Arrays.asList(
                new Project(new AllProjects.SimpleProject(
                        "A",
                        1,
                        1,
                        "",
                        1
                )), new Project(new AllProjects.SimpleProject(
                        "B",
                        1,
                        1,
                        "A",
                        1
                )), new Project(new AllProjects.SimpleProject(
                        "C",
                        1,
                        1,
                        "B",
                        1
                )), new Project(new AllProjects.SimpleProject(
                        "D",
                        1,
                        1,
                        "B",
                        1
                ))
        );
        for (Project project : projects) {
            projectsRaw.put(project.getName(), project);
            uidToProjects.put(project.getUid(), project);
        }
    }

    @Test
    public void initializeProjects() {
        AllProjects.initializeProjects(projectsRaw.values());
        assert projectsRaw.get("A").getImmediateRequirements().length == 0;
        assert projectsRaw.get("A").getAllRequirements().length == 0;
        assert projectsRaw.get("A").getRequirementNames().length == 0;

        TestHelper.isSameCollection(getProjects(projectsRaw.get("B").getAllRequirements()), Collections.singletonList(projectsRaw.get("A")));
        TestHelper.isSameCollection(getProjects(projectsRaw.get("B").getImmediateRequirements()), Collections.singletonList(projectsRaw.get("A")));
        TestHelper.isSameCollection(Arrays.asList(projectsRaw.get("B").getRequirementNames()), Collections.singletonList("A"));

        TestHelper.isSameCollection(getProjects(projectsRaw.get("C").getAllRequirements()), Arrays.asList(projectsRaw.get("A"), projectsRaw.get("B")));
        TestHelper.isSameCollection(getProjects(projectsRaw.get("C").getImmediateRequirements()), Collections.singletonList(projectsRaw.get("B")));
        TestHelper.isSameCollection(Arrays.asList(projectsRaw.get("C").getRequirementNames()), Collections.singletonList("B"));

        TestHelper.isSameCollection(getProjects(projectsRaw.get("D").getAllRequirements()), Arrays.asList(projectsRaw.get("A"), projectsRaw.get("B")));
        TestHelper.isSameCollection(getProjects(projectsRaw.get("D").getImmediateRequirements()), Collections.singletonList(projectsRaw.get("B")));
        TestHelper.isSameCollection(Arrays.asList(projectsRaw.get("D").getRequirementNames()), Collections.singletonList("B"));

    }

    private Collection<Project> getProjects(int[] uids) {
        Collection<Project> projects = new ArrayList<>();
        for (int uid : uids)
            projects.add(uidToProjects.get(uid));
        return projects;
    }

    @Test
    public void badReqName() {
        projectsRaw.put("Bad", new Project(new AllProjects.SimpleProject(
                "Bad",
                1,
                1,
                "Hello",
                1
        )));
        boolean success = true;
        try {
            AllProjects.initializeProjects(projectsRaw.values());
        } catch (IllegalStateException e) {
            success = false;
        }
        assert !success;
    }
}
