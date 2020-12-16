package aaron.analyzer.bridge;

import com.opencsv.CSVReader;
import com.opencsv.bean.*;

import java.io.*;
import java.util.*;

public class AllQuests {
    public final static Collection<Quest> allQuests = new ArrayList<>();

    public static void initialize(String filePath) throws IOException {
        CSVReader reader = new CSVReader(new BufferedReader(new FileReader(filePath)));
        HeaderColumnNameTranslateMappingStrategy<SimpleQuest> mappingStrat = new HeaderColumnNameTranslateMappingStrategy<>();
        mappingStrat.setType(SimpleQuest.class);
        mappingStrat.setColumnMapping(new HashMap<>() {{
            put("name", "name");
            put("duration", "duration");
            put("value", "value");
            put("requirements", "requirements");
        }});
        CsvToBean<SimpleQuest> transfer = new CsvToBeanBuilder<SimpleQuest>(reader)
                .withMappingStrategy(mappingStrat).build();
        Iterator<SimpleQuest> iterator = transfer.iterator();
        allQuests.clear();
        while (iterator.hasNext()) allQuests.add(new Quest(iterator.next()));
        reader.close();

        Map<String, Quest> nameToQuest = new HashMap<>();
        for (Quest quest : allQuests) nameToQuest.put(quest.getName(), quest);
        for (Quest quest : allQuests) {
            String[] requirementNames = quest.getRequirementNames();
            int[] immediateRequirements = new int[requirementNames.length];
            for (int i = 0; i < requirementNames.length; i++) {
                immediateRequirements[i] = nameToQuest.get(requirementNames[i]).getUid();
            }
            quest.setImmediateRequirements(immediateRequirements);
        }
        Map<Integer, Quest> uidToQuest = new HashMap<>();
        for (Quest quest : allQuests) uidToQuest.put(quest.getUid(), quest);
        for (Quest quest : allQuests) {
            Set<Integer> reqs = new HashSet<>();
            for (Integer req : quest.getImmediateRequirements()) {
                reqs.add(req);
                reqs.addAll(getReqs(req, uidToQuest));
            }
            int[] reqsArray = new int[reqs.size()];
            int i = 0;
            for (int req : reqs) reqsArray[i++] = req;
            quest.setAllRequirements(reqsArray);
        }
    }

    private static Set<Integer> getReqs(Integer req, Map<Integer, Quest> uidToQuest) {
        Set<Integer> reqs = new HashSet<>();
        for (Integer reqSub : uidToQuest.get(req).getImmediateRequirements())
            reqs.addAll(getReqs(reqSub, uidToQuest));
        reqs.add(req);
        return reqs;
    }

    public static void print() {
        for (Quest quest : allQuests) {
            quest.print();
        }
    }

    public static class SimpleQuest {
        public String name;
        public int duration;
        public int value;
        public String requirements;
    }
}
