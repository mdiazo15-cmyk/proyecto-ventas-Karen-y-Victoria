-- Esquema de la capa de persistencia (compatible con SQLite, MySQL y PostgreSQL).
-- Ejecutar una sola vez antes de correr el programa.

CREATE TABLE IF NOT EXISTS ventas (
    id_venta        VARCHAR(50)    PRIMARY KEY,      -- garantiza idempotencia
    fecha           VARCHAR(10)    NOT NULL,         -- formato ISO yyyy-MM-dd
    mes             VARCHAR(7)     NOT NULL,         -- yyyy-MM, facilita GROUP BY
    sucursal        VARCHAR(100)   NOT NULL,
    codigo_producto VARCHAR(50)    NOT NULL,
    producto        VARCHAR(150)   NOT NULL,
    cantidad        INT            NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(12,2)  NOT NULL CHECK (precio_unitario > 0),
    monto_total     DECIMAL(14,2)  NOT NULL,         -- cantidad * precio_unitario
    vendedor        VARCHAR(100)   NOT NULL,
    archivo_origen  VARCHAR(255)   NOT NULL
);

CREATE TABLE IF NOT EXISTS registros_rechazados (
    archivo_origen  VARCHAR(255)   NOT NULL,
    numero_registro INT            NOT NULL,
    tipo            VARCHAR(10)    NOT NULL,         -- INVALIDO o DUPLICADO
    id_venta        VARCHAR(50),                     -- puede venir vacío o nulo
    motivo          VARCHAR(500)   NOT NULL,
    PRIMARY KEY (archivo_origen, numero_registro)    -- evita repetir rechazos al reprocesar
);
