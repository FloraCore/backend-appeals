package net.kinomc.appeals.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.model.litebans.Bans;
import net.kinomc.appeals.model.litebans.Mutes;
import net.kinomc.appeals.model.litebans.Punish;
import net.kinomc.appeals.service.LitebansService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@SuppressWarnings("SqlResolve")
@Service
@DS("litebans")
@Slf4j
public class LitebansServiceImpl implements LitebansService {
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Value("${litebans.table_prefix:litebans_}")
    private String tablePrefix;

    @Override
    public UUID getUUIDByName(String username) {
        if (StringUtils.isAnyBlank(username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String sql = "SELECT uuid FROM " + tablePrefix + "history WHERE name=? ORDER BY date DESC LIMIT 1";
        String obj;
        try {
            obj = jdbcTemplate.queryForObject(sql, String.class, username);
        } catch (EmptyResultDataAccessException e) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "这名玩家从未上线过服务器");
        }
        try {
            assert obj != null;
            return UUID.fromString(obj);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的UUID");
        }
    }

    @Override
    public String getIPByUUID(UUID uuid) {
        if (uuid == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String sql = "SELECT ip FROM " + tablePrefix + "history WHERE uuid=? ORDER BY date DESC LIMIT 1";
        String obj = jdbcTemplate.queryForObject(sql, String.class, uuid.toString());
        if (obj == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "这名玩家从未上线过服务器");
        }
        return obj;
    }

    @Override
    public String getNameByUUID(UUID uuid) {
        if (uuid == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String sql = "SELECT name FROM " + tablePrefix + "history WHERE uuid=? ORDER BY date DESC LIMIT 1";
        String obj = jdbcTemplate.queryForObject(sql, String.class, uuid.toString());
        if (obj == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "这名玩家从未上线过服务器");
        }
        return obj;
    }

    @Override
    public Punish checkPunish(UUID uuid) {
        if (uuid == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        Punish punish = new Punish();
        boolean active = false;
        String banSql = "SELECT * FROM " + tablePrefix + "bans WHERE uuid=? ORDER BY id DESC LIMIT 1";
        Bans bans;
        try {
            bans = jdbcTemplate.queryForObject(banSql, (rs, rowNum) -> {
                Bans b = new Bans();
                b.setId(rs.getLong("id"));
                b.setUuid(UUID.fromString(rs.getString("uuid")));
                b.setReason(rs.getString("reason"));
                b.setBannedByName(rs.getString("banned_by_name"));
                b.setActive(rs.getInt("active"));
                b.setTime(rs.getLong("time"));
                b.setUntil(rs.getLong("until"));
                return b;
            }, uuid.toString());
        } catch (EmptyResultDataAccessException e) {
            bans = null;
        }
        if (bans != null) {
            if (bans.getActive() == 1) {
                punish.setType("ban");
                punish.setId(bans.getId());
                punish.setReason(bans.getReason());
                punish.setBannedByName(bans.getBannedByName());
                punish.setTime(bans.getTime());
                punish.setUntil(bans.getUntil());
                active = true;
            }
        }
        if (!active) {
            String muteSql = "SELECT * FROM " + tablePrefix + "mutes WHERE uuid=? ORDER BY id DESC LIMIT 1";
            Mutes mutes;
            try {
                mutes = jdbcTemplate.queryForObject(muteSql, (rs, rowNum) -> {
                    Mutes m = new Mutes();
                    m.setId(rs.getLong("id"));
                    m.setUuid(UUID.fromString(rs.getString("uuid")));
                    m.setReason(rs.getString("reason"));
                    m.setBannedByName(rs.getString("banned_by_name"));
                    m.setActive(rs.getInt("active"));
                    m.setTime(rs.getLong("time"));
                    m.setUntil(rs.getLong("until"));
                    return m;
                }, uuid.toString());
            } catch (EmptyResultDataAccessException e) {
                mutes = null;
            }
            if (mutes != null) {
                if (mutes.getActive() == 1) {
                    punish.setType("mute");
                    punish.setId(mutes.getId());
                    punish.setReason(mutes.getReason());
                    punish.setBannedByName(mutes.getBannedByName());
                    punish.setTime(mutes.getTime());
                    punish.setUntil(mutes.getUntil());
                    active = true;
                }
            }
        }
        if (active) {
            return punish;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "这名玩家当前无处罚记录");
        }
    }

    @Override
    public boolean unBanPlayer(UUID uuid) {
        if (uuid == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String sql = "UPDATE " + tablePrefix + "bans SET active=0,removed_by_date=NOW(),removed_by_uuid='APPEAL',removed_by_name='Appeal',removed_by_reason='Appeal' WHERE uuid=? AND active=1 AND(until<1 OR until>" + System.currentTimeMillis() + ") AND(server_scope='*')";
        return jdbcTemplate.update(sql, uuid.toString()) > 0;
    }

    @Override
    public boolean banPlayer(UUID uuid, String reason, Long date) {
        // 先解除有效处罚，以确保可以覆盖处罚。
        unBanPlayer(uuid);
        // 创建一个处罚。
        String ip = getIPByUUID(uuid);

        String sql = "INSERT INTO " + tablePrefix + "bans(uuid,ip,reason,banned_by_uuid,banned_by_name,time,until,template,server_scope,server_origin,silent,ipban,ipban_wildcard,active)VALUES(?,?,?,'APPEAL','appeal'," + System.currentTimeMillis() + ",?,255,'*','appeal',0,0,0,1)";
        return jdbcTemplate.update(sql, uuid.toString(), ip, reason, date) > 0;
    }
}
