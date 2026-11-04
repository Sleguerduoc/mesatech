-- RDS Catálogo (instancia mesatech-rds-catalogo). Ejecutar con el usuario maestro desde la EC2 A.
-- El usuario de aplicación se crea con crear-usuario-app.sh (la contraseña no se guarda en el repositorio).
CREATE DATABASE IF NOT EXISTS mesatech_catalogo CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE mesatech_catalogo;

CREATE TABLE IF NOT EXISTS categorias (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(100) NOT NULL,
  descripcion  VARCHAR(500) NULL,
  activa       BOOLEAN      NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categorias_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS prioridades (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  nombre     VARCHAR(50) NOT NULL,
  nivel      INT         NOT NULL,
  sla_horas  INT         NOT NULL,
  activa     BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE KEY uk_prioridades_nombre (nombre)
) ENGINE=InnoDB;
