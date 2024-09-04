package com.cometproject.server.game.catalog.types;

import com.cometproject.api.game.catalog.types.ICatalogOffer;

public record CatalogOffer(int offerId, int catalogPageId, int catalogItemId) implements ICatalogOffer {

}
