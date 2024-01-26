package net.kinomc.appeals.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.ErrorCode;
import net.kinomc.appeals.exception.BusinessException;
import net.kinomc.appeals.mapper.IPLocationMapper;
import net.kinomc.appeals.model.entity.IPLocation;
import net.kinomc.appeals.service.IPLocationService;
import net.kinomc.appeals.utils.NetUtils;
import net.kinomc.appeals.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class IPLocationServiceImpl extends ServiceImpl<IPLocationMapper, IPLocation> implements IPLocationService {
    @Resource
    private IPLocationMapper ipLocationMapper;
    @Value("${web.ip138.token:token}")
    private String ip_token;

    @Override
    public String getIPLocation(String ip) {
        QueryWrapper<IPLocation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ip", ip);
        IPLocation ipLocation = ipLocationMapper.selectOne(queryWrapper);
        List<String> ret = new ArrayList<>();
        boolean update = false;
        if (ipLocation == null) {
            //无记录的IP数据
            ret = NetUtils.queryIP(ip, ip_token);
            update = true;
        } else {
            Date date = new Date();
            if (TimeUtils.getMonthDiff(date, ipLocation.getUpdateTime()) >= 1) {
                //这个IP已经超过1个月未更新了
                ret = NetUtils.queryIP(ip, ip_token);
                update = true;
            } else {
                ret.add(ipLocation.getCountry());
                ret.add(ipLocation.getRegion());
                ret.add(ipLocation.getCity());
                ret.add(ipLocation.getDistrict());
                ret.add(ipLocation.getIsp());
                ret.add(ipLocation.getZip());
                ret.add(ipLocation.getZone());
            }
        }
        if (update) {
            updateIPLocation(ip, ret);
        }
        return NetUtils.getIPLocation(ret);
    }

    @Override
    public void updateIPLocation(String ip, List<String> locationInfo) {
        if (StringUtils.isAnyBlank(ip)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "IP数据为空");
        }
        QueryWrapper<IPLocation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ip", ip);
        IPLocation ipLocation = ipLocationMapper.selectOne(queryWrapper);
        if (ipLocation == null) {
            //无记录的IP数据
            synchronized (ip.intern()) {
                IPLocation ipLocation1 = new IPLocation();
                ipLocation1.setIp(ip);
                ipLocation1.setCountry(locationInfo.get(0));
                ipLocation1.setRegion(locationInfo.get(1));
                ipLocation1.setCity(locationInfo.get(2));
                ipLocation1.setDistrict(locationInfo.get(3));
                ipLocation1.setIsp(locationInfo.get(4));
                ipLocation1.setZip(locationInfo.get(5));
                ipLocation1.setZone(locationInfo.get(6));
                boolean saveResult = this.save(ipLocation1);
                if (!saveResult) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，数据库错误");
                }
            }
        } else {
            //有记录的IP数据，进行更新
            ipLocation.setCountry(locationInfo.get(0));
            ipLocation.setRegion(locationInfo.get(1));
            ipLocation.setCity(locationInfo.get(2));
            ipLocation.setDistrict(locationInfo.get(3));
            ipLocation.setIsp(locationInfo.get(4));
            ipLocation.setZip(locationInfo.get(5));
            ipLocation.setZone(locationInfo.get(6));
            boolean saveResult = this.save(ipLocation);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加失败，数据库错误");
            }
        }
    }
}
