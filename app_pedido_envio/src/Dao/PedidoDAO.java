package Dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import Config.DatabaseConnection;
import Models.Pedido;
import Models.Envio;
import Models.EstadoPedido;
import Models.EmpresaEnvio;
import Models.TipoEnvio;
import Models.EstadoEnvio;

/**
 * DAO para la entidad Pedido.
 * Gestiona todas las operaciones CRUD de pedidos en la base de datos.
 * 
 * IMPORTANTE: Pedido tiene relación 1→1 con Envio.
 * Al leer un Pedido, se carga su Envio asociado mediante LEFT JOIN.
 */
public class PedidoDAO implements GenericDAO<Pedido> {
    
    private static final String INSERT_SQL = 
        "INSERT INTO PEDIDO (numero, fecha, cliente_nombre, total, estado, envio_id, eliminado) " +
        "VALUES (?, ?, ?, ?, ?, ?, FALSE)";
    
    private static final String UPDATE_SQL = 
        "UPDATE PEDIDO SET numero = ?, fecha = ?, cliente_nombre = ?, " +
        "total = ?, estado = ?, envio_id = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "UPDATE PEDIDO SET eliminado = TRUE WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT p.*, e.* FROM PEDIDO p " +
        "LEFT JOIN ENVIO e ON p.envio_id = e.id " +
        "WHERE p.id = ? AND p.eliminado = FALSE";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT p.*, e.* FROM PEDIDO p " +
        "LEFT JOIN ENVIO e ON p.envio_id = e.id " +
        "WHERE p.eliminado = FALSE";
    
    private static final String SELECT_BY_NUMERO_SQL = 
        "SELECT p.*, e.* FROM PEDIDO p " +
        "LEFT JOIN ENVIO e ON p.envio_id = e.id " +
        "WHERE p.numero = ? AND p.eliminado = FALSE";
    
    @Override
    public void insertar(Pedido pedido) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setPedidoParameters(stmt, pedido);
            stmt.executeUpdate();
            setGeneratedId(stmt, pedido);
        }
    }
    
    @Override
    public void insertTx(Pedido pedido, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setPedidoParameters(stmt, pedido);
            stmt.executeUpdate();
            setGeneratedId(stmt, pedido);
        }
    }
    
    @Override
    public void actualizar(Pedido pedido) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, pedido.getNumero());
            stmt.setDate(2, pedido.getFecha() != null ? Date.valueOf(pedido.getFecha()) : null);
            stmt.setString(3, pedido.getClienteNombre());
            stmt.setDouble(4, pedido.getTotal());
            stmt.setString(5, pedido.getEstado().name());
            
            // FK al envío (puede ser null)
            if (pedido.getEnvio() != null && pedido.getEnvio().getId() > 0) {
                stmt.setInt(6, pedido.getEnvio().getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            stmt.setInt(7, pedido.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se pudo actualizar el pedido con ID: " + pedido.getId());
            }
        }
    }
    
    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró pedido con ID: " + id);
            }
        }
    }
    
    @Override
    public Pedido getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPedidoConEnvio(rs);
                }
            }
        }
        return null;
    }
    
    @Override
    public List<Pedido> getAll() throws Exception {
        List<Pedido> pedidos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            
            while (rs.next()) {
                pedidos.add(mapearPedidoConEnvio(rs));
            }
        }
        
        return pedidos;
    }
    
    /**
     * Busca un pedido por su número (campo UNIQUE).
     * Incluye el envío asociado mediante LEFT JOIN.
     */
    public Pedido buscarPorNumero(String numero) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NUMERO_SQL)) {
            
            stmt.setString(1, numero);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearPedidoConEnvio(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Setea los parámetros de un pedido en el PreparedStatement.
     */
    private void setPedidoParameters(PreparedStatement stmt, Pedido pedido) throws SQLException {
        stmt.setString(1, pedido.getNumero());
        stmt.setDate(2, pedido.getFecha() != null ? Date.valueOf(pedido.getFecha()) : null);
        stmt.setString(3, pedido.getClienteNombre());
        stmt.setDouble(4, pedido.getTotal());
        stmt.setString(5, pedido.getEstado().name());
        
        // FK al envío (puede ser null)
        if (pedido.getEnvio() != null && pedido.getEnvio().getId() > 0) {
            stmt.setInt(6, pedido.getEnvio().getId());
        } else {
            stmt.setNull(6, Types.INTEGER);
        }
    }
    
    /**
     * Obtiene el ID autogenerado y lo asigna al pedido.
     */
    private void setGeneratedId(PreparedStatement stmt, Pedido pedido) throws SQLException {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                pedido.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("La inserción del pedido falló, no se obtuvo ID generado");
            }
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Pedido CON su Envio asociado.
     * 
     * Columnas del ResultSet (LEFT JOIN):
     * - p.*: todos los campos de PEDIDO
     * - e.*: todos los campos de ENVIO (pueden ser null)
     */
    private Pedido mapearPedidoConEnvio(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        
        // Mapear campos de PEDIDO
        pedido.setId(rs.getInt("p.id"));
        pedido.setNumero(rs.getString("p.numero"));
        
        Date fecha = rs.getDate("p.fecha");
        if (fecha != null) {
            pedido.setFecha(fecha.toLocalDate());
        }
        
        pedido.setClienteNombre(rs.getString("p.cliente_nombre"));
        pedido.setTotal(rs.getDouble("p.total"));
        pedido.setEstado(EstadoPedido.valueOf(rs.getString("p.estado")));
        pedido.setEliminado(rs.getBoolean("p.eliminado"));
        
        // Mapear ENVIO asociado (puede ser null)
        int envioId = rs.getInt("e.id");
        if (envioId > 0) {  // Si hay envío asociado
            Envio envio = new Envio();
            envio.setId(envioId);
            envio.setTracking(rs.getString("e.tracking"));
            envio.setEmpresa(EmpresaEnvio.valueOf(rs.getString("e.empresa")));
            envio.setTipo(TipoEnvio.valueOf(rs.getString("e.tipo")));
            envio.setCosto(rs.getDouble("e.costo"));
            
            Date fechaDespacho = rs.getDate("e.fecha_despacho");
            if (fechaDespacho != null) {
                envio.setFechaDespacho(fechaDespacho.toLocalDate());
            }
            
            Date fechaEstimada = rs.getDate("e.fecha_estimada");
            if (fechaEstimada != null) {
                envio.setFechaEstimada(fechaEstimada.toLocalDate());
            }
            
            envio.setEstado(EstadoEnvio.valueOf(rs.getString("e.estado")));
            envio.setEliminado(rs.getBoolean("e.eliminado"));
            
            pedido.setEnvio(envio);  // ⬅️ Asociar el envío al pedido
        }
        
        return pedido;
    }
}