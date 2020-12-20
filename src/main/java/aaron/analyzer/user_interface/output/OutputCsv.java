package aaron.analyzer.user_interface.output;

import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class OutputCsv {
    public static void out(ProjectGroup answer, int[][] timeline) throws IOException {
        Map<Integer, ProjectLinked> projects = new HashMap<>() {{
            for (ProjectLinked project : answer.getProjects())
                put(project.getUid(), project);
        }};
        Writer writer = new BufferedWriter(new FileWriter("out.csv"));
        writer.write("Workers");
        for (int i = 0; i < timeline[0].length; i++) {
            writer.write(",");
            writer.write("Time " + i);
        }
        writer.write('\n');
        int i = 1;
        for (int[] worker : timeline) {
            writer.write("Worker " + i++);
            for (int j : worker) {
                writer.write(",");
                if (j == -1) writer.write(".");
                else writer.write(projects.get(j).getName());
            }
            writer.write('\n');
        }
        writer.flush();
    }
}
