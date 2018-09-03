package com.xil.botfn.v2;

import com.xil.botfn.Config.SnipeUser;

public class Player {
    private SnipeUser user;
    private boolean winner;
    private int kills;

    public Player(SnipeUser user, boolean winner, int kills) {
        this.user = user;
        this.winner = winner;
        this.kills = kills;
    }

    public SnipeUser getUser() {
        return user;
    }

    public boolean isWinner() {
        return winner;
    }

    public int getKills() {
        return kills;
    }
}
