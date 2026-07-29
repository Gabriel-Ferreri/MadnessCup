    package Coocos.madnessCup.game;

    import Coocos.madnessCup.MadnessCup;
    import org.bukkit.ChatColor;

    import java.util.List;
    import java.util.UUID;

    public class Team {
        private List<UUID> players;
        private String teamName;
        private final MadnessCup plugin;
        private ChatColor teamColor;
        private Integer teamCoins;
        private Integer teamLimit;

        public Team(MadnessCup plugin, List<UUID> players, String teamName, ChatColor teamColor, Integer teamCoins, Integer teamLimit) {
            this.plugin = plugin;
            this.players = players;
            this.teamName = teamName;
            this.teamColor = teamColor;
            this.teamCoins = teamCoins;
            this.teamLimit = teamLimit;
        }

        public MadnessCup getPlugin() { return plugin; }
        public List<UUID> getPlayers() { return players; }
        public String getTeamName() { return teamName; }
        public ChatColor getTeamColor() { return teamColor; }
        public Integer getTeamCoins() { return teamCoins; }
        public Integer getTeamLimit() { return teamLimit; }

        public void setPlayers(List<UUID> players) { this.players = players; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public void setTeamColor(ChatColor teamColor) { this.teamColor = teamColor; }
        public void setTeamCoins(Integer teamCoins) { this.teamCoins = teamCoins; }
        public void setTeamLimit(Integer teamLimit) { this.teamLimit = teamLimit; }

        /** The logic is in team manager so this class becomes a pure data object
         * and TeamManager is a pure logic class, which makes everything easier to
         * test, extend, maintain and harder to break. We also avoid circular
         * dependencies
         */
        public void addPlayer(UUID player) {
            players.add(player);
        }
        public void removePlayer(UUID player) { this.players.remove(player); }
    }
