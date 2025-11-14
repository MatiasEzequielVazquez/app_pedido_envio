package Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    // AJUSTA ESTOS VALORES SEGÚN TU CONFIGURACIÓN
    private static final String URL = "jdbc:mysql://localhost:3306/pedido_envio";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // Agregar la contraseña si existe
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL no encontrado", e);
        }
    }
}