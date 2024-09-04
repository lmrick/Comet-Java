package com.cometproject.server.game.catalog.types;

import com.cometproject.api.game.catalog.types.IClothingItem;

public record ClothingItem(String itemName, int[] parts) implements IClothingItem {

}
