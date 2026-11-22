-- D-018 · Bases de datos y usuario de la aplicación en un MySQL 8 local.
-- Uso (como root):  mysql -uroot -p < scripts/crear-bd.sql
-- Cambia 'CAMBIAR-CLAVE' por una contraseña propia y ponla en RADIO_DB_CLAVE (no la subas al repositorio).
CREATE DATABASE IF NOT EXISTS radiodiagnostico CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS radiodiagnostico_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'radiodiagnostico'@'localhost' IDENTIFIED BY 'CAMBIAR-CLAVE';
CREATE USER IF NOT EXISTS 'radiodiagnostico'@'127.0.0.1' IDENTIFIED BY 'CAMBIAR-CLAVE';
GRANT ALL PRIVILEGES ON radiodiagnostico.* TO 'radiodiagnostico'@'localhost', 'radiodiagnostico'@'127.0.0.1';
GRANT ALL PRIVILEGES ON radiodiagnostico_test.* TO 'radiodiagnostico'@'localhost', 'radiodiagnostico'@'127.0.0.1';
-- Las tablas las crea la aplicación al arrancar (una por almacén: pacientes, solicitudes, citas…).
