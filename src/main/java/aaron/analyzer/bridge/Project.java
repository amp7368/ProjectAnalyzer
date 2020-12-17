package aaron.analyzer.bridge;

public class Project {
    private static int currentUid = 0;
    private final String name;
    private final int uid;
    private int[] immediateRequirements;
    private int[] allRequirements;
    private String[] requirementNames;
    private final int time;
    private final int worth;

    public Project(String name, int[] immediateRequirements, int[] allRequirements, int time, int worth) {
        this.name = name;
        this.immediateRequirements = immediateRequirements;
        this.allRequirements = allRequirements;
        this.time = time;
        this.worth = worth;
        this.uid = currentUid++;
    }

    public Project(Project project) {
        this.name = project.name;
        this.uid = project.uid;
        this.immediateRequirements = project.immediateRequirements;
        this.allRequirements = project.allRequirements;
        this.time = project.time;
        this.worth = project.worth;
    }

    public Project(AllQuests.SimpleProject simpleProject) {
        this.name = simpleProject.name;
        this.immediateRequirements = null;
        this.allRequirements = null;
        this.requirementNames = (simpleProject.requirements.isBlank()) ?
                new String[0] :
                simpleProject.requirements.split(",");
        this.time = simpleProject.duration;
        this.worth = simpleProject.value;
        this.uid = currentUid++;
    }

    @Override
    public int hashCode() {
        return uid;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Project && this.uid == ((Project) obj).uid;
    }

    public void setAllRequirements(int[] allRequirements) {
        this.allRequirements = allRequirements;
    }

    public void setImmediateRequirements(int[] immediateRequirements) {
        this.immediateRequirements = immediateRequirements;
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

    public String[] getRequirementNames() {
        return requirementNames;
    }

    public int getTime() {
        return time;
    }

    public int getWorth() {
        return worth;
    }

    public void print() {
        String[] reqs = new String[allRequirements.length];
        int i = 0;
        for (int req : allRequirements) {
            reqs[i++] = String.valueOf(req);
        }
        System.out.printf("uid:%d, name:%s, time:%d, worth:%d,reqs:%s\n", uid, name, time, worth,String.join(".",reqs) );
    }
}