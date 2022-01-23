package ru.itsjava.dao;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.domain.User;
import ru.itsjava.domain.UserIncorrectEntered;
import ru.itsjava.domain.UserNotFoundException;
import ru.itsjava.utils.Props;

import java.sql.*;

@AllArgsConstructor
public class UserDaoImpl implements UserDao {
    private final Props props;

    @Override
    public User findByNameAndPassword(String name, String password) {
        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password"));
        ) {
            PreparedStatement preparedStatement = connection
                    .prepareStatement("select count(*) cnt from schema_online_course.users where name = ? and password = ?;");

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            int userCount = resultSet.getInt("cnt");

            if (userCount == 1) { // клиент есть в БД
                return new User(name, password);
            } else if (userCount == 0){ // клиента нет в БД
                return null;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        throw new UserNotFoundException("Пользователь с таким именем и паролем не найден в БД!");
    }

    @SneakyThrows
    @Override
    public User createNewUser(String newName, String newPassword) {
        // создаем Connection по данным файла application.properties
        try (Connection connection = DriverManager.getConnection(
                props.getValue("db.url"),
                props.getValue("db.login"),
                props.getValue("db.password")
        )) {
            // если пользователя нет в БД, то создаем нового пользователя и вносим в БД
            PreparedStatement preparedStatementNewUser = connection.prepareStatement("insert into schema_online_course.users (name, password) values (?, ?)");
            preparedStatementNewUser.setString(1, newName);
            preparedStatementNewUser.setString(2, newPassword);

            preparedStatementNewUser.executeUpdate();

            return new User(newName, newPassword);

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        throw new UserIncorrectEntered("Вы вели что-то не то!");
    }
}
