-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: db_ventas
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id_admin` int NOT NULL,
  PRIMARY KEY (`id_admin`),
  CONSTRAINT `fk_admin_usuario` FOREIGN KEY (`id_admin`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1);
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categoria`
--

DROP TABLE IF EXISTS `categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categoria` (
  `id_categoria` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id_categoria`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categoria`
--

LOCK TABLES `categoria` WRITE;
/*!40000 ALTER TABLE `categoria` DISABLE KEYS */;
INSERT INTO `categoria` VALUES (1,'Dulces'),(2,'Cítricas'),(3,'Tropicales');
/*!40000 ALTER TABLE `categoria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `id_cliente` int NOT NULL AUTO_INCREMENT,
  `id_tipo_documento` int NOT NULL,
  `nombre` varchar(45) DEFAULT NULL,
  `segundo_nombre` varchar(45) DEFAULT NULL,
  `apellido_pat` varchar(45) DEFAULT NULL,
  `apellido_mat` varchar(45) DEFAULT NULL,
  `razon_social` varchar(100) DEFAULT NULL,
  `domicilio_fiscal` varchar(100) DEFAULT NULL,
  `fecha_registro` datetime DEFAULT CURRENT_TIMESTAMP,
  `numero_documento` varchar(20) NOT NULL,
  PRIMARY KEY (`id_cliente`),
  UNIQUE KEY `uk_cliente_documento` (`id_tipo_documento`,`numero_documento`),
  KEY `fk_tipodoc` (`id_tipo_documento`),
  CONSTRAINT `fk_tipodoc` FOREIGN KEY (`id_tipo_documento`) REFERENCES `tipo_documento` (`id_tipo_documento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cliente`
--

LOCK TABLES `cliente` WRITE;
/*!40000 ALTER TABLE `cliente` DISABLE KEYS */;
/*!40000 ALTER TABLE `cliente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_nota_credito`
--

DROP TABLE IF EXISTS `detalle_nota_credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_nota_credito` (
  `id_detalle_nota` int NOT NULL AUTO_INCREMENT,
  `id_nota_credito` int NOT NULL,
  `id_producto` int NOT NULL,
  `cantidad` decimal(10,2) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_detalle_nota`),
  KEY `idx_dnc_nota_credito` (`id_nota_credito`),
  KEY `idx_dnc_producto` (`id_producto`),
  CONSTRAINT `fk_dnc_nota_credito` FOREIGN KEY (`id_nota_credito`) REFERENCES `nota_credito` (`id_nota_credito`),
  CONSTRAINT `fk_dnc_producto` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_nota_credito`
--

LOCK TABLES `detalle_nota_credito` WRITE;
/*!40000 ALTER TABLE `detalle_nota_credito` DISABLE KEYS */;
/*!40000 ALTER TABLE `detalle_nota_credito` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_venta`
--

DROP TABLE IF EXISTS `detalle_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalle_venta` (
  `id_detalle` int NOT NULL AUTO_INCREMENT,
  `cantidad` decimal(10,2) NOT NULL,
  `precio_fijo` decimal(10,2) NOT NULL,
  `id_venta` int DEFAULT NULL,
  `id_producto` int DEFAULT NULL,
  PRIMARY KEY (`id_detalle`),
  KEY `id_venta` (`id_venta`),
  KEY `id_producto` (`id_producto`),
  CONSTRAINT `detalle_venta_ibfk_1` FOREIGN KEY (`id_venta`) REFERENCES `venta` (`id_venta`),
  CONSTRAINT `detalle_venta_ibfk_2` FOREIGN KEY (`id_producto`) REFERENCES `producto` (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_venta`
--

LOCK TABLES `detalle_venta` WRITE;
/*!40000 ALTER TABLE `detalle_venta` DISABLE KEYS */;
/*!40000 ALTER TABLE `detalle_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `direcciones`
--

DROP TABLE IF EXISTS `direcciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `direcciones` (
  `id_direccion` int NOT NULL AUTO_INCREMENT,
  `departamento` varchar(50) NOT NULL,
  `provincia` varchar(50) NOT NULL,
  `distrito` varchar(50) NOT NULL,
  `calle` varchar(100) NOT NULL,
  `referencia` varchar(150) NOT NULL,
  `id_usuario` int DEFAULT NULL,
  PRIMARY KEY (`id_direccion`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `direcciones_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `direcciones`
--

LOCK TABLES `direcciones` WRITE;
/*!40000 ALTER TABLE `direcciones` DISABLE KEYS */;
INSERT INTO `direcciones` VALUES (9,'Lima','Lima','Los Ángeles','Calle Los Alamos 567','Cerca al estadio',1),(10,'Lima','Lima','Los Ángeles','Calle Los Alamos 567','Cerca al colegio',2),(11,'Lema','Lema','Comas','dadadasda','dasdada',3),(23,'dadawda','dadawda','dadawda','dadawda','dadawda',15),(24,'dadada','dadada','dadada','dadada','dadada',16),(25,'dadasdw','dadasdw','dadasdw','dadasdw','',17),(26,'dadavxcv','dadavxcv','dadavxcv','dadavxcv','',19),(27,'ldslkfdp','ldslkfdp','ldslkfdp','ldslkfdp','',20),(28,'adadategte','adadategte','adadategte','adadategte','',21),(29,'Lima','Lima','Comas','Lima','',22);
/*!40000 ALTER TABLE `direcciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `email_verification_token`
--

DROP TABLE IF EXISTS `email_verification_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_verification_token` (
  `id_token` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `token` varchar(255) NOT NULL,
  `expires_at` datetime NOT NULL,
  `is_used` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_token`),
  KEY `idx_token` (`token`),
  KEY `idx_usuario` (`id_usuario`),
  CONSTRAINT `fk_evt_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_verification_token`
--

LOCK TABLES `email_verification_token` WRITE;
/*!40000 ALTER TABLE `email_verification_token` DISABLE KEYS */;
INSERT INTO `email_verification_token` VALUES (3,15,'fFDW3X9NkXI0ulGLOni0','2026-05-30 18:06:34',1,'2026-05-29 18:06:34'),(4,16,'Tqsg-dOZVW4o5x256_fZ','2026-05-30 21:06:27',1,'2026-05-29 21:06:27'),(5,17,'MVuVc2B8VqhAvJQ8rh1M','2026-05-30 21:18:07',1,'2026-05-29 21:18:07'),(6,19,'hRRwVvKfK2JyVbLrai4Y','2026-05-30 21:23:33',1,'2026-05-29 21:23:33'),(7,20,'0xgZZNzxX7IrcCpZDl3s','2026-05-30 23:10:43',1,'2026-05-29 23:10:43'),(8,21,'xbBwRZsy5FjSpuYC2XFY','2026-05-31 07:41:38',1,'2026-05-30 07:41:38'),(9,22,'O2pFzZ5I3q-o7toGgQrU','2026-05-31 14:40:31',1,'2026-05-30 14:40:31');
/*!40000 ALTER TABLE `email_verification_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estado_venta`
--

DROP TABLE IF EXISTS `estado_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estado_venta` (
  `id_estado_venta` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(30) NOT NULL,
  PRIMARY KEY (`id_estado_venta`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estado_venta`
--

LOCK TABLES `estado_venta` WRITE;
/*!40000 ALTER TABLE `estado_venta` DISABLE KEYS */;
INSERT INTO `estado_venta` VALUES (8,'ANULADA'),(7,'COMPLETADA'),(6,'PAGADA'),(5,'PENDIENTE');
/*!40000 ALTER TABLE `estado_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `f_vencimiento`
--

DROP TABLE IF EXISTS `f_vencimiento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `f_vencimiento` (
  `id_fecha_vencimiento` int NOT NULL AUTO_INCREMENT,
  `fecha_vencimiento` date DEFAULT NULL,
  PRIMARY KEY (`id_fecha_vencimiento`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `f_vencimiento`
--

LOCK TABLES `f_vencimiento` WRITE;
/*!40000 ALTER TABLE `f_vencimiento` DISABLE KEYS */;
INSERT INTO `f_vencimiento` VALUES (1,'2026-07-29'),(2,'2026-06-15'),(3,'2026-06-10'),(4,'2026-06-20'),(5,'2026-06-25'),(6,'2026-07-05'),(7,'2026-06-30'),(8,'2026-07-15'),(9,'2026-07-10'),(10,'2026-07-20'),(11,'2026-07-08'),(12,'2026-07-25'),(13,'2026-06-28'),(14,'2026-07-12'),(15,'2026-07-18'),(16,'2026-06-22'),(17,'2026-07-28'),(18,'2026-06-18');
/*!40000 ALTER TABLE `f_vencimiento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `modulo`
--

DROP TABLE IF EXISTS `modulo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `modulo` (
  `id_modulo` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `ruta` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_modulo`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `modulo`
--

LOCK TABLES `modulo` WRITE;
/*!40000 ALTER TABLE `modulo` DISABLE KEYS */;
/*!40000 ALTER TABLE `modulo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos`
--

DROP TABLE IF EXISTS `movimientos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos` (
  `id_movimiento` int NOT NULL AUTO_INCREMENT,
  `id_venta` int NOT NULL,
  `id_estado_anterior` int DEFAULT NULL,
  `id_estado_nuevo` int NOT NULL,
  `fecha_movimiento` datetime DEFAULT CURRENT_TIMESTAMP,
  `id_usuario` int NOT NULL,
  `observacion` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `id_venta` (`id_venta`),
  KEY `id_estado_anterior` (`id_estado_anterior`),
  KEY `id_estado_nuevo` (`id_estado_nuevo`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `movimientos_ibfk_1` FOREIGN KEY (`id_venta`) REFERENCES `venta` (`id_venta`),
  CONSTRAINT `movimientos_ibfk_2` FOREIGN KEY (`id_estado_anterior`) REFERENCES `estado_venta` (`id_estado_venta`),
  CONSTRAINT `movimientos_ibfk_3` FOREIGN KEY (`id_estado_nuevo`) REFERENCES `estado_venta` (`id_estado_venta`),
  CONSTRAINT `movimientos_ibfk_4` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos`
--

LOCK TABLES `movimientos` WRITE;
/*!40000 ALTER TABLE `movimientos` DISABLE KEYS */;
/*!40000 ALTER TABLE `movimientos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nota_credito`
--

DROP TABLE IF EXISTS `nota_credito`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nota_credito` (
  `id_nota_credito` int NOT NULL AUTO_INCREMENT,
  `serie` varchar(4) NOT NULL DEFAULT 'NC01',
  `numero` int NOT NULL,
  `fecha_emision` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tipo` enum('TOTAL','PARCIAL') NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `motivo` varchar(200) NOT NULL,
  `id_venta_original` int NOT NULL,
  `id_usuario` int NOT NULL,
  `id_cliente` int DEFAULT NULL,
  PRIMARY KEY (`id_nota_credito`),
  UNIQUE KEY `uk_nota_credito_serie_numero` (`serie`,`numero`),
  KEY `idx_nc_venta_original` (`id_venta_original`),
  KEY `idx_nc_usuario` (`id_usuario`),
  KEY `idx_nc_cliente` (`id_cliente`),
  CONSTRAINT `fk_nc_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  CONSTRAINT `fk_nc_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `fk_nc_venta_original` FOREIGN KEY (`id_venta_original`) REFERENCES `venta` (`id_venta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nota_credito`
--

LOCK TABLES `nota_credito` WRITE;
/*!40000 ALTER TABLE `nota_credito` DISABLE KEYS */;
/*!40000 ALTER TABLE `nota_credito` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `origen_producto`
--

DROP TABLE IF EXISTS `origen_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `origen_producto` (
  `id_origen` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id_origen`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `origen_producto`
--

LOCK TABLES `origen_producto` WRITE;
/*!40000 ALTER TABLE `origen_producto` DISABLE KEYS */;
INSERT INTO `origen_producto` VALUES (1,'Nacional'),(2,'Importado');
/*!40000 ALTER TABLE `origen_producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `presentacion_producto`
--

DROP TABLE IF EXISTS `presentacion_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `presentacion_producto` (
  `id_presentacion` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id_presentacion`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `presentacion_producto`
--

LOCK TABLES `presentacion_producto` WRITE;
/*!40000 ALTER TABLE `presentacion_producto` DISABLE KEYS */;
INSERT INTO `presentacion_producto` VALUES (1,'Unidad'),(2,'Caja'),(3,'Kilogramo');
/*!40000 ALTER TABLE `presentacion_producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `producto` (
  `id_producto` int NOT NULL AUTO_INCREMENT,
  `precio_unitario` decimal(10,2) NOT NULL,
  `stock` int NOT NULL,
  `descripcion` varchar(250) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `id_categoria` int DEFAULT NULL,
  `id_origen` int DEFAULT NULL,
  `id_presentacion` int DEFAULT NULL,
  `id_fecha_vencimiento` int DEFAULT NULL,
  PRIMARY KEY (`id_producto`),
  KEY `id_categoria` (`id_categoria`),
  KEY `id_origen` (`id_origen`),
  KEY `id_presentacion` (`id_presentacion`),
  KEY `fk_fvencimiento` (`id_fecha_vencimiento`),
  CONSTRAINT `fk_fvencimiento` FOREIGN KEY (`id_fecha_vencimiento`) REFERENCES `f_vencimiento` (`id_fecha_vencimiento`),
  CONSTRAINT `producto_ibfk_1` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`),
  CONSTRAINT `producto_ibfk_2` FOREIGN KEY (`id_origen`) REFERENCES `origen_producto` (`id_origen`),
  CONSTRAINT `producto_ibfk_3` FOREIGN KEY (`id_presentacion`) REFERENCES `presentacion_producto` (`id_presentacion`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `producto`
--

LOCK TABLES `producto` WRITE;
/*!40000 ALTER TABLE `producto` DISABLE KEYS */;
INSERT INTO `producto` VALUES (1,5.50,100,'Manzana dulce de color rojo intenso','Manzana Roja',1,1,3,1),(2,6.00,80,'Manzana ácida crujiente','Manzana Verde',1,1,3,1),(3,12.00,50,'Fresas frescas y dulces','Fresa',1,1,2,2),(4,3.50,150,'Plátano de seda dulce','Plátano',1,1,3,3),(5,4.00,30,'Sandía jugosa y refrescante','Sandía',1,1,1,4),(6,5.00,40,'Melón dulce y aromático','Melón',1,1,1,5),(7,7.00,60,'Pera jugosa y dulce','Pera',1,1,3,6),(8,15.00,45,'Uva sin semilla crujiente','Uva Verde',1,2,3,7),(9,18.00,35,'Uva roja dulce importada','Uva Roja',1,2,3,7),(10,8.00,70,'Mango dulce de fibra corta','Mango',3,1,3,8),(11,6.50,55,'Papaya dulce y jugosa','Papaya',3,1,3,9),(12,7.50,40,'Piña dulce y refrescante','Piña',3,1,1,10),(13,10.00,35,'Maracuyá agridulce','Maracuyá',3,1,3,11),(14,4.50,120,'Naranja jugosa y refrescante','Naranja',2,1,3,12),(15,5.00,100,'Mandarina dulce fácil de pelar','Mandarina',2,1,3,13),(16,3.00,90,'Limón ácido para jugos','Limón',2,1,3,14),(17,6.00,50,'Toronja rosada refrescante','Toronja',2,1,1,15),(18,9.00,40,'Kiwi verde agridulce','Kiwi',3,2,1,16),(19,8.50,30,'Coco fresco con agua','Coco',3,1,1,17),(20,12.50,25,'Granada dulce y antioxidante','Granada',1,2,1,18);
/*!40000 ALTER TABLE `producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol`
--

DROP TABLE IF EXISTS `rol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol` (
  `id_rol` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `descripcion` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_rol`),
  UNIQUE KEY `nombre_UNIQUE` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol`
--

LOCK TABLES `rol` WRITE;
/*!40000 ALTER TABLE `rol` DISABLE KEYS */;
INSERT INTO `rol` VALUES (3,'ADMIN','Acceso total al sistema'),(4,'VENDEDOR','Solo ventas');
/*!40000 ALTER TABLE `rol` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rol_modulo`
--

DROP TABLE IF EXISTS `rol_modulo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rol_modulo` (
  `id_rol_modulo` int NOT NULL AUTO_INCREMENT,
  `id_rol` int NOT NULL,
  `id_modulo` int NOT NULL,
  PRIMARY KEY (`id_rol_modulo`),
  KEY `id_modulo_idx` (`id_modulo`),
  KEY `id_rol_idx` (`id_rol`),
  CONSTRAINT `fk_rol_modulo_modulo` FOREIGN KEY (`id_modulo`) REFERENCES `modulo` (`id_modulo`),
  CONSTRAINT `fk_rol_modulo_rol` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rol_modulo`
--

LOCK TABLES `rol_modulo` WRITE;
/*!40000 ALTER TABLE `rol_modulo` DISABLE KEYS */;
/*!40000 ALTER TABLE `rol_modulo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `telefonos`
--

DROP TABLE IF EXISTS `telefonos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `telefonos` (
  `id_telefono` int NOT NULL AUTO_INCREMENT,
  `numero` varchar(20) NOT NULL,
  `id_usuario` int DEFAULT NULL,
  PRIMARY KEY (`id_telefono`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `telefonos_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `telefonos`
--

LOCK TABLES `telefonos` WRITE;
/*!40000 ALTER TABLE `telefonos` DISABLE KEYS */;
INSERT INTO `telefonos` VALUES (11,'999999999',1),(12,'945163144',2),(13,'987654378',3),(25,'154785633',15),(26,'125478569',16),(27,'125653666',17),(28,'125478548',19),(29,'125452236',20),(30,'125478596',21),(31,'569854758',22);
/*!40000 ALTER TABLE `telefonos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tipo_documento`
--

DROP TABLE IF EXISTS `tipo_documento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tipo_documento` (
  `id_tipo_documento` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `abreviatura` varchar(20) NOT NULL,
  PRIMARY KEY (`id_tipo_documento`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tipo_documento`
--

LOCK TABLES `tipo_documento` WRITE;
/*!40000 ALTER TABLE `tipo_documento` DISABLE KEYS */;
INSERT INTO `tipo_documento` VALUES (3,'DOCUMENTO NACIONAL DE IDENTIDAD','DNI'),(4,'REGISTRO UNICO DE CONTRIBUYENTES','RUC');
/*!40000 ALTER TABLE `tipo_documento` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `id_tipo_documento` int NOT NULL,
  `id_rol` int NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `segundo_nombre` varchar(50) DEFAULT NULL,
  `apellido_pat` varchar(50) NOT NULL,
  `apellido_mat` varchar(50) NOT NULL,
  `correo` varchar(50) NOT NULL,
  `password` varchar(250) NOT NULL,
  `fecha_registro` date NOT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `email_verified` tinyint(1) NOT NULL DEFAULT '0',
  `email_verified_at` datetime DEFAULT NULL,
  `numero_documento` varchar(20) NOT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `correo` (`correo`),
  UNIQUE KEY `uk_usuario_documento` (`id_tipo_documento`,`numero_documento`),
  KEY `fk_usuario_tipo_documento1_idx` (`id_tipo_documento`),
  KEY `id_rol_idx` (`id_rol`),
  CONSTRAINT `fk_usuario_tipo_documento1` FOREIGN KEY (`id_tipo_documento`) REFERENCES `tipo_documento` (`id_tipo_documento`),
  CONSTRAINT `id_rol` FOREIGN KEY (`id_rol`) REFERENCES `rol` (`id_rol`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,3,3,'admin','admin','admin','Castro','admin@admin.com','$2a$10$jsijt489nH8lWOLYkz23Ge/hYz392d9iv./Ewq18cq/.7uGw35QZ.','2026-05-24',1,1,NULL,'78912342'),(2,3,4,'Daniel','Elias','Guerrero','Arango','daniel@daniel.com','$2a$10$.BZ9Swu6Fy8jlbpbDipePeYZD.FjV4f2YcLgRhP18FahC7hMiQRmW','2026-05-24',1,1,NULL,'78902345'),(3,3,3,'MELISAa','MARIO','JUAN','JUANA','dada@gmail.com','$2a$10$n95kRt05QznafRy5Tf0c0.sKHhV84AZYdLDgUY25LH4NIPnbypdFi','2026-05-24',0,1,NULL,'76547675'),(15,3,4,'dadawda','dadawda','dadawda','dadawda','sorebew294@hitzcart.com','$2a$10$K0oPgNHuBQiXAAPT/EITLuQRPBZLl5d4tJ/UPARxQLLEr7f4DRzDG','2026-05-29',1,1,NULL,'12256888'),(16,3,4,'dadada','dadada','dadada','dadada','ratahi5988@ifcoat.com','$2a$10$vrKzjgb/h0qODD8zwSStWuwi4BpQZIauqL4s.3AyXf/xK4tzZz8pW','2026-05-29',1,1,NULL,'12456325'),(17,3,4,'dadasdw','dadasdw','dadasdw','dadasdw','noxinin543@hitzcart.com','$2a$10$nmB923uTY2/dz5XFKCbcsOoOhnmVElOXIBaoR0Op1jXULmENOoLn2','2026-05-29',1,1,NULL,'12547856'),(19,3,4,'dadavxcv','dadavxcv','dadavxcv','dadavxcv','goyosa2125@ifcoat.com','$2a$10$mqolJERYpyIm6/Ftw9tDSORPkA4Mb7Fr7bq5o.FJ5hcxqnk7MwOE.','2026-05-29',1,1,'2026-05-29 21:23:58','12547854'),(20,3,4,'ldslkfdp','ldslkfdp','ldslkfdp','ldslkfdp','pehamel832@hitzcart.com','$2a$10$CuPV0QEatc80zHOhIEDf6eMkfL3v.99wz2VitVoGuVx3nFLxmUn9.','2026-05-29',1,1,'2026-05-29 23:11:00','12545223'),(21,3,4,'adadategte','adadategte','adadategte','adadategte','firax72022@ifcoat.com','$2a$10$2JKLwidg5DrXX8sbLlf.5.iAuoZIyeipUuZrM30jI1EOhJ5OYh9je','2026-05-30',1,1,'2026-05-30 07:41:47','12547859'),(22,3,4,'Daniela','Eliasa','Guerreroa','Arangoa','yeyovev542@ifcoat.com','$2a$10$dMm3HkPa8yCPwHmZaCEm7OP6kvkhn0.wBu88x1ruxWHeKSchmETMe','2026-05-30',0,1,'2026-05-30 14:41:20','56985475');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vendedor`
--

DROP TABLE IF EXISTS `vendedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendedor` (
  `id_vendedor` int NOT NULL,
  PRIMARY KEY (`id_vendedor`),
  CONSTRAINT `fk_vendedor_usuario` FOREIGN KEY (`id_vendedor`) REFERENCES `usuario` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vendedor`
--

LOCK TABLES `vendedor` WRITE;
/*!40000 ALTER TABLE `vendedor` DISABLE KEYS */;
INSERT INTO `vendedor` VALUES (15),(16),(17),(19),(20),(21),(22);
/*!40000 ALTER TABLE `vendedor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `venta`
--

DROP TABLE IF EXISTS `venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `venta` (
  `id_venta` int NOT NULL AUTO_INCREMENT,
  `serie` varchar(4) NOT NULL DEFAULT 'B001',
  `numero` int NOT NULL,
  `fecha_anulacion` datetime DEFAULT NULL,
  `motivo_anulacion` varchar(100) DEFAULT NULL,
  `total_pago` decimal(10,2) NOT NULL,
  `moneda` varchar(3) NOT NULL DEFAULT 'PEN',
  `condicion_pago` enum('CONTADO','CREDITO') NOT NULL DEFAULT 'CONTADO',
  `valor_venta` decimal(10,2) NOT NULL DEFAULT '0.00',
  `igv` decimal(10,2) NOT NULL DEFAULT '0.00',
  `tasa_igv` decimal(5,2) NOT NULL DEFAULT '18.00',
  `isc` decimal(10,2) NOT NULL DEFAULT '0.00',
  `otros_tributos` decimal(10,2) NOT NULL DEFAULT '0.00',
  `fecha_venta` datetime NOT NULL,
  `id_usuario` int NOT NULL,
  `tipo_comprobante` enum('BOLETA','FACTURA') NOT NULL,
  `id_cliente` int DEFAULT NULL,
  `id_estado_venta` int NOT NULL,
  PRIMARY KEY (`id_venta`),
  UNIQUE KEY `uk_venta_serie_numero` (`serie`,`numero`),
  KEY `id_usuario` (`id_usuario`),
  KEY `FK_idcliente` (`id_cliente`),
  KEY `id_estado_venta` (`id_estado_venta`),
  CONSTRAINT `FK_idcliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`),
  CONSTRAINT `venta_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `venta_ibfk_2` FOREIGN KEY (`id_estado_venta`) REFERENCES `estado_venta` (`id_estado_venta`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `venta`
--

LOCK TABLES `venta` WRITE;
/*!40000 ALTER TABLE `venta` DISABLE KEYS */;
/*!40000 ALTER TABLE `venta` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-30 16:12:36
