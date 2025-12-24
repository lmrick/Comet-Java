package com.cometproject.server.storage.queries.catalog;

import com.cometproject.api.game.catalog.types.pages.ICatalogFrontPageEntry;
import com.cometproject.api.game.catalog.types.items.ICatalogItem;
import com.cometproject.api.game.catalog.types.pages.ICatalogPage;
import com.cometproject.api.game.catalog.types.clothing.IClothingItem;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.game.catalog.types.CatalogFrontPageEntry;
import com.cometproject.server.game.catalog.types.CatalogItem;
import com.cometproject.server.game.catalog.types.CatalogPage;
import com.cometproject.server.game.catalog.types.ClothingItem;
import com.cometproject.server.storage.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CatalogDao {
    
    public static void getPages(Map<Integer, ICatalogPage> pages) {

        try(Connection sqlConnection = SQLUtility.getConnection();
            PreparedStatement preparedStatement = SQLUtility.prepare("SELECT * FROM catalog_pages WHERE visible = '1' ORDER BY order_num;", sqlConnection);
            ResultSet resultSet = preparedStatement.executeQuery()) {
           
            while (resultSet.next()) {
                try {
                    int pageId = resultSet.getInt("id");
                    pages.put(pageId, new CatalogPage(resultSet, CatalogManager.getInstance().getItemsForPage(pageId)));
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Comet.getServer().getLogger().warn("Failed to load catalog page: " + resultSet.getInt("id"));
                }
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        }
    }

    public static void getItems(Map<Integer, ICatalogItem> items) {
        try(Connection sqlConnection = SQLUtility.getConnection();
            PreparedStatement preparedStatement = SQLUtility.prepare("SELECT * FROM catalog_items", sqlConnection);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            
            while (resultSet.next()) {
                try {
                    final ICatalogItem catalogItem = itemFromResultSet(resultSet);

                    if (!catalogItem.getItemId().equals("-1") && catalogItem.getItems().size() == 0) {
                        Comet.getServer().getLogger().warn(String.format("Catalog Item with ID: %s and name: %s has invalid item data! (DataWrapper: %s)", catalogItem.getId(), catalogItem.getDisplayName(), catalogItem.getItemId()));
                        continue;
                    }

                    items.put(resultSet.getInt("id"), catalogItem);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Comet.getServer().getLogger().warn("Failed to load catalog item: " + resultSet.getString("id"));
                }
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        }
    }


    private static Map<Integer, ICatalogItem> getItemsByPage(int pageId) {
        Map<Integer, ICatalogItem> data = new HashMap<>();

        try(Connection sqlConnection = SQLUtility.getConnection();
            PreparedStatement preparedStatement = SQLUtility.prepare("SELECT * FROM catalog_items WHERE page_id = ?", sqlConnection);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            
            preparedStatement.setInt(1, pageId);

            while (resultSet.next()) {
                try {
                    final ICatalogItem catalogItem = itemFromResultSet(resultSet);

                    if (!catalogItem.getItemId().equals("-1") && catalogItem.getItems().size() == 0) {
                        Comet.getServer().getLogger().warn(String.format("Catalog Item with ID: %s and name: %s has invalid item data! (DataWrapper: %s)", catalogItem.getId(), catalogItem.getDisplayName(), catalogItem.getItemId()));
                        continue;
                    }

                    data.put(resultSet.getInt("id"), catalogItem);
                } catch (Exception e) {
                    Comet.getServer().getLogger().warn("Error while loading catalog item: " + resultSet.getInt("id"));
                }
            }

        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        }

        return data;
    }

    public static void updateLimitSellsForItem(int itemId, int amount) {
        try(Connection sqlConnection = SQLUtility.getConnection();
            PreparedStatement preparedStatement = SQLUtility.prepare("UPDATE catalog_items SET limited_sells = limited_sells + ? WHERE id = ?", sqlConnection)) {
            
            preparedStatement.setInt(1, amount);
            preparedStatement.setInt(2, itemId);

            SQLUtility.executeStatementSilently(preparedStatement, false);
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        } 
    }

    public static void loadGiftBoxes(List<Integer> giftBoxesOld, List<Integer> giftBoxesNew) {
        try(Connection sqlConnection = SQLUtility.getConnection();
            PreparedStatement preparedStatement = SQLUtility.prepare("SELECT * FROM catalog_gift_wrapping", sqlConnection);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                if (resultSet.getString("type").equals("old")) {
                    giftBoxesOld.add(resultSet.getInt("sprite_id"));
                } else {
                    giftBoxesNew.add(resultSet.getInt("sprite_id"));
                }
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        }
    }

    public static void getFeaturedPages(List<ICatalogFrontPageEntry> frontPageEntries) {
        try(Connection connection = SQLUtility.getConnection();
            PreparedStatement preparedStatement = SQLUtility.prepare("SELECT * FROM catalog_featured_pages", connection);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            
            while (resultSet.next()) {
                frontPageEntries.add(
                    new CatalogFrontPageEntry(resultSet.getInt("id"), 
                    resultSet.getString("caption"),
                        resultSet.getString("image"), 
                        resultSet.getString("page_link"), 
                        resultSet.getInt("page_id")));
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        }
    }

    public static void getClothing(Map<String, IClothingItem> clothingItems) {
        try(Connection sqlConnection = SQLUtility.getConnection();
            PreparedStatement preparedStatement = SQLUtility.prepare("SELECT * FROM catalog_clothing", sqlConnection);
            ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                final String itemsStr = resultSet.getString("clothing_items").replace(" ", "");

                if (itemsStr.equals("")) {
                    continue;
                }

                final String itemName = resultSet.getString("item_name");
                final String[] itemsStrArray = itemsStr.split(",");
                int[] items = new int[itemsStrArray.length];

                for (int i = 0; i < itemsStrArray.length; i++) {
                    items[i] = Integer.parseInt(itemsStrArray[i]);
                }

                clothingItems.put(itemName, new ClothingItem(itemName, items));
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        }
    }

    public static void saveRecentPurchase(final int playerId, final int catalogItem, final int amount, final String data) {
        try(Connection sqlConnection = SQLUtility.getConnection();
            PreparedStatement preparedStatement = SQLUtility.prepare("INSERT INTO `player_recent_purchases` (player_id, catalog_item, amount, data) VALUES(?, ?, ?, ?);", sqlConnection)) {
            
            preparedStatement.setInt(1, playerId);
            preparedStatement.setInt(2, catalogItem);
            preparedStatement.setInt(3, amount);
            preparedStatement.setString(4, data);

            preparedStatement.execute();
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        }
    }

    public static Set<Integer> findRecentPurchases(final int count, final int playerId) {
        final Set<Integer> recentPurchases = new HashSet<>();

        try(Connection connection = SQLUtility.getConnection();
            PreparedStatement preparedStatement = SQLUtility.prepare("SELECT DISTINCT `catalog_item` FROM player_recent_purchases WHERE player_id = ? LIMIT " + count, connection)) {
            
            preparedStatement.setInt(1, playerId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                final int catalogItemId = resultSet.getInt("catalog_item");
                recentPurchases.add(catalogItemId);
            }
        } catch (SQLException e) {
            SQLUtility.handleSqlException(e);
        }

        return recentPurchases;
    }

    private static ICatalogItem itemFromResultSet(ResultSet resultSet) throws SQLException {
        final int id = resultSet.getInt("id");
        final String itemIds = resultSet.getString("item_ids");
        final String catalogName = resultSet.getString("catalog_name");
        final int costCredits = resultSet.getInt("cost_credits");
        final int costPixels = resultSet.getInt("cost_pixels");
        final int costDiamonds = resultSet.getInt("cost_diamonds");
        final int costSeasonal = resultSet.getInt("cost_seasonal");
        final int amount = resultSet.getInt("amount");
        final boolean vip = resultSet.getBoolean("vip");
        final int limitedStack = resultSet.getInt("limited_stack");
        final int limitedSells = resultSet.getInt("limited_sells");
        final boolean offerActive = resultSet.getBoolean("offer_active");
        final String badgeId = resultSet.getString("badge_id");
        final String extraData = resultSet.getString("extradata");
        final int pageId = resultSet.getInt("page_id");

        return new CatalogItem(id, itemIds, catalogName, costCredits, costPixels,
                costDiamonds, costSeasonal, amount, vip, limitedStack,
                limitedSells, offerActive, badgeId, extraData, pageId);
    }

}
