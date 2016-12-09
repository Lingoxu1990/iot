PRAGMA foreign_keys = false;

-- ----------------------------
--  Table structure for device_class
-- ----------------------------
DROP TABLE IF EXISTS "device_class";
CREATE TABLE "device_class" (
  "id" VARCHAR(64,0) NOT NULL,
  "device_type" VARCHAR(32,0),
  "main_param" VARCHAR(64,0),
  "main_param_device_on" VARCHAR(32,0),
  "main_param_device_value" VARCHAR(32,0),
  PRIMARY KEY("id")
);

INSERT INTO device_class VALUE ('00000001','bulb','illuminance','1','1')

-- ----------------------------
--  Table structure for device_standard
-- ----------------------------
DROP TABLE IF EXISTS "device_standard";
CREATE TABLE "device_standard" (
  "id" VARCHAR(64,0) NOT NULL,
  "device_class" VARCHAR(64,0),
  "standard" VARCHAR(64,0),
  PRIMARY KEY("id")
);

-- ----------------------------
--  Table structure for map_line
-- ----------------------------
DROP TABLE IF EXISTS "map_line";
CREATE TABLE "map_line" (
  "id" VARCHAR(64,0) NOT NULL,
  "param" VARCHAR(64,0),
  "sensor_value" integer(64,0),
  "device_value" integer(64,0),
  PRIMARY KEY("id")
);

-- ----------------------------
--  Table structure for private_recipe
-- ----------------------------
DROP TABLE IF EXISTS "private_recipe";
CREATE TABLE "private_recipe" (
  "id" VARCHAR(64,0) NOT NULL,
  "user_id" VARCHAR(64,0),
  "crop_name" VARCHAR(64,0),
  PRIMARY KEY("id")
);

-- ----------------------------
--  Table structure for private_recipe_data
-- ----------------------------
DROP TABLE IF EXISTS "private_recipe_data";
CREATE TABLE "private_recipe_data" (
  "id" VARCHAR(64,0) NOT NULL,
  "crop_name" VARCHAR(64,0),
  "day" VARCHAR(64,0),
  "start_time" VARCHAR(64,0),
  "end_time" VARCHAR(64,0),
  "channel_combination" VARCHAR(64,0),
  "air_temperature" VARCHAR(64,0),
  "air_humidity" VARCHAR(64,0),
  "soil_temperature" VARCHAR(64,0),
  "soil_PH_value" VARCHAR(64,0),
  "soil_humidity" VARCHAR(64,0),
  "carbon_dioxide" VARCHAR(64,0),
  "illuminance" VARCHAR(64,0),
  "soil_conductivity" VARCHAR(64,0),
  "photons" VARCHAR(64,0),
  "liquid_PH_value" VARCHAR(64,0),
  "lai_value" VARCHAR(64,0),
  "user_id" VARCHAR(64,0),
  PRIMARY KEY("id")
);

-- ----------------------------
--  Table structure for private_recipe_index
-- ----------------------------
DROP TABLE IF EXISTS "private_recipe_index";
CREATE TABLE "private_recipe_index" (
  "id" VARCHAR(64,0) NOT NULL,
  "user_id" VARCHAR(64,0),
  "region_guid" VARCHAR(64,0),
  "private_term_id" VARCHAR(64,0),
  "index" VARCHAR(64,0),
  "start_time" VARCHAR(64,0),
  PRIMARY KEY("id")
);

-- ----------------------------
--  Table structure for table_device
-- ----------------------------
DROP TABLE IF EXISTS "table_device";
CREATE TABLE "table_device" (
  "device_guid" VARCHAR,
  "gateway_id" VARCHAR NOT NULL,
  "device_addr" VARCHAR NOT NULL,
  "device_name" VARCHAR NOT NULL,
  "device_id" VARCHAR NOT NULL,
  "device_type" VARCHAR NOT NULL,
  "device_valid" VARCHAR NOT NULL,
  "device_switch" VARCHAR NOT NULL,
  "device_value" VARCHAR NOT NULL,
  "device_delay" VARCHAR NOT NULL,
  "device_register_type" VARCHAR NOT NULL,
  PRIMARY KEY("device_guid")
);

-- ----------------------------
--  Table structure for table_group
-- ----------------------------
DROP TABLE IF EXISTS "table_group";
CREATE TABLE "table_group" (
  "group_guid" VARCHAR,
  "gateway_id" VARCHAR NOT NULL,
  "group_addr" VARCHAR NOT NULL,
  "group_name" VARCHAR NOT NULL,
  "group_switch" VARCHAR NOT NULL,
  "group_value" VARCHAR NOT NULL,
  "group_delay" VARCHAR NOT NULL,
  PRIMARY KEY("group_guid")
);

-- ----------------------------
--  Table structure for table_group_members
-- ----------------------------
DROP TABLE IF EXISTS "table_group_members";
CREATE TABLE "table_group_members" (
  "group_members_guid" VARCHAR,
  "table_group_guid" VARCHAR NOT NULL,
  "group_addr" VARCHAR NOT NULL,
  "device_addr" VARCHAR NOT NULL,
  "device_guid" VARCHAR NOT NULL,
  "gateway_id" VARCHAR NOT NULL,
  "device_name" VARCHAR NOT NULL,
  PRIMARY KEY("group_members_guid")
);

-- ----------------------------
--  Table structure for table_region
-- ----------------------------
DROP TABLE IF EXISTS "table_region";
CREATE TABLE "table_region" (
  "region_guid" VARCHAR,
  "region_addr" VARCHAR NOT NULL,
  "region_name" VARCHAR NOT NULL,
  "region_switch" VARCHAR NOT NULL,
  "region_value" VARCHAR NOT NULL,
  "region_delay" VARCHAR NOT NULL,
  "gateway_id" VARCHAR NOT NULL,
  PRIMARY KEY("region_guid")
);

-- ----------------------------
--  Table structure for table_region_device
-- ----------------------------
DROP TABLE IF EXISTS "table_region_device";
CREATE TABLE "table_region_device" (
  "region_device_guid" VARCHAR,
  "region_guid" VARCHAR NOT NULL,
  "region_addr" VARCHAR NOT NULL,
  "region_name" VARCHAR NOT NULL,
  "table_device_guid" VARCHAR NOT NULL,
  "gateway_id" VARCHAR NOT NULL,
  "device_addr" VARCHAR NOT NULL,
  "device_name" VARCHAR NOT NULL,
  "channel_class" VARCHAR NOT NULL,
  "channel_guid" VARCHAR NOT NULL,
  "channel_name" VARCHAR NOT NULL,
  "channel_type" VARCHAR NOT NULL,
  "channel_bit_num" VARCHAR NOT NULL,
  PRIMARY KEY("region_device_guid")
);

-- ----------------------------
--  Table structure for table_region_group
-- ----------------------------
DROP TABLE IF EXISTS "table_region_group";
CREATE TABLE table_region_group
(
  region_group_guid	VARCHAR  PRIMARY KEY,
  region_guid		VARCHAR  NOT NULL,
  table_group_guid	VARCHAR  NOT NULL,
  gateway_id		VARCHAR  NOT NULL,
  group_addr		VARCHAR  NOT NULL,
  group_name		VARCHAR  NOT NULL

);

-- ----------------------------
--  Table structure for table_region_scene
-- ----------------------------
DROP TABLE IF EXISTS "table_region_scene";
CREATE TABLE table_region_scene
(
  region_scene_guid 	VARCHAR  PRIMARY KEY,
  region_guid		VARCHAR  NOT NULL,
  table_scene_guid	VARCHAR	 NOT NULL,
  gateway_id		VARCHAR  NOT NULL,
  scene_addr		VARCHAR  NOT NULL,
  scene_name		VARCHAR  NOT NULL

);

-- ----------------------------
--  Table structure for table_scene
-- ----------------------------
DROP TABLE IF EXISTS "table_scene";
CREATE TABLE table_scene
(
  scene_guid              VARCHAR  PRIMARY KEY,
  gateway_id              VARCHAR  NOT NULL,
  scene_addr    		VARCHAR  NOT NULL,
  scene_name    		VARCHAR  NOT NULL,
  scene_switch  		VARCHAR  NOT NULL
);

-- ----------------------------
--  Table structure for table_scene_members
-- ----------------------------
DROP TABLE IF EXISTS "table_scene_members";
CREATE TABLE table_scene_members
(
  scene_members_guid      VARCHAR  PRIMARY KEY,
  table_scene_guid        VARCHAR  NOT NULL,
  scene_addr              VARCHAR  NOT NULL,
  device_addr             VARCHAR  NOT NULL,
  device_value            VARCHAR  NOT NULL,
  device_delay            VARCHAR  NOT NULL,
  device_guid		VARCHAR  NOT NULL,
  gateway_id		VARCHAR  NOT NULL,
  device_name		VARCHAR  NOT NULL
);

-- ----------------------------
--  Indexes structure for table table_device
-- ----------------------------
CREATE INDEX "table_device_idx" ON table_device ("device_addr" ASC);

PRAGMA foreign_keys = true;
