package com.dacaspex.unogram.ai;

import com.dacaspex.unogram.common.IdGenerator;
import com.dacaspex.unogram.common.PlayerStorage;
import com.dacaspex.unogram.common.exception.CannotGenerateIdException;

public class AgentFactory {
    private static final int MAX_ATTEMPTS;

    static {
        MAX_ATTEMPTS = 10;
    }

    private final AgentNameFactory nameFactory;
    private final IdGenerator idGenerator;
    private final PlayerStorage playerStorage;

    public AgentFactory(IdGenerator idGenerator, PlayerStorage playerStorage) {
        this.nameFactory = new AgentNameFactory();
        this.idGenerator = idGenerator;
        this.playerStorage = playerStorage;
    }

    public Agent create() throws CannotGenerateIdException {
        // TODO: Generate different agent based on difficulty level

        // Try to generate an id. This id must be unique w.r.t. all the ids in the player storage
        String id;
        int attempts = 0;
        do {
            if (attempts >= MAX_ATTEMPTS) {
                throw new CannotGenerateIdException();
            }

            id = idGenerator.generate("agent/", 10);
            attempts++;
        } while (playerStorage.get(id) != null);

        return new RandomAgent(id, nameFactory.create());
    }
}
