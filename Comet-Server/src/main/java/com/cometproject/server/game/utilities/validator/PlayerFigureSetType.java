package com.cometproject.server.game.utilities.validator;

import java.util.Map;

public record PlayerFigureSetType(String typeName, int paletteId, Map<Integer, PlayerFigureSet> sets) {

}
