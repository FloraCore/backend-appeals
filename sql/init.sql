CREATE TABLE IF NOT EXISTS user
(
    id           BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    userName     VARCHAR(256)                           NOT NULL COMMENT '用户昵称',
    userEmail    VARCHAR(256)                           NOT NULL COMMENT '用户邮箱',
    userAvatar   VARCHAR(1024)                          NULL COMMENT '用户头像',
    userGender   TINYINT      DEFAULT 0                 NOT NULL COMMENT '性别：男(0)/女(1)',
    userPosition VARCHAR(256) DEFAULT 'unknown'         NOT NULL COMMENT '用户职位',
    userRole     VARCHAR(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/ admin',
    userPassword VARCHAR(512)                           NOT NULL COMMENT '密码',
    userKey      VARCHAR(512)                           NOT NULL COMMENT '用户2FA密钥',
    createTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isBaned      TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否封禁：否(0)/是(1)',
    acceptMail   TINYINT      DEFAULT 1                 NOT NULL COMMENT '是否接受邮件：否(0)/是(1)',
    isDelete     TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否删除'
) COMMENT '用户';

-- 第一次加载把下列注释删除，创建默认账户
-- INSERT INTO user (userName, userEmail, userRole, userPassword, userKey)
-- VALUES ('xLikeWATCHDOG', '86328425@qq.com', 'admin', 'c8a8640d1e6a917f8436bdd225152f9d', 'HYWEY4M3D3XDPB3T');

CREATE TABLE IF NOT EXISTS appeal
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    userName   VARCHAR(256)                           NOT NULL COMMENT '用户名',
    appealType VARCHAR(256) DEFAULT 'punish'          NOT NULL COMMENT '申诉类型：punish/abnormal',
    uuid       VARCHAR(36)                            NOT NULL COMMENT 'UUID，与详情表中的UUID一致',
    createTime DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    state      TINYINT      DEFAULT 0                 NOT NULL COMMENT '受理状态：未受理(0)/受理(1)/已处理(2)',
    isDelete   TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否删除'
) COMMENT '申诉列表';

CREATE TABLE IF NOT EXISTS appeal_abnormal
(
    id               BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    uuid             VARCHAR(36)                        NOT NULL COMMENT 'UUID',
    userName         VARCHAR(256)                       NOT NULL COMMENT '玩家名',
    userEmail        VARCHAR(256)                       NULL COMMENT '玩家邮箱',
    appealReason     TEXT                               NOT NULL COMMENT '申诉理由',
    photos           TEXT                               NULL COMMENT '图片数据库UUID',
    assignee         BIGINT                             NULL COMMENT 'STAFF-ID',
    processingTime   DATETIME                           NULL COMMENT '受理时间',
    conclusion       TINYINT                            NULL COMMENT '结论：通过(0)/不通过(1)/无效(2)',
    conclusionReason TEXT                               NULL COMMENT '结论理由',
    updateTime       DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete         TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
) COMMENT '异常申诉详情';

CREATE TABLE IF NOT EXISTS appeal_punish
(
    id               BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    punishInfo       BIGINT                             NOT NULL COMMENT '处罚信息ID',
    uuid             VARCHAR(36)                        NOT NULL COMMENT 'UUID',
    userName         VARCHAR(256)                       NOT NULL COMMENT '玩家名',
    userEmail        VARCHAR(256)                       NULL COMMENT '玩家邮箱',
    appealReason     TEXT                               NOT NULL COMMENT '申诉理由',
    photos           TEXT                               NULL COMMENT '图片数据库UUID',
    assignee         BIGINT                             NULL COMMENT 'STAFF-ID',
    processingTime   DATETIME                           NULL COMMENT '受理时间',
    conclusion       TINYINT                            NULL COMMENT '结论：通过(0)/不通过(1)/无效(2)',
    conclusionReason TEXT                               NULL COMMENT '结论理由',
    updateTime       DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete         TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
) COMMENT '处罚申诉详情';

CREATE TABLE IF NOT EXISTS punish_info
(
    id           BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    punishID     BIGINT                     NOT NULL COMMENT '在LiteBans数据库的处罚ID',
    reason       TEXT                       NOT NULL COMMENT '处罚原因',
    bannedByName VARCHAR(256)               NOT NULL COMMENT '处理人',
    type         VARCHAR(256) DEFAULT 'ban' NOT NULL COMMENT '申诉类型：ban/mute',
    time         BIGINT                     NULL COMMENT '处罚时间（时间戳）',
    until        BIGINT                     NULL COMMENT '处罚过期时间（若为永久，则<=0）',
    isDelete     TINYINT      DEFAULT 0     NOT NULL COMMENT '是否删除'
) COMMENT '处罚信息';

CREATE TABLE IF NOT EXISTS appeal_blacklist
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    uuid       VARCHAR(36)                        NOT NULL COMMENT '玩家的UUID',
    assignee   BIGINT                             NOT NULL COMMENT '处理人',
    reason     TEXT                               NOT NULL COMMENT '处罚原因',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    isDelete   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
) COMMENT '黑名单玩家';

CREATE TABLE IF NOT EXISTS photos
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    uuid       VARCHAR(36)                        NOT NULL COMMENT '图片UUID',
    data       LONGTEXT                           NOT NULL COMMENT '图片数据',
    md5        VARCHAR(36)                        NOT NULL COMMENT 'MD5',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    isDelete   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
) COMMENT '图片数据库';

CREATE TABLE IF NOT EXISTS markdowns
(
    id          BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    uuid        VARCHAR(36)                        NOT NULL COMMENT 'UUID',
    type        TINYINT                            NOT NULL COMMENT '类型目前分为两个。0表示主页公告；1表示规则公告',
    title       TEXT                               NOT NULL COMMENT '标题',
    content     TEXT                               NOT NULL COMMENT '内容',
    creator     BIGINT                             NOT NULL COMMENT '创建这个md的人(id)',
    participant TEXT                               NULL COMMENT '参与修改这个md的人(ids)',
    top         TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否置顶这个md：否(0)/是(1)',
    enabled     TINYINT  DEFAULT 1                 NOT NULL COMMENT '是否启用md（若未启用，则在获取时将会自动过滤）：否(0)/是(1)',
    createTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete    TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
) COMMENT 'MD公告，保存主页和规则的内容';

CREATE TABLE IF NOT EXISTS ip_location
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    ip         VARCHAR(15)                        NOT NULL COMMENT 'ip地址',
    country    VARCHAR(16)                        NULL COMMENT '国家（极少为空，当此项为空时会通过ip138查询）',
    region     VARCHAR(16)                        NULL COMMENT '省份/自治区/直辖市（少数为空，当此项为空时会通过ip138查询）',
    city       VARCHAR(16)                        NULL COMMENT '地级市（部分为空，当此项为空时会通过ip138查询）',
    district   VARCHAR(16)                        NULL COMMENT '区/县（部分为空）',
    isp        VARCHAR(16)                        NULL COMMENT '运营商',
    zip        VARCHAR(16)                        NULL COMMENT '邮政编码',
    zone       VARCHAR(16)                        NULL COMMENT '地区区号',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
) COMMENT 'IP地址定位数据库，约定为如果一个IP已经超过1个月未更新了，在下次查询时会通过ip138自动刷新';

CREATE TABLE IF NOT EXISTS reason_list
(
    id         BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    title      TEXT                               NOT NULL COMMENT '理由标题',
    reason     TEXT                               NOT NULL COMMENT '结论理由',
    type       TINYINT                            NOT NULL COMMENT '结论：通过(0)/不通过(1)/无效(2)',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    isDelete   TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除'
) COMMENT '推荐理由列表';