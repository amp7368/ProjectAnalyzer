package aaron.analyzer.algorithm;

import aaron.analyzer.bridge.Project;
import aaron.analyzer.bridge.ProjectGroup;
import aaron.analyzer.bridge.ProjectLinked;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Sorting {
    public static void sortQuestsByAPT(List<ProjectLinked> quests) {
        quests.sort((o1, o2) -> {
            double apt = (((double) o2.getWorth() / (double) o2.getTime()) - ((double) o1.getWorth() / (double) o1.getTime()));
            if (apt > 0) return 1;
            else if (apt == 0) return 0;
            return -1;
        });
    }

    public static List<Set<ProjectLinked>> sortQuestCombinationByAPT(Collection<Set<ProjectLinked>> allQuestLines) {
        List<Set<ProjectLinked>> sortedQuestLines = new ArrayList<>(allQuestLines);
        sortedQuestLines.sort((c1, c2) -> {
            long worth1 = 0;
            long time1 = 0;
            long worth2 = 0;
            long time2 = 0;
            for (Project o1 : c1) {
                worth1 += o1.getWorth();
                time1 += o1.getTime();
            }
            for (Project o2 : c2) {
                worth2 += o2.getWorth();
                time2 += o2.getTime();
            }
            // 10 is arbitrary. integer division should be fine in most cases.
            BigDecimal apt1 = new BigDecimal(worth1).divide(new BigDecimal(time1), 10, RoundingMode.HALF_EVEN);
            BigDecimal apt2 = new BigDecimal(worth2).divide(new BigDecimal(time2), 10, RoundingMode.HALF_EVEN);
            return apt2.compareTo(apt1);
        });
        return sortedQuestLines;
    }

    public static void sortProjectGroupsByAmount(List<ProjectGroup> projectGroups) {
        projectGroups.sort((o1, o2) -> {
            long difference = o2.worth() - o1.worth();
            if (difference > 0) return 1;
            else if (difference == 0) return 0;
            return -1;
        });
    }
}

