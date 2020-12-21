package aaron.analyzer.bridge;

import com.opencsv.CSVReader;
import com.opencsv.bean.*;

import java.io.*;
import java.util.*;

public class AllProjects {
    public final static Collection<Project> ALL_PROJECTS = new ArrayList<>();

    public static void initialize(String filePath) throws IOException {
        CSVReader reader = new CSVReader(new BufferedReader(new FileReader(filePath)));
        HeaderColumnNameTranslateMappingStrategy<SimpleProject> mappingStrat = new HeaderColumnNameTranslateMappingStrategy<>();
        mappingStrat.setType(SimpleProject.class);
        mappingStrat.setColumnMapping(new HashMap<>() {{
            put("name", "name");
            put("duration", "duration");
            put("value", "value");
            put("requirements", "requirements");
            put("workers", "playersRequired");
        }});
        CsvToBean<SimpleProject> transfer = new CsvToBeanBuilder<SimpleProject>(reader)
                .withMappingStrategy(mappingStrat).build();
        Iterator<SimpleProject> iterator = transfer.iterator();
        ALL_PROJECTS.clear();
        while (iterator.hasNext()) ALL_PROJECTS.add(new Project(iterator.next()));
        reader.close();

        initializeProjects(ALL_PROJECTS);
    }

    public static void initializeProjects(Collection<Project> projectsToInitialize) {
        Map<String, Project> nameToProject = new HashMap<>();
        for (Project project : projectsToInitialize) nameToProject.put(project.getName(), project);
        for (Project project : projectsToInitialize) {
            String[] requirementNames = project.getRequirementNames();
            int[] immediateRequirements = new int[requirementNames.length];
            for (int i = 0; i < requirementNames.length; i++) {
                Project projectRequired = nameToProject.get(requirementNames[i]);
                if (projectRequired == null) {
                    System.err.printf("Project '%s' has the requirement '%s', which isn't a project name..\n", project.getName(), requirementNames[i]);
                    System.exit(1);
                    return;
                }
                immediateRequirements[i] = projectRequired.getUid();
            }
            project.setImmediateRequirements(immediateRequirements);
        }
        Map<Integer, Project> uidToProjects = new HashMap<>();
        for (Project project : projectsToInitialize) uidToProjects.put(project.getUid(), project);
        for (Project project : projectsToInitialize) {
            Set<Integer> reqs = new HashSet<>();
            for (Integer req : project.getImmediateRequirements()) {
                reqs.add(req);
                reqs.addAll(getReqs(req, uidToProjects));
            }
            int[] reqsArray = new int[reqs.size()];
            int i = 0;
            for (int req : reqs) reqsArray[i++] = req;
            project.setAllRequirements(reqsArray);
        }
    }

    private static Set<Integer> getReqs(Integer req, Map<Integer, Project> uidToProjects) {
        Set<Integer> reqs = new HashSet<>();
        for (Integer reqSub : uidToProjects.get(req).getImmediateRequirements())
            reqs.addAll(getReqs(reqSub, uidToProjects));
        reqs.add(req);
        return reqs;
    }

    public static class SimpleProject {
        public String name;
        public int duration;
        public int value;
        public String requirements;
        public int playersRequired;

        // this is required for CSV mapping
        public SimpleProject() {
        }

        public SimpleProject(String name, int duration, int value, String requirements, int playersRequired) {
            this.name = name;
            this.duration = duration;
            this.value = value;
            this.requirements = requirements;
            this.playersRequired = playersRequired;
        }
    }

}
