package com.xil.botfn.v2;

import com.xil.botfn.Config.SnipeUser;
import com.xil.fnapi.v2.Configuration;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;

public class SnipeManager extends ListenerAdapter {
    private HashMap<String, Snipe> running;
    // string host channel id
    private HashMap<String, String> ids;
    // string code channel id, string host channel id

    public SnipeManager() {
        this.running = new HashMap<>();
        this.ids = new HashMap<>();
    }

    public void startGame(String hostChannelId, String codeChannelId){
        this.ids.put(codeChannelId, hostChannelId);
        this.running.put(hostChannelId, new Snipe(hostChannelId, codeChannelId));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String regex = "\\S{3}";
        if (event.getMessage().getContentRaw().matches(regex)) {
            String codeChannelId = event.getMessage().getChannel().getId();
            if (ids.containsKey(codeChannelId)) {
                String hostChannelId = ids.get(codeChannelId);
                if (running.containsKey(hostChannelId)) {
                    Snipe snipe = running.get(hostChannelId);
                    CodeManager codeManager = snipe.getCodeManager();

                    String discordId = event.getAuthor().getId();
                    Configuration userConfig = new Configuration("users/" + discordId, SnipeUser.class);
                    if (userConfig.exists()) {
                        SnipeUser user = userConfig.read();
                        codeManager.addPlayer(event.getMessage().getContentRaw(), user);
                    }
                }
            }
        }
    }
}
