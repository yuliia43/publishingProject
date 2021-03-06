package daos.repositories;

import enums.Category;
import enums.Periodicity;
import factories.CategoryFactory;
import factories.PeriodicityFactory;
import models.Edition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuliia Shcherbakova ON 19.07.2019
 * @project publishing
 */
public class EditionRepository implements Repository<Edition> {
    /**
     * @param item
     */
    @Override
    public void add(Edition item) throws SQLException {
        String sqlAdd = "INSERT INTO Editions(edition_title, category, periodicity, `description`, price) " +
                "VALUES (?, ?, ?, ?, ?);";
        Connection connection = receiveConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sqlAdd);
        preparedStatement.setString(1, item.getEditionTitle());
        preparedStatement.setString(2, item.getCategory().toLowerCase());
        preparedStatement.setString(3, item.getPeriodicity().toLowerCase());
        preparedStatement.setString(4, item.getDescription());
        preparedStatement.setDouble(5, item.getPrice());
        preparedStatement.execute();
        connection.close();
    }

    /**
     * @param id
     */
    @Override
    public void delete(int id) throws SQLException {
        String sqlDelete = "DELETE FROM  Editions WHERE edition_id = ?;";
        Connection connection = receiveConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sqlDelete);
        preparedStatement.setInt(1, id);
        preparedStatement.execute();
        connection.close();
    }

    /**
     * @param item
     */
    @Override
    public void update(Edition item) throws SQLException {
        String sqlUpdate = "UPDATE Editions " +
                "SET edition_title = ?, category = ?, periodicity = ?, `description` = ?, price = ? " +
                "WHERE edition_id = ?;";
        Connection connection = receiveConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdate);
        preparedStatement.setString(1, item.getEditionTitle());
        preparedStatement.setString(2, item.getCategory().toString().toLowerCase());
        preparedStatement.setString(3, item.getPeriodicity().toString().toLowerCase());
        preparedStatement.setString(4, item.getDescription());
        preparedStatement.setDouble(5, item.getPrice());
        preparedStatement.setInt(6, item.getEditionId());
        preparedStatement.execute();
        connection.close();
    }

    /**
     * @return
     */
    @Override
    public List<Edition> getAll() throws SQLException {
        String sqlSelect = "SELECT * FROM Editions";
        return query(sqlSelect);
    }

    /**
     * @param id
     * @return
     */
    @Override
    public Edition getOneById(int id) throws SQLException {
        String sqlSelect = "SELECT * FROM Editions WHERE edition_id = ?;";
        List<Edition> items = searchForItemsUsingId(id, sqlSelect);
        if (items.size() != 0) return items.get(0);
        else return getDeletedEditionOneById(id);
    }

    private List<Edition> searchForItemsUsingId(int id, String sqlScript) throws SQLException {
        Connection connection = receiveConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sqlScript);
        preparedStatement.setInt(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Edition> items = getItems(resultSet);
        connection.close();
        return items;
    }

    /**
     * @param id
     * @return
     */
    public Edition getDeletedEditionOneById(int id) throws SQLException {
        String sqlSelect = "SELECT * FROM `deleted editions` WHERE edition_id = ?;";
        List<Edition> items = searchForItemsUsingId(id, sqlSelect);
        if (items.size() != 0) {
            Edition edition = items.get(0);
            edition.setDeleted(true);
            return edition;
        }
        else return null;
    }

    /**
     * @param resultSet
     * @return
     * @throws SQLException
     */
    @Override
    public List<Edition> getItems(ResultSet resultSet) throws SQLException {
        List<Edition> editions = new ArrayList<>();
        while (resultSet.next()) {
            int editionId = resultSet.getInt(1);
            String editionTitle = resultSet.getString(2);
            Category category = CategoryFactory.getCategory(resultSet.getString(3));
            Periodicity periodicity = PeriodicityFactory
                    .getPeriodicity(resultSet.getString(4));
            String description = resultSet.getString(5);
            double price = resultSet.getDouble(6);

            Edition edition = new Edition();
            edition.setEditionId(editionId);
            edition.setEditionTitle(editionTitle);
            edition.setCategory(category);
            edition.setPeriodicity(periodicity);
            edition.setDescription(description);
            edition.setPrice(price);

            editions.add(edition);
        }
        return editions;
    }

    /**
     * @param userId
     * @return
     * @throws SQLException
     */
    public List<Edition> getAllUnsubscribedEditions(int userId) throws SQLException {
        String sqlSelect = "SELECT * FROM editions " +
                "WHERE edition_id NOT IN " +
                "(SELECT edition_id FROM subscriptions WHERE user_id = ? AND expire_date > CURDATE());";
        List<Edition> items = searchForItemsUsingId(userId, sqlSelect);
        return items;
    }


}
