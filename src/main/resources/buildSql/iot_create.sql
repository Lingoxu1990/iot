

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
--  Table structure for `account_data_info`
-- ----------------------------
DROP TABLE IF EXISTS `account_data_info`;
CREATE TABLE `account_data_info` (
  `account_id` varchar(64) NOT NULL,
  `basis_data_file_last_modified` varchar(64) DEFAULT NULL,
  `record_data_file_last_modified` varchar(64) DEFAULT NULL,
  `table_dcgs_record` varchar(64) DEFAULT NULL,
  `table_control_record` varchar(64) DEFAULT NULL,
  `table_sensor_record` varchar(64) DEFAULT NULL,
  `gateway_id` varchar(64) DEFAULT '',
  `id` varchar(64) NOT NULL,
  `region_addr` varchar(64) DEFAULT '9000',
  `group_addr` varchar(64) DEFAULT '1000',
  `sence_adde` varchar(64) DEFAULT 'a009',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `app_info`
-- ----------------------------
DROP TABLE IF EXISTS `app_info`;
CREATE TABLE `app_info` (
  `app_name` varchar(64) DEFAULT NULL,
  `app_type` varchar(64) DEFAULT NULL,
  `app_version` varchar(64) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `device_class`
-- ----------------------------
DROP TABLE IF EXISTS `device_class`;
CREATE TABLE `device_class` (
  `id` varchar(64) NOT NULL,
  `device_type` varchar(64) DEFAULT NULL,
  `main_param` varchar(64) DEFAULT NULL,
  `main_param_device_on` varchar(8) DEFAULT NULL,
  `main_param_device_value` varchar(8) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `device_standard`
-- ----------------------------
DROP TABLE IF EXISTS `device_standard`;
CREATE TABLE `device_standard` (
  `id` varchar(64) NOT NULL,
  `device_class_id` varchar(64) DEFAULT NULL,
  `standard` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `dispatcher_location`
-- ----------------------------
DROP TABLE IF EXISTS `dispatcher_location`;
CREATE TABLE `dispatcher_location` (
  `id` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  `dispatcher_gateway` varchar(64) DEFAULT NULL,
  `sub_gateway` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `private_recipe`
-- ----------------------------
DROP TABLE IF EXISTS `private_recipe`;
CREATE TABLE `private_recipe` (
  `private_recipe_id` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  `crop_name` varchar(64) DEFAULT NULL,
  `create_time` varchar(64) NOT NULL,
  PRIMARY KEY (`private_recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `private_recipe_data`
-- ----------------------------
DROP TABLE IF EXISTS `private_recipe_data`;
CREATE TABLE `private_recipe_data` (
  `id` varchar(64) NOT NULL,
  `private_recipe_id` varchar(64) DEFAULT NULL,
  `crop_name` varchar(64) DEFAULT NULL,
  `day` varchar(64) DEFAULT NULL,
  `start_time` int(11) DEFAULT NULL,
  `end_time` varchar(64) DEFAULT NULL,
  `channel_combination` varchar(64) DEFAULT NULL,
  `reserve03` varchar(64) DEFAULT NULL,
  `reserve04` varchar(64) DEFAULT NULL,
  `reserve05` varchar(64) NOT NULL,
  `reserve06` varchar(64) DEFAULT NULL,
  `reserve07` varchar(64) DEFAULT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  `substrate_PH_start` varchar(64) DEFAULT NULL,
  `substrate_PH_end` varchar(64) DEFAULT NULL,
  `substrate_Conductivity_start` varchar(64) DEFAULT NULL,
  `substrate_Conductivity_end` varchar(64) DEFAULT NULL,
  `substrate_Temperature_start` varchar(64) DEFAULT NULL,
  `substrate_Temperature_end` varchar(64) DEFAULT NULL,
  `ppfd_start` varchar(64) DEFAULT NULL,
  `ppfd_end` varchar(64) DEFAULT NULL,
  `liquid_PH_start` varchar(64) DEFAULT NULL,
  `liquid_PH_end` varchar(64) DEFAULT NULL,
  `substrate_Humidity_start` varchar(64) DEFAULT NULL,
  `substrate_Humidity_end` varchar(64) DEFAULT NULL,
  `liquid_DOC_start` varchar(64) DEFAULT NULL,
  `liquid_DOC_end` varchar(64) DEFAULT NULL,
  `liquid_Conductivity_start` varchar(64) DEFAULT NULL,
  `liquid_Conductivity_end` varchar(64) DEFAULT NULL,
  `substrate_DOC_start` varchar(64) DEFAULT NULL,
  `substrate_DOC_end` varchar(64) DEFAULT NULL,
  `lai_start` varchar(64) DEFAULT NULL,
  `lai_end` varchar(64) DEFAULT NULL,
  `carbon_Dioxide_start` varchar(64) DEFAULT NULL,
  `carbon_Dioxide_end` varchar(64) DEFAULT NULL,
  `illuminance_start` varchar(64) DEFAULT NULL,
  `illuminance_end` varchar(64) DEFAULT NULL,
  `air_Temperature_start` varchar(64) DEFAULT NULL,
  `air_Temperature_end` varchar(64) DEFAULT NULL,
  `air_Humidity_start` varchar(64) DEFAULT NULL,
  `air_Humidity_end` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ----------------------------
--  Table structure for `private_recipe_index`
-- ----------------------------
DROP TABLE IF EXISTS `private_recipe_index`;
CREATE TABLE `private_recipe_index` (
  `id` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  `region_guid` varchar(64) DEFAULT NULL,
  `private_recipe_id` varchar(64) DEFAULT NULL,
  `seq` varchar(64) DEFAULT NULL,
  `start_time` varchar(64) DEFAULT NULL,
  `status` varchar(64) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `public_recipe`
-- ----------------------------
DROP TABLE IF EXISTS `public_recipe`;
CREATE TABLE `public_recipe` (
  `public_recipe_id` varchar(64) DEFAULT NULL,
  `crop_name` varchar(64) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `public_recipe_data`
-- ----------------------------
DROP TABLE IF EXISTS `public_recipe_data`;
CREATE TABLE `public_recipe_data` (
  `id` varchar(64) NOT NULL,
  `public_recipe_id` varchar(64) DEFAULT NULL,
  `day` varchar(64) DEFAULT NULL,
  `start_time` varchar(64) DEFAULT NULL,
  `end_time` varchar(64) DEFAULT NULL,
  `channel_combination` varchar(64) DEFAULT NULL,
  `reserve03` varchar(64) DEFAULT NULL,
  `reserve04` varchar(64) DEFAULT NULL,
  `reserve05` varchar(64) DEFAULT NULL,
  `reserve06` varchar(64) DEFAULT NULL,
  `reserve07` varchar(64) DEFAULT NULL,
  `create_time` varchar(64) DEFAULT NULL,
  `substrate_PH_start` varchar(64) DEFAULT NULL,
  `substrate_PH_end` varchar(64) DEFAULT NULL,
  `substrate_Conductivity_start` varchar(64) DEFAULT NULL,
  `substrate_Conductivity_end` varchar(64) DEFAULT NULL,
  `substrate_Temperature_start` varchar(64) DEFAULT NULL,
  `substrate_Temperature_end` varchar(64) DEFAULT NULL,
  `ppfd_start` varchar(64) DEFAULT NULL,
  `ppfd_end` varchar(64) DEFAULT NULL,
  `liquid_PH_start` varchar(64) DEFAULT NULL,
  `liquid_PH_end` varchar(64) DEFAULT NULL,
  `substrate_Humidity_start` varchar(64) DEFAULT NULL,
  `substrate_Humidity_end` varchar(64) DEFAULT NULL,
  `liquid_DOC_start` varchar(64) DEFAULT NULL,
  `liquid_DOC_end` varchar(64) DEFAULT NULL,
  `liquid_Conductivity_start` varchar(64) DEFAULT NULL,
  `liquid_Conductivity_end` varchar(64) DEFAULT NULL,
  `substrate_DOC_start` varchar(64) DEFAULT NULL,
  `substrate_DOC_end` varchar(64) DEFAULT NULL,
  `lai_start` varchar(64) DEFAULT NULL,
  `lai_end` varchar(64) DEFAULT NULL,
  `carbon_Dioxide_start` varchar(64) DEFAULT NULL,
  `carbon_Dioxide_end` varchar(64) DEFAULT NULL,
  `illuminance_start` varchar(64) DEFAULT NULL,
  `illuminance_end` varchar(64) DEFAULT NULL,
  `air_Temperature_start` varchar(64) DEFAULT NULL,
  `air_Temperature_end` varchar(64) DEFAULT NULL,
  `air_Humidity_start` varchar(64) DEFAULT NULL,
  `air_Humidity_end` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_cdts_list`
-- ----------------------------
DROP TABLE IF EXISTS `table_cdts_list`;
CREATE TABLE `table_cdts_list` (
  `id` varchar(64) NOT NULL,
  `cdts_list_guid` varchar(64) NOT NULL,
  `cdts_name` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_channel`
-- ----------------------------
DROP TABLE IF EXISTS `table_channel`;
CREATE TABLE `table_channel` (
  `id` varchar(64) NOT NULL,
  `channel_guid` varchar(64) NOT NULL,
  `table_device_guid` varchar(64) NOT NULL,
  `device_name` varchar(64) NOT NULL,
  `channel_class` varchar(64) NOT NULL,
  `channel_number` varchar(64) NOT NULL,
  `channel_name` varchar(64) NOT NULL,
  `channel_type` varchar(64) NOT NULL,
  `channel_bit_num` varchar(64) NOT NULL,
  `channel_in_out` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  `gateway_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_check`
-- ----------------------------
DROP TABLE IF EXISTS `table_check`;
CREATE TABLE `table_check` (
  `id` int(4) NOT NULL,
  `checks` char(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_conditions`
-- ----------------------------
DROP TABLE IF EXISTS `table_conditions`;
CREATE TABLE `table_conditions` (
  `conditions_guid` varchar(64) NOT NULL,
  `cdts_list_guid` varchar(64) NOT NULL,
  `table_device_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `channel_bit_num` varchar(64) NOT NULL,
  `compare_val` varchar(64) NOT NULL,
  `offset_val` varchar(64) NOT NULL,
  PRIMARY KEY (`conditions_guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_conditons`
-- ----------------------------
DROP TABLE IF EXISTS `table_conditons`;
CREATE TABLE `table_conditons` (
  `id` varchar(64) NOT NULL,
  `conditions_guid` varchar(64) NOT NULL,
  `cdts_list_guid` varchar(64) NOT NULL,
  `table_device_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `channel_bit_num` varchar(64) NOT NULL,
  `compare_val` varchar(64) NOT NULL,
  `offset_val` varchar(64) NOT NULL,
  `account_id` varchar(64) NOT NULL,
  `channel_class` varchar(64) NOT NULL,
  `channel_type` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_control`
-- ----------------------------
DROP TABLE IF EXISTS `table_control`;
CREATE TABLE `table_control` (
  `id` varchar(64) NOT NULL,
  `control_guid` varchar(64) NOT NULL,
  `ctrl_sqn_guid` varchar(64) NOT NULL,
  `main_table_name` varchar(64) NOT NULL,
  `dcgs_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `m_address` varchar(64) NOT NULL,
  `channel_bit_num` varchar(64) NOT NULL,
  `m_value` varchar(64) NOT NULL,
  `m_delay` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_control_record`
-- ----------------------------
DROP TABLE IF EXISTS `table_control_record`;
CREATE TABLE `table_control_record` (
  `id` varchar(64) NOT NULL,
  `record_guid` varchar(64) NOT NULL,
  `table_main_guid` varchar(64) NOT NULL,
  `table_main_name` varchar(64) NOT NULL,
  `record_time` varchar(64) NOT NULL,
  `switch_value` varchar(64) NOT NULL,
  `delay` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_ctrl_sequence`
-- ----------------------------
DROP TABLE IF EXISTS `table_ctrl_sequence`;
CREATE TABLE `table_ctrl_sequence` (
  `id` varchar(64) NOT NULL,
  `ctrl_sqn_guid` varchar(64) NOT NULL,
  `cdts_list_guid` varchar(64) NOT NULL,
  `control_number` varchar(64) NOT NULL,
  `control_time` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_dcgs_record`
-- ----------------------------
DROP TABLE IF EXISTS `table_dcgs_record`;
CREATE TABLE `table_dcgs_record` (
  `id` varchar(64) NOT NULL,
  `dcgs_record_guid` varchar(64) NOT NULL,
  `region_dcgs_guid` varchar(64) NOT NULL,
  `region_name` varchar(64) NOT NULL,
  `dcgs_name` varchar(64) NOT NULL,
  `dcgs_value` varchar(64) NOT NULL,
  `dcgs_delay` varchar(64) NOT NULL,
  `record_time` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_device`
-- ----------------------------
DROP TABLE IF EXISTS `table_device`;
CREATE TABLE `table_device` (
  `id` varchar(64) NOT NULL,
  `device_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `device_addr` varchar(64) NOT NULL,
  `device_name` varchar(64) NOT NULL,
  `device_id` varchar(64) NOT NULL,
  `device_type` varchar(64) NOT NULL,
  `device_valid` varchar(64) NOT NULL,
  `device_switch` varchar(64) NOT NULL,
  `device_value` varchar(64) NOT NULL,
  `device_delay` varchar(64) NOT NULL,
  `device_register_type` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  `region_bunding` varchar(255) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_group`
-- ----------------------------
DROP TABLE IF EXISTS `table_group`;
CREATE TABLE `table_group` (
  `id` varchar(64) NOT NULL,
  `group_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `group_addr` varchar(64) NOT NULL,
  `group_name` varchar(64) NOT NULL,
  `group_switch` varchar(64) NOT NULL,
  `group_value` varchar(64) NOT NULL,
  `group_delay` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_group_members`
-- ----------------------------
DROP TABLE IF EXISTS `table_group_members`;
CREATE TABLE `table_group_members` (
  `id` varchar(64) NOT NULL,
  `group_members_guid` varchar(64) NOT NULL,
  `table_group_guid` varchar(64) NOT NULL,
  `group_addr` varchar(64) NOT NULL,
  `device_addr` varchar(64) NOT NULL,
  `device_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  `device_name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_region`
-- ----------------------------
DROP TABLE IF EXISTS `table_region`;
CREATE TABLE `table_region` (
  `id` varchar(64) NOT NULL,
  `region_guid` varchar(64) NOT NULL,
  `region_addr` varchar(64) NOT NULL,
  `region_name` varchar(64) NOT NULL,
  `region_switch` varchar(64) NOT NULL,
  `region_value` varchar(64) NOT NULL,
  `region_delay` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_region_cdts_ctrl`
-- ----------------------------
DROP TABLE IF EXISTS `table_region_cdts_ctrl`;
CREATE TABLE `table_region_cdts_ctrl` (
  `id` varchar(64) NOT NULL,
  `region_cdts_ctrl` varchar(64) NOT NULL,
  `region_guid` varchar(64) NOT NULL,
  `cdts_list_guid` varchar(64) NOT NULL,
  `cdts_name` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  `region_name` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_region_device`
-- ----------------------------
DROP TABLE IF EXISTS `table_region_device`;
CREATE TABLE `table_region_device` (
  `id` varchar(64) NOT NULL,
  `region_device_guid` varchar(64) NOT NULL,
  `region_guid` varchar(64) NOT NULL,
  `region_addr` varchar(64) NOT NULL,
  `region_name` varchar(64) NOT NULL,
  `table_device_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `device_addr` varchar(64) NOT NULL,
  `device_name` varchar(64) NOT NULL,
  `channel_class` varchar(64) NOT NULL,
  `channel_guid` varchar(64) NOT NULL,
  `channel_name` varchar(64) NOT NULL,
  `channel_type` varchar(64) NOT NULL,
  `channel_bit_num` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_region_group`
-- ----------------------------
DROP TABLE IF EXISTS `table_region_group`;
CREATE TABLE `table_region_group` (
  `id` varchar(64) NOT NULL,
  `region_group_guid` varchar(64) NOT NULL,
  `region_guid` varchar(64) NOT NULL,
  `table_group_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `group_addr` varchar(64) NOT NULL,
  `group_name` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_region_scene`
-- ----------------------------
DROP TABLE IF EXISTS `table_region_scene`;
CREATE TABLE `table_region_scene` (
  `id` varchar(64) NOT NULL,
  `region_scene_guid` varchar(64) NOT NULL,
  `region_guid` varchar(64) NOT NULL,
  `table_scene_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `scene_addr` varchar(64) NOT NULL,
  `scene_name` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_register`
-- ----------------------------
DROP TABLE IF EXISTS `table_register`;
CREATE TABLE `table_register` (
  `id` varchar(64) NOT NULL,
  `register_guid` varchar(64) NOT NULL,
  `table_device_guid` varchar(64) NOT NULL,
  `device_register_type` varchar(64) NOT NULL,
  `register_name` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_scene`
-- ----------------------------
DROP TABLE IF EXISTS `table_scene`;
CREATE TABLE `table_scene` (
  `id` varchar(64) NOT NULL,
  `scene_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `scene_addr` varchar(64) NOT NULL,
  `scene_name` varchar(64) NOT NULL,
  `scene_switch` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_scene_members`
-- ----------------------------
DROP TABLE IF EXISTS `table_scene_members`;
CREATE TABLE `table_scene_members` (
  `id` varchar(64) NOT NULL,
  `scene_members_guid` varchar(64) NOT NULL,
  `table_scene_guid` varchar(64) NOT NULL,
  `scene_addr` varchar(64) NOT NULL,
  `device_addr` varchar(64) NOT NULL,
  `device_value` varchar(64) NOT NULL,
  `device_delay` varchar(64) NOT NULL,
  `device_guid` varchar(64) NOT NULL,
  `gateway_id` varchar(64) NOT NULL,
  `account_id` varchar(64) NOT NULL,
  `device_name` varchar(64) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `table_sensor_record`
-- ----------------------------
DROP TABLE IF EXISTS `table_sensor_record`;
CREATE TABLE `table_sensor_record` (
  `id` varchar(64) NOT NULL,
  `record_guid` varchar(64) NOT NULL,
  `table_device_guid` varchar(64) NOT NULL,
  `record_time` varchar(64) NOT NULL,
  `air_temperature` varchar(64) NOT NULL,
  `air_humidity` varchar(64) NOT NULL,
  `soil_temperature` varchar(64) NOT NULL,
  `soil_humidity` varchar(64) NOT NULL,
  `soil_PH_value` varchar(64) NOT NULL,
  `carbon_dioxide` varchar(64) NOT NULL,
  `illuminance` varchar(64) NOT NULL,
  `soil_conductivity` varchar(64) NOT NULL,
  `photons` varchar(64) NOT NULL,
  `liquid_PH_value` varchar(64) NOT NULL,
  `lai_value` varchar(64) NOT NULL,
  `reserve03` varchar(64) NOT NULL,
  `reserve04` varchar(64) NOT NULL,
  `reserve05` varchar(64) NOT NULL,
  `reserve06` varchar(64) NOT NULL,
  `reserve07` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `user_gateway`
-- ----------------------------
DROP TABLE IF EXISTS `user_gateway`;
CREATE TABLE `user_gateway` (
  `id` int(64) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL,
  `account_id` varchar(64) DEFAULT NULL,
  `gateway_id` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22222255 DEFAULT CHARSET=utf8;

-- ----------------------------
--  Table structure for `user_table`
-- ----------------------------
DROP TABLE IF EXISTS `user_table`;
CREATE TABLE `user_table` (
  `user_id` varchar(64) NOT NULL,
  `user_name` varchar(64) NOT NULL,
  `password` varchar(64) NOT NULL,
  `e_mail` varchar(64) NOT NULL,
  `phone` varchar(64) NOT NULL,
  `user_authorization` varchar(64) NOT NULL,
  `father_user` varchar(64) DEFAULT NULL,
  `country` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
--  Table structure for `region_status`
-- ----------------------------
DROP TABLE IF EXISTS `region_status`;
CREATE TABLE `region_status` (
  `region_guid` varchar(64) NOT NULL,
  `region_status` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`region_guid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

SET FOREIGN_KEY_CHECKS = 1;



-- ----------------------------
--  Table data for `account_data_info`
-- ----------------------------

INSERT INTO `account_data_info` VALUES ('00000001', null, null, null, null, null, '', '', '9000', '1000', 'a009');

-- ----------------------------
--  Table data for `user_table`
-- ----------------------------
INSERT INTO `user_table` VALUES ('12345667', 'root','e10adc3949ba59abbe56e057f20f883e', 'root', '1399999999', '3', null, null), ('xxxxxxxxx', 'cccc', 'e10adc3949ba59abbe56e057f20f883e', 'admin', '12345678932', '2', '12345667', '86');

-- ----------------------------
--  Table data for `user_gateway`
-- ----------------------------
INSERT INTO `user_gateway` VALUES ('1', '12345667', '11111111', null), ('2', 'xxxxxxxxx', '00000001', '');

-- ----------------------------
--  Table data for `public_recipe_data`
-- ----------------------------


INSERT INTO `public_recipe_data` VALUES ('00248166-80d3-4de3-8279-f1524442712b','cf3581e4-d098-42de-b931-c62f3820b061','48','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('02e70c4f-e001-4b08-a661-bdb5009806ff','cf3581e4-d098-42de-b931-c62f3820b061','17','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('041b68b3-5b65-4464-a6ab-c761144057ea','cf3581e4-d098-42de-b931-c62f3820b061','10','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('0ecf6889-6146-4db5-9451-74d9cd48b1a8','cf3581e4-d098-42de-b931-c62f3820b061','44','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('0ff55889-6454-4d87-83de-f1700a16f4ee','cf3581e4-d098-42de-b931-c62f3820b061','4','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('1057bf0a-8993-4c5e-b47f-309a5ca7ceee','cf3581e4-d098-42de-b931-c62f3820b061','23','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('15339188-cad7-4f7b-a4a3-50590558e492','cf3581e4-d098-42de-b931-c62f3820b061','45','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('179aef03-3768-4fff-9d04-f9603afc32ec','cf3581e4-d098-42de-b931-c62f3820b061','35','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('1af9eb8d-721e-48c2-a0c6-0dc15aed896c','cf3581e4-d098-42de-b931-c62f3820b061','46','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('1bb4eeea-6fbf-443d-97b9-d4e9d433cb50','cf3581e4-d098-42de-b931-c62f3820b061','35','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75'),('22e984a5-ccb5-4008-be5b-325cf549c32d','cf3581e4-d098-42de-b931-c62f3820b061','42','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('2964564c-fabe-4fdd-9afc-ba7a4113127d','cf3581e4-d098-42de-b931-c62f3820b061','27','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75'),('29acf701-b632-41ae-ba46-bcb97943296d','cf3581e4-d098-42de-b931-c62f3820b061','6','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('2e7f17d6-0484-4722-82cc-f896f2624c8a','cf3581e4-d098-42de-b931-c62f3820b061','48','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('31553c9d-8b8b-4b80-badf-c527e27fed6b','cf3581e4-d098-42de-b931-c62f3820b061','49','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('334795ec-1454-46cc-85c7-0cf490985ce9','cf3581e4-d098-42de-b931-c62f3820b061','32','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75'),('347e82c4-8903-43a5-8d7c-551dd4216c86','cf3581e4-d098-42de-b931-c62f3820b061','16','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('4090794c-d679-4c77-9dbc-154cae183b9d','cf3581e4-d098-42de-b931-c62f3820b061','24','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('419169ef-9053-43de-84e5-06aa5905e9d0','cf3581e4-d098-42de-b931-c62f3820b061','22','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('422b5e62-1fe0-4eed-8d6b-526bbd440888','cf3581e4-d098-42de-b931-c62f3820b061','37','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('42dfd713-713f-4d56-b6a4-1b470f78bc32','cf3581e4-d098-42de-b931-c62f3820b061','3','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('456a50bb-e014-4d3d-b591-1f5d746edcb4','cf3581e4-d098-42de-b931-c62f3820b061','40','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('45ad6a77-8fad-43f4-853e-509e64553a11','cf3581e4-d098-42de-b931-c62f3820b061','42','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('4682ac69-a9e6-40bd-b1a1-b435da59b48a','cf3581e4-d098-42de-b931-c62f3820b061','50','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('48088422-e8b9-4b76-9fb4-bc839a699ff0','cf3581e4-d098-42de-b931-c62f3820b061','39','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('48152a39-5adc-4d65-921a-43d4ad080ad6','cf3581e4-d098-42de-b931-c62f3820b061','33','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('4b90cac4-79d4-4506-99b6-874b7ae26982','cf3581e4-d098-42de-b931-c62f3820b061','36','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('4e1a76db-c8f8-4143-95f2-415504a2357c','cf3581e4-d098-42de-b931-c62f3820b061','41','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('5024d059-9687-4bf5-bbd3-a20adcde8fa3','cf3581e4-d098-42de-b931-c62f3820b061','1','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('504f5031-d688-4c99-8c8e-99865c0a01ba','cf3581e4-d098-42de-b931-c62f3820b061','33','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75'),('544fa770-e9ea-4055-b922-08c54a3e4e5f','cf3581e4-d098-42de-b931-c62f3820b061','28','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('588f3e1c-30ac-4992-8046-bf9c5c431ac1','cf3581e4-d098-42de-b931-c62f3820b061','38','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('5c41868d-31b8-441f-af3b-546b7d38474d','cf3581e4-d098-42de-b931-c62f3820b061','31','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75'),('5d8e23ee-495d-43bb-b7a2-0a2fc796366b','cf3581e4-d098-42de-b931-c62f3820b061','2','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('5e4efc5a-1bae-4554-9683-0a8a4127be0c','cf3581e4-d098-42de-b931-c62f3820b061','21','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('65ee7d08-13d5-41f5-974d-6b83feb456b5','cf3581e4-d098-42de-b931-c62f3820b061','37','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('68679f3d-7f09-48e5-9d93-12cb0a8f5c0e','cf3581e4-d098-42de-b931-c62f3820b061','26','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('69adcec3-1c5c-463d-bd08-82775f5b0f24','cf3581e4-d098-42de-b931-c62f3820b061','45','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('6a6ae2af-2060-47c8-bea1-d5b0f6281177','cf3581e4-d098-42de-b931-c62f3820b061','4','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('6a8a60a7-4e5d-467b-aab9-4efa871af09a','cf3581e4-d098-42de-b931-c62f3820b061','39','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('6b0aca4b-cc5d-4986-958b-34c3517ded8f','cf3581e4-d098-42de-b931-c62f3820b061','1','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('6c3ef238-1452-460b-a8e4-21e826a7194b','cf3581e4-d098-42de-b931-c62f3820b061','11','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('6e37b698-51b5-496f-a8be-76038b42ce07','cf3581e4-d098-42de-b931-c62f3820b061','5','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('6e81983e-15ae-4593-bbd5-2638f040905e','cf3581e4-d098-42de-b931-c62f3820b061','15','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('6f18ccf1-8713-484a-a401-22e597c49bc0','cf3581e4-d098-42de-b931-c62f3820b061','29','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('708e4d76-2afb-4799-9473-730dfd367b97','cf3581e4-d098-42de-b931-c62f3820b061','19','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('71abbcfb-777c-427d-a87a-1c186aca952f','cf3581e4-d098-42de-b931-c62f3820b061','47','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('7279c32f-8432-47ad-85a9-75b5dea00ed7','cf3581e4-d098-42de-b931-c62f3820b061','10','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('7392e39f-c9ec-45d0-b837-a9186a31c363','cf3581e4-d098-42de-b931-c62f3820b061','21','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('778d96c0-8b31-496f-b7e2-3213026617bd','cf3581e4-d098-42de-b931-c62f3820b061','49','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('7c34b1e3-63a8-48c5-902d-5edb29be5724','cf3581e4-d098-42de-b931-c62f3820b061','34','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('7ee2414a-4e6c-4881-be43-5dc5b33702e1','cf3581e4-d098-42de-b931-c62f3820b061','2','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('810aaa9e-4875-4a72-97fd-8c6d0e86f4af','cf3581e4-d098-42de-b931-c62f3820b061','7','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('83abd921-ceef-4947-8077-c70cd5606f39','cf3581e4-d098-42de-b931-c62f3820b061','36','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('83c5ef60-778d-4fba-9759-ec5369830b36','cf3581e4-d098-42de-b931-c62f3820b061','31','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('85c0025d-1ff5-46bc-b881-9cbf957c9130','cf3581e4-d098-42de-b931-c62f3820b061','27','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('8c0f5c76-6d16-4818-9424-c1130d839ee8','cf3581e4-d098-42de-b931-c62f3820b061','8','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('8eb6d483-ecd1-440e-8531-971b82ae09cd','cf3581e4-d098-42de-b931-c62f3820b061','12','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('9007b068-dcf4-4b7b-91f5-163044499047','cf3581e4-d098-42de-b931-c62f3820b061','17','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('9b1731f7-606b-4ea3-a35b-7e12c022eb0b','cf3581e4-d098-42de-b931-c62f3820b061','14','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('9c38f776-d541-4a50-a0d1-293442697339','cf3581e4-d098-42de-b931-c62f3820b061','14','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('9eb8c884-f62a-4d14-9973-26328aba11d8','cf3581e4-d098-42de-b931-c62f3820b061','20','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('a1fc7c1e-9333-4022-a9d6-d4a11d7bb7a1','cf3581e4-d098-42de-b931-c62f3820b061','32','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('a42affef-9f44-407b-88e0-f665b1aa576a','cf3581e4-d098-42de-b931-c62f3820b061','43','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('a6e43604-b857-4a61-9ff6-47866c8857b7','cf3581e4-d098-42de-b931-c62f3820b061','13','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('acbbf8a4-4045-49a4-adfc-6555aaedacdc','cf3581e4-d098-42de-b931-c62f3820b061','9','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('aee8aaaa-a037-4cd5-8522-9ea9a3c3c2df','cf3581e4-d098-42de-b931-c62f3820b061','44','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('b2f8933f-8cd0-4065-a6b7-18c749fdb2e9','cf3581e4-d098-42de-b931-c62f3820b061','7','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('b39962ff-c18e-45ce-b22c-3160d607ecc1','cf3581e4-d098-42de-b931-c62f3820b061','19','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('b9ab301d-89e0-4df6-8e90-5f27b344fc30','cf3581e4-d098-42de-b931-c62f3820b061','30','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('b9df3c5a-d12c-40c4-b609-6a864c9a653a','cf3581e4-d098-42de-b931-c62f3820b061','38','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('bc9c75f5-0667-4687-ab16-f8ade57b33ac','cf3581e4-d098-42de-b931-c62f3820b061','11','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('c2a9b5f1-fba7-4db5-bb65-47dd5e9d2dd0','cf3581e4-d098-42de-b931-c62f3820b061','15','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('c6aa0405-92f4-4f5f-a741-d7f0a5afb269','cf3581e4-d098-42de-b931-c62f3820b061','26','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75'),('c73a88b7-c0b0-4641-90c2-b8e6dc94c676','cf3581e4-d098-42de-b931-c62f3820b061','46','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('c9233815-a905-40ee-ad56-bde4edfb536a','cf3581e4-d098-42de-b931-c62f3820b061','18','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('c969254c-a2be-4c98-a18a-4ed73e889ddc','cf3581e4-d098-42de-b931-c62f3820b061','3','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('c9ebd6ea-596e-4947-b601-a94766609199','cf3581e4-d098-42de-b931-c62f3820b061','40','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('cd9e6dc1-21ab-4350-aa91-5ae73d91daec','cf3581e4-d098-42de-b931-c62f3820b061','5','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('d00b8eb7-c11f-4e87-9492-ad8e37a5167d','cf3581e4-d098-42de-b931-c62f3820b061','47','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('d613fd24-b7b0-4c90-82bc-8187b5ff911f','cf3581e4-d098-42de-b931-c62f3820b061','8','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('d74a65b1-4e32-4a4d-9533-90d44bf3ed1a','cf3581e4-d098-42de-b931-c62f3820b061','41','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('d8534fe8-71cd-4ee5-9ff0-1fa722941b92','cf3581e4-d098-42de-b931-c62f3820b061','23','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('d9453455-b09a-47b4-af81-821f3c49121f','cf3581e4-d098-42de-b931-c62f3820b061','25','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('d9597b27-e1f0-4eb8-918c-c4902b815fa5','cf3581e4-d098-42de-b931-c62f3820b061','43','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','22','22','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','22','22','70','75'),('dcef8af0-fbd7-449a-b980-75ec19ee406f','cf3581e4-d098-42de-b931-c62f3820b061','20','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('e6827502-ef5e-4eb0-914e-6fd9e976d4a1','cf3581e4-d098-42de-b931-c62f3820b061','50','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','25','25','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','25','25','70','75'),('e9328067-6511-4855-b3a6-ae7c5423ff04','cf3581e4-d098-42de-b931-c62f3820b061','13','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('ec615483-e83e-4c26-b376-59569b374c61','cf3581e4-d098-42de-b931-c62f3820b061','29','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75'),('ed129899-bf7b-44b4-a944-30ec03a016ed','cf3581e4-d098-42de-b931-c62f3820b061','24','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('ee22655f-b22e-4800-b87f-a6f2d11e07cf','cf3581e4-d098-42de-b931-c62f3820b061','16','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('ee9269a5-c15e-4bdc-8f99-a0e17f7c9631','cf3581e4-d098-42de-b931-c62f3820b061','30','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75'),('f0b86428-54e1-4375-9210-bb7444a18b0e','cf3581e4-d098-42de-b931-c62f3820b061','18','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('f3815a2f-6e20-4ad7-bb38-71616f9587ef','cf3581e4-d098-42de-b931-c62f3820b061','6','0','12',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','15','15','70','75'),('f4f86373-7cd6-4de1-81bf-febfeaecab16','cf3581e4-d098-42de-b931-c62f3820b061','9','12','24',NULL,'1',NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','150','150','5.6','6.8','70','75','4','7','1.2','1.6','4','7','0.5','1','1200','1500','10000','15000','20','20','70','75'),('f7bdcdce-289d-4690-a49e-f46c4b6fdfe7','cf3581e4-d098-42de-b931-c62f3820b061','34','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75'),('f81b309c-b166-48e9-8b94-49fb9fc1c9e0','cf3581e4-d098-42de-b931-c62f3820b061','25','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('f8e0b024-c844-4917-857c-b3d0504cf126','cf3581e4-d098-42de-b931-c62f3820b061','12','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','15','15','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','15','15','70','75'),('fa862eff-b13c-4fcb-ab75-8c92e0ebb879','cf3581e4-d098-42de-b931-c62f3820b061','22','12','24',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','20','20','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','1.2','3','1200','1500','10000','15000','20','20','70','75'),('ffd001f8-013c-4925-ac83-8d52c3dc0d51','cf3581e4-d098-42de-b931-c62f3820b061','28','0','12',NULL,NULL,NULL,'1',NULL,NULL,'2016-09-05 00:00:00','6','6.3','1.3','2.7','18','18','200','250','5.6','6.8','70','75','4','7','1.2','1.6','4','7','3','5.4','1200','1500','10000','15000','18','18','70','75');

-- ----------------------------
--  Table data for `public_recipe`
-- ----------------------------
INSERT INTO `public_recipe` VALUE ('cf3581e4-d098-42de-b931-c62f3820b061','Lettuce');
COMMIT;