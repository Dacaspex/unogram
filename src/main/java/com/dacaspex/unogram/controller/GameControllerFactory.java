package com.dacaspex.unogram.controller;

import com.dacaspex.unogram.common.GameControllerStorage;
import com.dacaspex.unogram.common.IdGenerator;
import com.dacaspex.unogram.common.exception.CannotGenerateIdException;
import com.dacaspex.unogram.controller.announcements.Announcer;

public class GameControllerFactory {
    private static final int MAX_ATTEMPTS;

    static {
        MAX_ATTEMPTS = 10;
    }

    private final IdGenerator idGenerator;
    private final GameControllerStorage controllerStorage;

    public GameControllerFactory(IdGenerator idGenerator, GameControllerStorage controllerStorage) {
        this.idGenerator = idGenerator;
        this.controllerStorage = controllerStorage;
    }

    public GameController create(Announcer announcer) throws CannotGenerateIdException {
        // Try to generate an id. This id must be unique w.r.t. all the ids in the controller storage
        String id;
        int attempts = 0;
        do {
            if (attempts >= MAX_ATTEMPTS) {
                throw new CannotGenerateIdException();
            }

            id = idGenerator.generate(4);
            attempts++;
        } while (controllerStorage.get(id) != null);

        return new GameController(id, announcer);
    }
}
