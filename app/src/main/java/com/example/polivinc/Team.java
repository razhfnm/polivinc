public class Team {

    private String teamName;
    private String teamId;
    private String teamCode;

    // Constructor
    public Team(String teamName, String teamId) {
        this.teamName = teamName;
        this.teamId = teamId;
        this.teamCode = generateTeamCode();
    }

    // Métodos getter
    public String getTeamName() {
        return teamName;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getTeamCode() {
        return teamCode;
    }

    // Generar código único de equipo
    private String generateTeamCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
