package test.analyzer.bridge;

import aaron.analyzer.algorithm.ElaborateAlgorithm.ReturnSingleComplex;
import aaron.analyzer.algorithm.ElaborateAlgorithm;
import aaron.analyzer.bridge.AllQuests;
import aaron.analyzer.bridge.Project;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestProjectGroupConstructorWithProjects {
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
        projectsRaw.put("SingletonShort", new Project(new AllQuests.SimpleProject(
                "SingletonShort",
                1,
                2,
                "",
                2)
        ));
        projectsRaw.put("SingletonLong", new Project(new AllQuests.SimpleProject(
                "SingletonLong",
                3,
                2,
                "",
                1)
        ));
        projectsRaw.put("ComplexStarter", new Project(new AllQuests.SimpleProject(
                "ComplexStarter",
                1,
                1,
                "",
                1)
        ));
        projectsRaw.put("ComplexA", new Project(new AllQuests.SimpleProject(
                "ComplexA",
                1,
                2,
                "ComplexStarter",
                1)
        ));
        projectsRaw.put("ComplexB", new Project(new AllQuests.SimpleProject(
                "ComplexB",
                1,
                2,
                "ComplexStarter",
                1)
        ));
        projectsRaw.put("ComplexLast", new Project(new AllQuests.SimpleProject(
                "ComplexLast",
                1,
                4,
                "ComplexA,ComplexB",
                1)
        ));

        AllQuests.initializeProjects(projectsRaw.values());
        ReturnSingleComplex singleComplex = ElaborateAlgorithm.sortQuestsToComplexSingleton(projectsRaw.values());
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
    public void constructorWithProjects1CountImpossible() {
        Collection<ProjectLinked> myProjects = new ArrayList<>() {
            {
                add(allProjectsLinked.get("SingletonShort"));
            }
        };
        boolean success = true;
        try {
            new ProjectGroup(myProjects, 0, 0);
        } catch (IllegalStateException e) {
            success = false;
        }
        assert !success;
    }

    @Test
    public void constructorWithProjects1Count() {
        List<ProjectLinked> myProjects = new ArrayList<>() {
            {
                add(allProjectsLinked.get("SingletonShort"));
            }
        };
        boolean success = true;
        try {
            ProjectGroup projectGroup = new ProjectGroup(myProjects, myProjects.get(0).getWorkersCount(), myProjects.get(0).getTime());
            isSameCollection(myProjects, projectGroup);
            assert projectGroup.worth() == 2;
            assert projectGroup.time() == 1;
        } catch (IllegalStateException e) {
            success = false;
        }
        assert success;
    }

    @Test
    public void constructorWithProjects2CountSingleton() {
        List<ProjectLinked> myProjects = new ArrayList<>() {
            {
                add(singletonProjects.get("SingletonShort"));
                add(singletonProjects.get("SingletonLong"));
            }
        };
        ProjectGroup projectGroup = new ProjectGroup(myProjects, 2, 4);
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 4;
        assert projectGroup.time() == 4;
        projectGroup = new ProjectGroup(myProjects, 3, 3);
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 4;
        assert projectGroup.time() == 3;

        // different order
        myProjects = new ArrayList<>() {
            {
                add(singletonProjects.get("SingletonLong"));
                add(singletonProjects.get("SingletonShort"));
            }
        };
        projectGroup = new ProjectGroup(myProjects, 2, 4);
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 4;
        assert projectGroup.time() == 4;
        projectGroup = new ProjectGroup(myProjects, 3, 3);
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 4;
        assert projectGroup.time() == 3;
    }

    @Test
    public void constructorWithProjects2CountStarterAndComplex() {
        List<ProjectLinked> myProjects = new ArrayList<>() {
            {
                add(complexProjects.get("ComplexStarter"));
                add(complexProjects.get("ComplexA"));
            }
        };
        ProjectGroup projectGroup = new ProjectGroup(myProjects, 1, 2);
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 3;
        assert projectGroup.time() == 2;

        myProjects = new ArrayList<>() {
            {
                add(complexProjects.get("ComplexA"));
                add(complexProjects.get("ComplexStarter"));
            }
        };
        projectGroup = new ProjectGroup(myProjects, 1, 2);
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 3;
        assert projectGroup.time() == 2;
    }

    @Test
    public void constructorWithProjects4CountAllComplex() {
        //        S A L  <== ProjectNames
        //          B    <== ProjectNames
        List<ProjectLinked> myProjects = new ArrayList<>() {
            {
                add(complexProjects.get("ComplexStarter"));
                add(complexProjects.get("ComplexA"));
                add(complexProjects.get("ComplexB"));
                add(complexProjects.get("ComplexLast"));
            }
        };
        ProjectGroup projectGroup = new ProjectGroup(myProjects, 2, 3);
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 9;
        assert projectGroup.time() == 3;
        projectGroup = new ProjectGroup(myProjects, 1, 4);
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 9;
        assert projectGroup.time() == 4;
    }

    @Test
    public void constructorWithProjectsAllProjects() {
        ProjectGroup projectGroup = new ProjectGroup(allProjectsLinked.values(), 2, 5);
        isSameCollection(allProjectsLinked.values(), projectGroup);
        assert projectGroup.worth() == 13;
        assert projectGroup.time() == 5;

        projectGroup = new ProjectGroup(allProjectsLinked.values(), 2, 100);
        isSameCollection(allProjectsLinked.values(), projectGroup);
        assert projectGroup.worth() == 13;
        assert projectGroup.time() == 5;

        projectGroup = new ProjectGroup(allProjectsLinked.values(), 4, 3);
        isSameCollection(allProjectsLinked.values(), projectGroup);
        assert projectGroup.worth() == 13;
        assert projectGroup.time() == 3;
    }

    private void isSameCollection(Collection<ProjectLinked> myProjects, ProjectGroup projectGroup) {
        assert projectGroup.getProjects().containsAll(myProjects);
        assert myProjects.containsAll(projectGroup.getProjects());
    }
}
