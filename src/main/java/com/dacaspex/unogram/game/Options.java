package com.dacaspex.unogram.game;

public class Options {
    private int maxNumberOfPlayers;
    private int minimumAgentTurnSpeed;

    public Options(int maxNumberOfPlayers, int minimumAgentTurnSpeed) {
        this.maxNumberOfPlayers = maxNumberOfPlayers;
        this.minimumAgentTurnSpeed = minimumAgentTurnSpeed;
    }

    public static Options createStandard() {
        return new Options(6, 2);
    }

    public int getMaxNumberOfPlayers() {
        return maxNumberOfPlayers;
    }

    public void setMaxNumberOfPlayers(int maxNumberOfPlayers) {
        this.maxNumberOfPlayers = maxNumberOfPlayers;
    }

    public int getMinimumAgentTurnSpeed() {
        return minimumAgentTurnSpeed;
    }

    public void setMinimumAgentTurnSpeed(int minimumAgentTurnSpeed) {
        this.minimumAgentTurnSpeed = minimumAgentTurnSpeed;
    }
}
