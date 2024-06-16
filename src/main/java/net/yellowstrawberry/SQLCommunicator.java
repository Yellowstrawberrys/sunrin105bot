package net.yellowstrawberry;

import java.sql.*;

public class SQLCommunicator {

    public static SQLCommunicator INSTANCE = new SQLCommunicator("127.0.0.1", "user", "pass", "class119105");

    private Connection connection;
    private final String host;
    private final String user;
    private final String pass;
    private final String database;
    public SQLCommunicator(String host, String user, String pass, String database) {
        this.host = host;
        this.user = user;
        this.pass = pass;
        this.database = database;
        connect();
    }

    private boolean isReachable() throws SQLException {
        return !getConnection().isClosed()&&getConnection().isValid(1000);
    }

    /**
     * SQLCommunicator#executeN(String, Object...)
     * @param statement an SQL statement that may contain one or more '?' IN parameter placeholders
     * @param values values that can replace ? IN statement
     * */
    public void executeN(String statement, Object... values) {
        try (PreparedStatement stmt = getConnection().prepareStatement(statement)){
            for(int i = 0; i< values.length; i++) {
                stmt.setObject(1+i, values[i]);
            }
            stmt.execute();
        } catch (SQLException e) {
            try {
                if(isReachable()){
                    throw new RuntimeException(e);
                }else {
                    connect();
                    executeN(statement, values);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * SQLCommunicator#executeQueryN(String, Object...)
     * @param statement an SQL statement that may contain one or more '?' IN parameter placeholders
     * @param values values that can replace ? IN statement
     * @return a ResultSet object that contains the data produced by the query; never null
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or the SQL statement does not return a ResultSet object
     * */
    public ResultSet executeQueryN(String statement, Object... values) throws SQLException {
        try {
            PreparedStatement stmt = getConnection().prepareStatement(statement);
            for(int i = 0; i< values.length; i++) {
                stmt.setObject(1+i, values[i]);
            }
            return stmt.executeQuery();
        }catch (SQLException e) {
            if(isReachable()){
                throw e;
            }else {
                connect();
                return executeQueryN(statement, values);
            }
        }
    }

    /**
     * SQLCommunicator#executeUpdateN(String, Object...)
     * @param statement an SQL statement that may contain one or more '?' IN parameter placeholders
     * @param values values that can replace ? IN statement
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
     * */
    public int executeUpdateN(String statement, Object... values) {
        try (PreparedStatement stmt = getConnection().prepareStatement(statement)){
            for(int i = 0; i< values.length; i++) {
                stmt.setObject(1+i, values[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            try {
                if(isReachable()){
                    throw new RuntimeException(e);
                }else {
                    connect();
                    return executeUpdateN(statement, values);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void connect() {
        try {
            System.out.println("Making connection with the database...");
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mariadb://"+host+"/"+database+"?connectTimeout=0&socketTimeout=0&autoReconnect=true",
                    user,
                    pass
            );
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Success to make connection with the database!");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * SQLCommunicator#execute(String, Object...)
     * @param statement an SQL statement that may contain one or more '?' IN parameter placeholders
     * @param values values that can replace ? IN statement
     * */
    public static void execute(String statement, Object... values) {
        INSTANCE.executeN(statement, values);
    }

    /**
     * SQLCommunicator#executeQuery(String, Object...)
     * @param statement an SQL statement that may contain one or more '?' IN parameter placeholders
     * @param values values that can replace ? IN statement
     * @return a ResultSet object that contains the data produced by the query; never null
     * @throws SQLException if a database access error occurs; this method is called on a closed PreparedStatement or the SQL statement does not return a ResultSet object
     * */
    public static ResultSet executeQuery(String statement, Object... values) throws SQLException {
        return INSTANCE.executeQueryN(statement, values);
    }

    /**
     * SQLCommunicator#executeUpdate(String, Object...)
     * @param statement an SQL statement that may contain one or more '?' IN parameter placeholders
     * @param values values that can replace ? IN statement
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
     * */
    public static int executeUpdate(String statement, Object... values) {
        return INSTANCE.executeUpdateN(statement, values);
    }
}