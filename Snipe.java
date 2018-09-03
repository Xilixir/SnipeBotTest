package com.xil.botfn.v2;

import com.xil.botfn.Config.SnipeUser;
import com.xil.botfn.Main;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class Snipe implements Runnable {
    private final String icon = "https://pbs.twimg.com/profile_images/1036076750885638144/WdCBv0OX_400x400.jpg";
    private CodeManager codeManager;
    private TextChannel hostChannel;
    private TextChannel codeChannel;
    private Message codeMessage;
    private int codeTimer = 60; // 60 sec code timer
    private final int minimumToApprove = 1; // 8 min players

    public Snipe(String hostChannelId, String codeChannelId) {
        this.hostChannel = Main.getJda().getTextChannelById(hostChannelId);
        this.codeChannel = Main.getJda().getTextChannelById(codeChannelId);
        new Thread(this).start();
    }

    @Override
    public void run() {
        this.sendMatchStarting();
        // send voice audio
        this.codeMessage = this.sendWaitingForCodes();
        this.codeManager = new CodeManager();

        // refresh code message embed
        while (codeTimer > 0) {
            try {
                codeTimer -= 2;
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            codeMessage.editMessage(buildCodeMessage()).queue();
        }

        // remove matches below x amount of players
        HashMap<String, SnipeUser[]> approvedMatches = new HashMap<>();
        for (Map.Entry<String, SnipeUser[]> entry : this.codeManager.getMatches().entrySet()){
            if (this.codeManager.getNumPlayers(entry.getKey()) >= this.minimumToApprove){
                approvedMatches.put(entry.getKey(), entry.getValue());
            }
        }
        this.codeManager.setMatches(approvedMatches);
        // refresh code message embed one last time
        codeMessage.editMessage(buildCodeMessage()).queue();

        for (Map.Entry<String, SnipeUser[]> entry : this.codeManager.getMatches().entrySet()){
            new Thread(new Game(hostChannel, entry.getKey(), entry.getValue())).start();
        }
    }

    private MessageEmbed buildCodeMessage(){
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Waiting for codes...")
                .setFooter("Bot by @xil", icon);
        this.codeManager.setFields(embedBuilder);
        return embedBuilder.build();
    }

    private void sendMatchStarting(){
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Match Starting...")
                .setFooter("Bot by @xil", icon);
        hostChannel.sendMessage(embedBuilder.build()).queue();
    }

    private Message sendWaitingForCodes(){
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Waiting for codes...")
                .setFooter("Bot by @xil", icon);
        return hostChannel.sendMessage(embedBuilder.build()).complete();
    }

    public CodeManager getCodeManager() {
        return codeManager;
    }
}
