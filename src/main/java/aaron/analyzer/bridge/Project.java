package aaron.analyzer.bridge;

public class Project {
    private static int currentUid = 0;
    private final String name;
    private final int uid;
    private int[] immediateRequirements;
    private int[] allRequirements;
    private final String[] requirementNames;
    private final int time;
    private final int worth;
    private final int playersRequired;

    public Project(Project project) {
        this.name = project.name;
        this.uid = project.uid;
        this.immediateRequirements = project.immediateRequirements;
        this.allRequirements = project.allRequirements;
        this.time = project.time;
        this.worth = project.worth;
        this.playersRequired = project.playersRequired;
        this.requirementNames = project.requirementNames;
    }

    public Project(AllProjects.SimpleProject simpleProject) {
        this.name = simpleProject.name;
        this.immediateRequirements = null;
        this.allRequirements = null;
        this.requirementNames = (simpleProject.requirements.isBlank()) ?
                new String[0] :
                simpleProject.requirements.split(",");
        this.time = simpleProject.duration;
        this.worth = simpleProject.value;
        this.playersRequired = simpleProject.playersRequired;
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

    public boolean isImmediateRequirementsEmpty() {
        return immediateRequirements.length == 0;
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

    public int getWorkersCount() {
        return playersRequired;
    }

    public int getUserEffectiveTime() {
        return time * playersRequired;
    }
}
