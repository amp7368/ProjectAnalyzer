package aaron.analyzer.bridge;

import java.util.ArrayList;
import java.util.Collection;

public class QuestLinked extends Quest {
    public final Collection<Integer> allRequirements = new ArrayList<>();
    public final int[] immediateRequirements;
    public final Collection<Integer> requireMe = new ArrayList<>();

    public QuestLinked(Quest quest) {
        super(quest);
        for (Integer questReq : quest.getAllRequirements()) {
            this.allRequirements.add(questReq);
        }
        this.immediateRequirements = quest.getAllRequirements();
    }
}
