package test.analyzer.bridge;

import aaron.analyzer.algorithm.ElaborateAlgorithm;
import aaron.analyzer.bridge.AllQuests;
import aaron.analyzer.bridge.Project;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.*;

import static test.analyzer.bridge.TestHelper.isSameCollection;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestProjectGroupAddProjectsGroups { // add in groups projects below this
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
    public void addAllProjectsImpossibleStart() {
        ProjectGroup projectGroup = new ProjectGroup(0, 0);
        boolean added = projectGroup.addProject(singletonProjects.get("SingletonShort"), null);
        assert !added;
        added = projectGroup.addProject(singletonProjects.get("SingletonLong"), null);
        assert !added;
    }

    @Test
    public void addAllProjectsSingleton1Count() {
        List<ProjectLinked> myProjects = new ArrayList<>() {
            {
                add(allProjectsLinked.get("SingletonShort"));
            }
        };
        ProjectGroup projectGroup = new ProjectGroup(myProjects.get(0).getWorkersCount(), myProjects.get(0).getTime());
        boolean added = projectGroup.addProjects(myProjects);
        assert added;
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 2;
        assert projectGroup.time() == 1;
    }

    @Test
    public void addAllProjectsSingleton2Count() {
        List<ProjectLinked> myProjects = new ArrayList<>() {
            {
                add(singletonProjects.get("SingletonShort"));
                add(singletonProjects.get("SingletonLong"));
            }
        };
        ProjectGroup projectGroup = new ProjectGroup(2, 4);
        boolean added = projectGroup.addProjects(myProjects);
        assert added;
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 4;
        assert projectGroup.time() == 4;

        projectGroup = new ProjectGroup(3, 3);
        added = projectGroup.addProjects(myProjects);
        assert added;
        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 4;
        assert projectGroup.time() == 3;

        // different order
        Collections.reverse(myProjects);
        projectGroup = new ProjectGroup(2, 4);
        added = projectGroup.addProjects(myProjects);
        assert added;

        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 4;
        assert projectGroup.time() == 4;
        projectGroup = new ProjectGroup(3, 3);
        added = projectGroup.addProjects(myProjects);
        assert added;

        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 4;
        assert projectGroup.time() == 3;
    }

    @Test
    public void addAllComplex2Count() {
        List<ProjectLinked> myProjects = new ArrayList<>() {
            {
                add(complexProjects.get("ComplexStarter"));
                add(complexProjects.get("ComplexA"));
            }
        };
        ProjectGroup projectGroup = new ProjectGroup(1, 2);
        boolean added = projectGroup.addProjects(myProjects);
        assert added;

        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 3;
        assert projectGroup.time() == 2;

        myProjects = new ArrayList<>() {
            {
                add(complexProjects.get("ComplexA"));
                add(complexProjects.get("ComplexStarter"));
            }
        };
        projectGroup = new ProjectGroup(1, 2);
        added = projectGroup.addProjects(myProjects);
        assert added;
        isSameCollection(Collections.emptyList(), projectGroup);
        assert projectGroup.worth() == 0;
        assert projectGroup.time() == 0;
    }

    @Test
    public void addAllAllComplex() {
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
        ProjectGroup projectGroup = new ProjectGroup(2, 3);
        boolean added = projectGroup.addProjects(myProjects);
        assert added;

        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 9;
        assert projectGroup.time() == 3;
        projectGroup = new ProjectGroup(1, 4);
        added = projectGroup.addProjects(myProjects);
        assert added;

        isSameCollection(myProjects, projectGroup);
        assert projectGroup.worth() == 9;
        assert projectGroup.time() == 4;
    }

    @Test
    public void addAllAllProjects() {
        for (int i = 0; i < 4; i++) {
            List<ProjectLinked> myProjects = null;
            switch (i) {
                case 0:
                    myProjects = new ArrayList<>() {
                        {
                            add(singletonProjects.get("SingletonShort"));
                            add(singletonProjects.get("SingletonLong"));
                            add(complexProjects.get("ComplexStarter"));
                            add(complexProjects.get("ComplexA"));
                            add(complexProjects.get("ComplexB"));
                            add(complexProjects.get("ComplexLast"));
                        }
                    };
                    break;
                case 1:
                    myProjects = new ArrayList<>() {
                        {
                            add(singletonProjects.get("SingletonShort"));
                            add(singletonProjects.get("SingletonLong"));
                            add(complexProjects.get("ComplexStarter"));
                            add(complexProjects.get("ComplexB"));
                            add(complexProjects.get("ComplexA"));
                            add(complexProjects.get("ComplexLast"));
                        }
                    };
                    break;
                case 2:
                    myProjects = new ArrayList<>() {
                        {
                            add(singletonProjects.get("SingletonLong"));
                            add(singletonProjects.get("SingletonShort"));
                            add(complexProjects.get("ComplexStarter"));
                            add(complexProjects.get("ComplexA"));
                            add(complexProjects.get("ComplexB"));
                            add(complexProjects.get("ComplexLast"));
                        }
                    };
                    break;
                case 3:
                    myProjects = new ArrayList<>() {
                        {
                            add(singletonProjects.get("SingletonLong"));
                            add(singletonProjects.get("SingletonShort"));
                            add(complexProjects.get("ComplexStarter"));
                            add(complexProjects.get("ComplexB"));
                            add(complexProjects.get("ComplexA"));
                            add(complexProjects.get("ComplexLast"));
                        }
                    };
                    break;
            }
            ProjectGroup projectGroup = new ProjectGroup(2, 5);
            boolean added = projectGroup.addProjects(myProjects);
            assert added;

            isSameCollection(myProjects, projectGroup);
            assert projectGroup.worth() == 13;
            assert projectGroup.time() == 5;

            projectGroup = new ProjectGroup(2, 100);
            added = projectGroup.addProjects(myProjects);
            assert added;

            projectGroup.getProjectTimeline(); // just redefines the project with a possible shorter time
            isSameCollection(myProjects, projectGroup);
            assert projectGroup.worth() == 13;
            assert projectGroup.time() == 5;

            projectGroup = new ProjectGroup(4, 3);
            added = projectGroup.addProjects(myProjects);
            assert added;

            isSameCollection(myProjects, projectGroup);
            assert projectGroup.worth() == 13;
            assert projectGroup.time() == 3;
        }
    }
}
