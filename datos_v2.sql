USE pedido_envio;

-- Limpiar
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE pedido;
TRUNCATE TABLE envio;
SET FOREIGN_KEY_CHECKS = 1;

-- Reiniciar IDs
ALTER TABLE envio AUTO_INCREMENT = 1;
ALTER TABLE pedido AUTO_INCREMENT = 1;

-- Insertar envíos
INSERT INTO envio (id, eliminado, tracking, empresa, tipo, costo, fecha_despacho, fecha_estimada, estado) VALUES
(1, FALSE, 'AND-2025-000001', 'ANDREANI', 'EXPRES', 1500.00, '2025-01-15', '2025-01-17', 'EN_TRANSITO'),
(2, FALSE, 'OCA-2025-000001', 'OCA', 'ESTANDAR', 800.00, '2025-01-14', '2025-01-20', 'ENTREGADO'),
(3, FALSE, 'ARG-2025-000001', 'CORREO_ARG', 'ESTANDAR', 600.00, '2025-01-16', '2025-01-22', 'EN_PREPARACION'),
(4, FALSE, 'ARG-2025-000002', 'CORREO_ARG', 'ESTANDAR', 600.00, '2025-01-16', '2025-01-22', 'ENTREGADO');

-- Insertar pedidos
INSERT INTO pedido (id, eliminado, numero, fecha, cliente_nombre, total, estado, envio_id) VALUES
(1, FALSE, 'PED-2025-0001', '2025-01-10', 'Juan Pérez', 25000.50, 'ENVIADO', 1),
(2, FALSE, 'PED-2025-0002', '2025-01-11', 'María González', 15500.00, 'ENVIADO', 2),
(3, FALSE, 'PED-2025-0003', '2025-01-12', 'Carlos Rodríguez', 8900.75, 'FACTURADO', 3),
(4, FALSE, 'PED-2025-0004', '2025-01-13', 'Ana Martínez', 12300.00, 'NUEVO', 4);

-- Verificar
SELECT 'ENVIOS:' as tabla, COUNT(*) as total FROM envio WHERE eliminado = FALSE
UNION ALL
SELECT 'PEDIDOS:' as tabla, COUNT(*) as total FROM pedido WHERE eliminado = FALSE;

-- Ver datos
SELECT * FROM envio WHERE eliminado = FALSE;
SELECT * FROM pedido WHERE eliminado = FALSE;