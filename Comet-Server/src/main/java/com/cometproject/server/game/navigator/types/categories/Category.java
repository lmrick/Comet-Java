package com.cometproject.server.game.navigator.types.categories;

import com.cometproject.api.game.rooms.IRoomCategory;

public record Category(int id, String category, String categoryId, String publicName, boolean canDoActions, int colour,
                       int requiredRank, NavigatorViewMode viewMode, NavigatorCategoryType categoryType,
                       NavigatorSearchAllowance searchAllowance, int orderId, boolean visible, int roomCount, int roomCountExpanded) implements IRoomCategory {
    
    public Category(int id, String category, String categoryId, String publicName, boolean canDoActions, int colour, int requiredRank, NavigatorViewMode viewMode, String categoryType, String searchAllowance, int orderId, boolean visible, int roomCount, int roomCountExpanded) {
			this(id, category, categoryId, publicName, canDoActions, colour, requiredRank, viewMode, NavigatorCategoryType.valueOf(categoryType.toUpperCase()), NavigatorSearchAllowance.valueOf(searchAllowance.toUpperCase()), orderId, visible, roomCount, roomCountExpanded);
		}
    
}
