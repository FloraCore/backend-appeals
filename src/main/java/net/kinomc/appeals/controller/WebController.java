package net.kinomc.appeals.controller;

import lombok.extern.slf4j.Slf4j;
import net.kinomc.appeals.common.BaseResponse;
import net.kinomc.appeals.common.ResultUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网站接口
 */
@RestController
@RequestMapping("/web")
@Slf4j
public class WebController {
    @Value("${web.icp.information:ICP备案号}")
    private String icpInformation;
    @Value("${web.icp.code:123456789}")
    private String icpCode;


    @GetMapping("/get/icp/information")
    public BaseResponse<String> getICPInformation() {
        return ResultUtils.success(icpInformation);
    }

    @GetMapping("/get/icp/code")
    public BaseResponse<String> getICPCode() {
        return ResultUtils.success(icpCode);
    }
}
