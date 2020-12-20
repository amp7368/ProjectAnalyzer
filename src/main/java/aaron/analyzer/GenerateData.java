package aaron.analyzer;

import aaron.analyzer.bridge.AllQuests.SimpleProject;

import java.io.*;
import java.util.*;

public class GenerateData {
    private static final int PROJECT_LENGTH = 1000;
    private static final int MAX_DURATION = 100;
    private static final int MAX_VALUE = 50;
    private static final int MAX_REQUIREMENTS_COUNT = 10;
    private static final int CHAIN_LENGTH = 5;
    private static final int MAX_PLAYERS_REQUIRED = 5;

    public static void main(String[] args) throws IOException {
        Random random = new Random();
        SimpleProject[] allProjects = new SimpleProject[PROJECT_LENGTH];
        for (int i = 0; i < allProjects.length; i++) {
            String name = String.valueOf(i);
            int duration = Math.max(1, random.nextInt(MAX_DURATION));
            int value = random.nextInt(MAX_VALUE);
            int playersRequired = Math.max(1, random.nextInt(MAX_PLAYERS_REQUIRED));
            String[] requirementsRaw = new String[random.nextInt(MAX_REQUIREMENTS_COUNT)];
            for (int j = 0; j < requirementsRaw.length; j++) {
                requirementsRaw[j] = String.valueOf(random.nextInt(allProjects.length));
            }
            Set<String> requirements = new HashSet<>() {{
                addAll(Arrays.asList(requirementsRaw));
            }};
            allProjects[i] = new SimpleProject(name, duration, value, String.join(",", requirements), playersRequired);
        }
        boolean isFail = true;
        failLoop:
        while (isFail) {
            isFail = false;
            for (SimpleProject project : allProjects) {
                // go through and check that the loops aren't too long
                String[] reqs = project.requirements.split(",");
                for (String req : reqs) {
                    if (req.isEmpty()) continue;
                    if (parseChain(allProjects, Integer.parseInt(req), CHAIN_LENGTH, Collections.singletonList(req))) {
                        isFail = true;
                        continue failLoop;
                    }

                }
            }
        }
        Writer writer = new BufferedWriter(new FileWriter("test.csv"));
        writer.write("name,duration,value,requirements,workers");
        for (SimpleProject project : allProjects) {
            writer.write("\n");
            writer.write(project.name);
            writer.write(",");
            writer.write(String.valueOf(project.duration));
            writer.write(",");
            writer.write(String.valueOf(project.value));
            writer.write(",");
            writer.write(!project.requirements.contains(",") ? "" : "\"" + project.requirements + "\"");
            writer.write(",");
            writer.write(String.valueOf(project.playersRequired));
        }
        writer.close();
    }

    private static boolean parseChain(SimpleProject[] allProjects, int req, int depthLeft, List<String> doNotReqThis) {
        if (depthLeft == 0) {
            return true;
        }
        for (String subReq : allProjects[req].requirements.split(",")) {
            if (subReq.isEmpty()) continue;
            List<String> doNotReqThisSub = new ArrayList<>(doNotReqThis) {{
                add(subReq);
            }};
            if (parseChain(allProjects, Integer.parseInt(subReq), depthLeft - 1, doNotReqThisSub)) {
                if (depthLeft == 1) {
                    List<String> reqs = new ArrayList<>(Arrays.asList(allProjects[req].requirements.split(",")));
                    for (String i : doNotReqThisSub) {
                        reqs.remove(i);
                    }
                    allProjects[req].requirements = String.join(",", reqs);
                }
                return true;
            }
        }
        return false;
    }
}
