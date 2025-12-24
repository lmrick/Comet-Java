package com.cometproject.server.game.catalog.types;

import com.cometproject.api.game.catalog.types.bundles.ICatalogBundledItem;

public record CatalogBundledItem(String presetData, int amount, int itemId) implements ICatalogBundledItem {

}