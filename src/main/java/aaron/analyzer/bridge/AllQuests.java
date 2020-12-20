package aaron.analyzer.bridge;

import com.opencsv.CSVReader;
import com.opencsv.bean.*;

import java.io.*;
import java.util.*;

public class AllQuests {
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

        Map<String, Project> nameToQuest = new HashMap<>();
        for (Project project : ALL_PROJECTS) nameToQuest.put(project.getName(), project);
        for (Project project : ALL_PROJECTS) {
            String[] requirementNames = project.getRequirementNames();
            int[] immediateRequirements = new int[requirementNames.length];
            for (int i = 0; i < requirementNames.length; i++) {
                Project projectRequired = nameToQuest.get(requirementNames[i]);
                if (projectRequired == null) {
                    System.err.printf("Project '%s' has the requirement '%s', which isn't a project name..\n", project.getName(), requirementNames[i]);
                    System.exit(1);
                    return;
                }
                immediateRequirements[i] = projectRequired.getUid();
            }
            project.setImmediateRequirements(immediateRequirements);
        }
        Map<Integer, Project> uidToQuest = new HashMap<>();
        for (Project project : ALL_PROJECTS) uidToQuest.put(project.getUid(), project);
        for (Project project : ALL_PROJECTS) {
            Set<Integer> reqs = new HashSet<>();
            for (Integer req : project.getImmediateRequirements()) {
                reqs.add(req);
                reqs.addAll(getReqs(req, uidToQuest));
            }
            int[] reqsArray = new int[reqs.size()];
            int i = 0;
            for (int req : reqs) reqsArray[i++] = req;
            project.setAllRequirements(reqsArray);
        }
    }

    private static Set<Integer> getReqs(Integer req, Map<Integer, Project> uidToQuest) {
        Set<Integer> reqs = new HashSet<>();
        for (Integer reqSub : uidToQuest.get(req).getImmediateRequirements())
            reqs.addAll(getReqs(reqSub, uidToQuest));
        reqs.add(req);
        return reqs;
    }

    public static class SimpleProject {
        public String name;
        public int duration;
        public int value;
        public String requirements;
        public int playersRequired;

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
