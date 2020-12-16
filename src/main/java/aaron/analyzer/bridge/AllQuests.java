package aaron.analyzer.bridge;

import com.opencsv.CSVReader;
import com.opencsv.bean.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

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
