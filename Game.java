package com.xil.botfn.v2;

import com.xil.botfn.Config.SnipeUser;
import com.xil.botfn.Main;
import com.xil.fnapi.v2.FortniteAPI;
import com.xil.fnapi.v2.Stats;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Game implements Runnable {
    private final String icon = "https://pbs.twimg.com/profile_images/1036076750885638144/WdCBv0OX_400x400.jpg";
    private TextChannel hostChannel;
    private String identifier;
    private SnipeUser[] players;
    private Stats[] stats;
    private long[] updateTimestamps;

    public Game(TextChannel hostChannel, String identifier, SnipeUser[] players) {
        this.hostChannel = hostChannel;
        this.identifier = identifier;
        this.players = players;
        this.stats = new Stats[100];
        this.updateTimestamps = new long[100];
    }

    @Override
    public void run() {
        // get initial stats
        System.out.println("Getting initial stats...");
        FortniteAPI fnapi = Main.getFnapi();
        int count = 0; // num times waited 2 seconds
        for (int i = 0; i <= 99; i++) {
            if (this.players[i] != null) {
                SnipeUser player = this.players[i];
                try {
                    stats[i] = fnapi.getStats(player.getFortniteUserId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                count++;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Waiting roughly 18 minutes...");
        // wait 18 mins - 2 seconds for every player
        try {
            Thread.sleep((18 * 60 - (count * 2)) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Waiting for stats change...");
        // loop over first player in game until stats change, max 8 minutes
        count = 0;
        for (int i = 0; i <= 99; i++) {
            if (this.players[i] != null) {
                while (true) {
                    SnipeUser player = this.players[i];
                    try {
                        Stats ns = fnapi.getStats(player.getFortniteUserId());
                        Stats diff = Stats.subtract(this.stats[i], ns).calculate();
                        if (diff.getTotalMatchesPlayed() > 0) {
                            System.out.println("Caught stats update for match '" + identifier + "'");
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        count++;
                        if (count >= 240) {
                            System.out.println("Match '" + identifier + "' 26 minutes without stats update on first entry");
                            break;
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }

        System.out.println("Grabbing stats on all players...");
        // stat update has happened on first player, get stats on all players and store them, wait 1 second between requests
        boolean done = false;
        count = 0;
        while (!done) {
            done = true;
            for (int i = 0; i <= 99; i++) {
                if (players[i] != null && stats[i] != null) {
                    if (updateTimestamps[i] == 0) {
                        SnipeUser player = this.players[i];
                        try {
                            long time = System.currentTimeMillis();
                            Stats ns = fnapi.getStats(player.getFortniteUserId());
                            Stats diff = Stats.subtract(this.stats[i], ns).calculate();
                            if (diff.getTotalMatchesPlayed() > 0) {
                                this.stats[i] = diff;
                                this.updateTimestamps[i] = time;
                                continue;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        done = false;

                        try {
                            Thread.sleep(1000);
                            count++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // if it has been roughly 26 minutes since game start, exit loop
            if (count >= (8 * 60)) {
                done = true;
            }
        }

        // TODO: find groups of stat changes that occurred around the same time and ONLY accept those

        System.out.println("Building match results...");
        List<Player> players = new ArrayList<>();
        Player winner = null;
        for (int i = 0; i <= 99; i++) {
            if (this.players[i] != null) {
                SnipeUser user = this.players[i];
                if (this.stats[i] != null) {
                    Stats ns = this.stats[i];
                    int numKill = (int) ns.getTotalKills();
                    boolean isWinner = ns.getTotalWins() > 0;
                    Player player = new Player(user, isWinner, numKill);
                    if (isWinner) {
                        winner = player;
                    }
                    players.add(player);
                } else {
                    System.out.println("Player <@" + user.getDiscordUserId() + "> updated stats not found");
                }
            }
        }

        System.out.println("Sending match results...");
        // sort by kills
        players.sort(Comparator.comparingInt(Player::getKills));
        this.sendMatchResults(players, winner);
        System.out.println("Match '" + this.identifier + "' done!");
    }

    public void sendMatchResults(List<Player> results, Player winner) {
        String result = "\uD83C\uDFC6 - " + (winner != null ? "<@" + winner.getUser().getDiscordUserId() + ">" : "No winner found");
        result += "\n\n";
        result += namesToStringWithKills(results);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Match Results [" + this.identifier + "]")
                .setDescription(result)
                .setFooter("Bot by @xil", this.icon);
        this.hostChannel.sendMessage(embedBuilder.build()).queue();
    }

    private String namesToStringWithKills(List<Player> players) {
        boolean first = true;
        StringBuilder reeeee = new StringBuilder();
        for (Player player : players) {
            reeeee.append(first ? "" : "\n")
                    .append("\uD83D\uDC80 ")
                    .append(player.getKills())
                    .append(" - ").append("<@")
                    .append(player.getUser().getDiscordUserId())
                    .append(">");
            first = false;
        }
        return reeeee.toString();
    }

    public TextChannel getHostChannel() {
        return hostChannel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public SnipeUser[] getPlayers() {
        return players;
    }

    public Stats[] getStats() {
        return stats;
    }

    public long[] getUpdateTimestamps() {
        return updateTimestamps;
    }
}
