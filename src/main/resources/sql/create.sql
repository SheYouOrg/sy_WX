CREATE TABLE `wx_access_token` (
`id`  bigint NOT NULL AUTO_INCREMENT,
`at`  varchar(512) NOT NULL ,
`create_time`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
`expires_in`  int NOT NULL ,
`update_time`  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ,
UNIQUE INDEX `idx_id` (`id`) USING BTREE ,
UNIQUE INDEX `idx_at` (`at`) USING BTREE
)
;
ALTER TABLE `wx_access_token`
ADD COLUMN `invalid_at`  timestamp NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'at失效时间' AFTER `update_time`,
ADD COLUMN `latest`  tinyint NOT NULL DEFAULT 0 COMMENT '是否为最新记录 1：是' AFTER `invalid_at`;



