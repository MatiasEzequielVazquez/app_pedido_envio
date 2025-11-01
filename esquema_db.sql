DROP SCHEMA IF EXISTS db_pedido_envio;
CREATE SCHEMA db_pedido_envio;
USE db_pedido_envio;

DROP TABLE IF EXISTS PEDIDO;
DROP TABLE IF EXISTS ENVIO;

USE db_pedido_envio;

CREATE TABLE PEDIDO (
    id LONG AUTO_INCREMENT PRIMARY KEY,
    eliminado BOOL,
    numero VARCHAR(20) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cliente_nombre VARCHAR(120) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'Nuevo',
    envio_id LONG NOT NULL,
    CONSTRAINT fk_pedido_envio FOREIGN KEY (envio_id) 
        REFERENCES ENVIO(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
	CONSTRAINT uk_pedido_numero UNIQUE (numero),
    CONSTRAINT uk_pedido_id UNIQUE (id),
    CONSTRAINT chk_pedido_estado CHECK (
        estado_pedido IN ('Nuevo', 'Facturado', 'Enviado')
    ),
    CONSTRAINT chk_pedido_total CHECK (total >= 0)
);

CREATE TABLE ENVIO (
    id LONG AUTO_INCREMENT PRIMARY KEY,
    eliminado BOOL,
    tracking VARCHAR(40),
    empresa VARCHAR(10) NOT NULL,
    tipo VARCHAR(8) NOT NULL DEFAULT 'Estandar',
    costo DECIMAL(10,2) NOT NULL,
    fecha_despacho DATETIME,
    fecha_entrega DATETIME,
    estado VARCHAR(15) NOT NULL DEFAULT 'En Preparacion',
    CONSTRAINT uk_envio_tracking UNIQUE (tracking),
    CONSTRAINT uk_envio_id UNIQUE (id),
    CONSTRAINT chk_envio_estado CHECK (
        estado IN ('En Preparacion', 'En Transito', 'Entregado')
    ),
    CONSTRAINT chk_envio_costo CHECK (costo_envio >= 0),
    CONSTRAINT chk_envio_fechas CHECK (fecha_entrega >= fecha_despacho)
);