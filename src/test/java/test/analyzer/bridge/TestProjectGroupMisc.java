package test.analyzer.bridge;

import aaron.analyzer.algorithm.ElaborateAlgorithm;
import aaron.analyzer.bridge.AllQuests;
import aaron.analyzer.bridge.Project;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestProjectGroupMisc {
    private final Map<String, Project> projectsRaw = new HashMap<>();
    private final Map<String, ProjectLinked> allProjectsLinked = new HashMap<>();
    private final Map<String, ProjectLinked> singletonProjects = new HashMap<>();
    private final Map<String, ProjectLinked> complexProjects = new HashMap<>();

    /**
     * SingletonShort
     * SingletonLong
     * <p>
     * ComplexStarter
     * ComplexA requires ComplexStarter
     * ComplexB requires ComplexStarter
     * ComplexLast requires ComplexA andComplexB
     */
    @BeforeAll
    public void setup() {
        this.projectsRaw.putAll(TestHelper.getProjectsRaw());

        AllQuests.initializeProjects(projectsRaw.values());
        ElaborateAlgorithm.ReturnSingleComplex singleComplex = ElaborateAlgorithm.sortQuestsToComplexSingleton(projectsRaw.values());
        Map<Integer, ProjectLinked> complex = singleComplex.complexProjects;
        List<ProjectLinked> singleton = singleComplex.singletonProjects;
        for (ProjectLinked c : complex.values()) {
            complexProjects.put(c.getName(), c);
            allProjectsLinked.put(c.getName(), c);
        }
        for (ProjectLinked s : singleton) {
            singletonProjects.put(s.getName(), s);
            allProjectsLinked.put(s.getName(), s);
        }
    }

    @Test
    public void timeline() {
        List<ProjectLinked> myProjects = new ArrayList<>() {
            {
                add(allProjectsLinked.get("SingletonShort"));
                add(allProjectsLinked.get("SingletonLong"));
                add(allProjectsLinked.get("ComplexStarter"));
                add(allProjectsLinked.get("ComplexA"));
                add(allProjectsLinked.get("ComplexB"));
                add(allProjectsLinked.get("ComplexLast"));
            }
        };
        ProjectGroup projectGroup = new ProjectGroup(2, 100);
        int[][] timeline = projectGroup.getProjectTimeline();
        assert timeline[0].length == 0;
        assert timeline.length == 2;

        for (ProjectLinked project : myProjects) {
            projectGroup.addProject(project, null);
            timeline = projectGroup.getProjectTimeline();
            assert timeline[0].length == projectGroup.time();
            assert timeline.length == 2;
        }
        projectGroup = new ProjectGroup(3, 100);
        timeline = projectGroup.getProjectTimeline();
        assert timeline[0].length == 0;
        assert timeline.length == 3;

        for (ProjectLinked project : myProjects) {
            projectGroup.addProject(project, null);
            timeline = projectGroup.getProjectTimeline();
            assert timeline[0].length == projectGroup.time();
            assert timeline.length == 3;
        }
    }
}
