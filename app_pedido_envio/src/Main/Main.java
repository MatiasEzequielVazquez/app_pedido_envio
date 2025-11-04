package Main;

import Config.DatabaseConnection;
import Service.PedidoServiceImpl;
import Service.EnvioServiceImpl;
import Dao.PedidoDAO;
import Dao.EnvioDAO;
import Models.*;

import java.time.LocalDate;

public class TestPedido {
    public static void main(String[] args) {
        try {
            // Crear servicios
            EnvioDAO envioDAO = new EnvioDAO();
            PedidoDAO pedidoDAO = new PedidoDAO();
            EnvioServiceImpl envioService = new EnvioServiceImpl(envioDAO);
            PedidoServiceImpl pedidoService = new PedidoServiceImpl(pedidoDAO, envioService);
            
            // Crear un envío
            Envio envio = new Envio();
            envio.setTracking("TEST-001");
            envio.setEmpresa(EmpresaEnvio.ANDREANI);
            envio.setTipo(TipoEnvio.ESTANDAR);
            envio.setCosto(500.0);
            envio.setEstado(EstadoEnvio.EN_PREPARACION);
            
            // Crear un pedido
            Pedido pedido = new Pedido();
            pedido.setNumero("PED-TEST-001");
            pedido.setFecha(LocalDate.now());
            pedido.setClienteNombre("Juan Pérez");
            pedido.setTotal(15000.0);
            pedido.setEstado(EstadoPedido.NUEVO);
            pedido.setEnvio(envio);
            
            // Insertar con transacción
            pedidoService.crearPedidoConEnvio(pedido);
            
            System.out.println("✅ Pedido creado: " + pedido);
            
            // Listar todos
            System.out.println("\n📋 Todos los pedidos:");
            pedidoService.getAll().forEach(System.out::println);
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}