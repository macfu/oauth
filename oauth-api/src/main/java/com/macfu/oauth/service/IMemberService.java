package com.macfu.oauth.service;

import com.macfu.oauth.po.Member;

import java.util.Map;
import java.util.Set;

/**
 * @Author: liming
 * @Date: 2018/12/12 14:41
 * @Description: MemberService接口 
 */
public interface IMemberService {

    /**
     * 根据用户Id取得一个完整对象信息，包含有密码和锁定状态
     * @param mid 要查询的用户Id
     * @return 用户对象，如果用户不存在返回null
     */
    Member get(String mid);

    /**
     * 根据指定的用户编号获取对应的授权信息
     * @param mid 用户ID
     * @return 返回的信息里面包含有两类数据
     * 1.key = allRoles,value = 该用户具备的所有的角色的信息
     * 2.key = allActions,value = 该用户具备的所有的权限
     */
    Map<String, Set<String>> getRoleAndActionByMember(String mid);
}
