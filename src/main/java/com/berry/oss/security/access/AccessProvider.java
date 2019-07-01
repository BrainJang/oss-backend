package com.berry.oss.security.access;

import com.berry.oss.common.ResultCode;
import com.berry.oss.common.constant.Constants;
import com.berry.oss.common.exceptions.BaseException;
import com.berry.oss.common.utils.Auth;
import com.berry.oss.core.service.IAccessKeyInfoDaoService;
import com.berry.oss.security.core.entity.Role;
import com.berry.oss.security.core.service.IUserDaoService;
import com.berry.oss.security.dto.UserInfoDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Berry_Cooper.
 * @date 2019-06-29 17:22
 * fileName：AccessProvider
 * Use：
 */
@Component
public class AccessProvider {

    private final IAccessKeyInfoDaoService accessKeyInfoDaoService;

    @Resource
    private IUserDaoService userDaoService;

    public AccessProvider(IAccessKeyInfoDaoService accessKeyInfoDaoService) {
        this.accessKeyInfoDaoService = accessKeyInfoDaoService;
    }

    Authentication getAuthentication(String accessToken) throws IllegalAccessException {
        String[] data = accessToken.split(":");
        if (data.length != Constants.ENCODE_DATA_LENGTH) {
            throw new BaseException(ResultCode.ILLEGAL_ACCESS_TOKEN);
        }
        String accessKeyId = data[0];
        UserInfoDTO principal = accessKeyInfoDaoService.getUserInfoDTO(accessKeyId);
        if (principal == null) {
            throw new BaseException(ResultCode.DATA_NOT_EXIST);
        }

        // 验证token有效性，请求无法获取错误信息
        Auth.verifyThenGetData(accessToken, principal.getAccessKeySecret());

        Set<Role> roleList = userDaoService.findRoleListByUserId(principal.getId());
        List<GrantedAuthority> grantedAuthorities = roleList.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(principal, accessToken, grantedAuthorities);
    }
}