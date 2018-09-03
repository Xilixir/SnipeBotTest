package com.xil.botfn.v2;

import com.xil.botfn.Config.SnipeUser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class CodeManager extends ListenerAdapter {
    private final String icon = "https://pbs.twimg.com/profile_images/1036076750885638144/WdCBv0OX_400x400.jpg";
    private HashMap<String, SnipeUser[]> matches;

    public CodeManager() {
        this.matches = new HashMap<>();
    }

    public void setFields(EmbedBuilder embedBuilder){
        int totalPlayers = 0;
        for (Map.Entry<String, SnipeUser[]> entry : matches.entrySet()){
            int numPlayers = this.getNumPlayers(entry.getKey());
            totalPlayers += numPlayers;
            embedBuilder.addField(entry.getKey() + " [" + numPlayers + " players]", namesToString(entry.getValue()), false);
        }
        embedBuilder.setFooter(totalPlayers + " total players | bot by @xil", icon);
    }

    private String namesToString(SnipeUser[] players){
        boolean first = true;
        StringBuilder reeeee = new StringBuilder();
        for (int i = 0; i <= 99; i++) {
            if (players[i] != null) {
                SnipeUser user = players[i];
                reeeee.append(first ? "" : "\n").append("<@").append(user.getDiscordUserId()).append(">");
                first = false;
            }
        }
        return reeeee.toString();
    }

    int getNumPlayers(String matchIdentifier){
        if (matches.containsKey(matchIdentifier)) {
            SnipeUser[] playerList = matches.get(matchIdentifier);
            int count = 0;
            for (int i = 0; i <= 99; i++) {
                if (playerList[i] != null){
                    count++;
                }
            }
            return count;
        }
        return 0;
    }

    void addPlayer(String matchIdentifier, SnipeUser player){
        if (matches.containsKey(matchIdentifier)){
             SnipeUser[] playerList = matches.get(matchIdentifier);
             for (int i = 0; i <= 99; i++){
                 if (playerList[i] == null){
                     System.out.println("Player '<@" + player.getDiscordUserId() + ">' registered for match '" + matchIdentifier + "'");
                     playerList[i] = player;
                     return;
                 }
             }
        } else {
            SnipeUser[] playerList = new SnipeUser[100];
            System.out.println("Player '<@" + player.getDiscordUserId() + ">' registered for match '" + matchIdentifier + "'");
            playerList[0] = player;
            matches.put(matchIdentifier, playerList);
        }
    }

    void removePlayer(SnipeUser player){
        for (Map.Entry<String, SnipeUser[]> entry : matches.entrySet()){
            SnipeUser[] playerList = entry.getValue();
            for (int i = 0; i <= 99; i++){
                if (playerList[i] != null) {
                    if (playerList[i].getDiscordUserId().equals(player.getDiscordUserId())) {
                        playerList[i] = null;
                        System.out.println("Player '<@" + player.getDiscordUserId() + ">' removed from match with identifier '" + entry.getKey() + "'");
                        return;
                    }
                }
            }
        }
    }

    public void setMatches(HashMap<String, SnipeUser[]> matches) {
        this.matches = matches;
    }

    public HashMap<String, SnipeUser[]> getMatches() {
        return matches;
    }

    private void removeIdentifier(String matchIdentifier){
        matches.remove(matchIdentifier);
    }
}
