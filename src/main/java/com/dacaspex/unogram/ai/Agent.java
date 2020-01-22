package com.dacaspex.unogram.ai;

import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.game.UnoGame;

public abstract class Agent extends Player {
    public Agent(String id, String username) {
        super(id, username);
    }

    public abstract Move getMove(UnoGame game);
}
