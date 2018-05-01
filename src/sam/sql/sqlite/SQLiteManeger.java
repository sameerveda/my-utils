package sam.sql.sqlite;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import sam.sql.JDBCHelper;

public class SQLiteManeger extends JDBCHelper {
    private final Object path;

    public SQLiteManeger(String dbPath) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        this(Paths.get(dbPath), new Properties());
    }
    public SQLiteManeger(Path dbPath) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        this(dbPath, new Properties());
    }
    public SQLiteManeger(File dbPath) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        this(dbPath.toPath(), new Properties());
    }
    /**
     * 
     * @param dbPath can be a file, path, string, url
     * @param prop
     * @param createDefaultStatement
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public SQLiteManeger(Path dbPath, Properties prop) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        super(connection(dbPath, prop));
        // JDBC.class.getCanonicalName() =  "org.sqlite.JDBC"
        // JDBC.PREFIX = "jdbc:sqlite:"
       this.path = dbPath; 
    }
    private static Connection connection(Path dbPath, Properties prop) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Driver driver = (Driver) Class.forName("org.sqlite.JDBC").newInstance();
        Connection connection = driver.connect("jdbc:sqlite:"+dbPath, prop);
        connection.setAutoCommit(false);
        return connection;
    }
    public Object getPath() {
        return path;
    }
}
