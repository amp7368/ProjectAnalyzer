package aaron.analyzer.bridge;

public class Quest {
    private static int currentUid = 0;
    private final String name;
    private final int uid;
    private int[] immediateRequirements;
    private int[] allRequirements;
    private String[] requirementNames;
    private final int time;
    private final int worth;

    public Quest(String name, int[] immediateRequirements, int[] allRequirements, int time, int worth) {
        this.name = name;
        this.immediateRequirements = immediateRequirements;
        this.allRequirements = allRequirements;
        this.time = time;
        this.worth = worth;
        this.uid = currentUid++;
    }

    public Quest(Quest quest) {
        this.name = quest.name;
        this.uid = quest.uid;
        this.immediateRequirements = quest.immediateRequirements;
        this.allRequirements = quest.allRequirements;
        this.time = quest.time;
        this.worth = quest.worth;
    }

    public Quest(AllQuests.SimpleQuest simpleQuest) {
        this.name = simpleQuest.name;
        this.immediateRequirements = null;
        this.allRequirements = null;
        this.requirementNames = simpleQuest.requirements.split(",");
        this.time = simpleQuest.duration;
        this.worth = simpleQuest.value;
        this.uid = currentUid++;
    }

    @Override
    public int hashCode() {
        return uid;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Quest && this.uid == ((Quest) obj).uid;
    }

    public void setAllRequirements(int[] allRequirements) {
        this.allRequirements = allRequirements;
    }

    public String getName() {
        return name;
    }

    public int getUid() {
        return uid;
    }

    public int[] getImmediateRequirements() {
        return immediateRequirements;
    }

    public int[] getAllRequirements() {
        return allRequirements;
    }

    public int getTime() {
        return time;
    }

    public int getWorth() {
        return worth;
    }

    public void print() {
        System.out.printf("uid:%d, name:%s, time:%d, worth:%d,reqs:%s\n", uid, name, time, worth, String.join(".", requirementNames));
    }
}
