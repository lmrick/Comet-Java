package com.cometproject.server.game.catalog.types;

import com.cometproject.api.game.catalog.types.pages.ICatalogFrontPageEntry;

public record CatalogFrontPageEntry(int id, String caption, String image, String pageLink, int pageId) implements ICatalogFrontPageEntry {

}
