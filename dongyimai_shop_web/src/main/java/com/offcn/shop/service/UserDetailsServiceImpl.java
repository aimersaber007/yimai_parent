package com.offcn.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: lhq
 * @Date: 2020/8/14 15:15
 * @Description:  自定义认证类
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;


    public UserDetails loadUserByUsername(String sellerId) throws UsernameNotFoundException {

        //1.构建权限列表
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //2.根据用户名sellerId查询商家信息
        TbSeller tbSeller =  sellerService.findOne(sellerId);
        //3.判断商家信息是否为空
        if(null!=tbSeller) {
            //4.判断审核状态为 审核通过
            if(tbSeller.getStatus().equals("1")) {
                return new User(sellerId, tbSeller.getPassword(), grantedAuthorityList);
            }else{
                return null;
            }
        }else{
            return null;
        }
    }
}
