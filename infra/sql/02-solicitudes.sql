-- RDS Solicitudes (instancia mesatech-rds-solicitudes). Ejecutar con el usuario maestro desde la EC2 B.
CREATE DATABASE IF NOT EXISTS mesatech_solicitudes CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mesatech_solicitudes;

CREATE TABLE IF NOT EXISTS solicitudes (
  id                   BIGINT        NOT NULL AUTO_INCREMENT,
  titulo               VARCHAR(150)  NOT NULL,
  descripcion          VARCHAR(2000) NOT NULL,
  categoria_id         BIGINT        NOT NULL,
  prioridad_id         BIGINT        NOT NULL,
  solicitante_oid      VARCHAR(100)  NOT NULL,
  solicitante_email    VARCHAR(200)  NULL,
  estado               VARCHAR(20)   NOT NULL,
  fecha_creacion       DATETIME(6)   NOT NULL,
  fecha_actualizacion  DATETIME(6)   NULL,
  operador_asignado    VARCHAR(200)  NULL,
  detalle_atencion     VARCHAR(4000) NULL,
  PRIMARY KEY (id),
  KEY ix_solicitudes_solicitante (solicitante_oid)
) ENGINE=InnoDB;

-- Copia local de categorías recibidas por RabbitMQ. La clave es el categoriaId original de Catálogo.
CREATE TABLE IF NOT EXISTS categorias_recibidas (
  categoria_id     BIGINT       NOT NULL,
  nombre           VARCHAR(100) NOT NULL,
  event_id         VARCHAR(36)  NOT NULL,
  fecha_evento     VARCHAR(40)  NOT NULL,
  fecha_recepcion  DATETIME(6)  NOT NULL,
  PRIMARY KEY (categoria_id)
) ENGINE=InnoDB;
