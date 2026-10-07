package com.innospots.nexus.console.credential.otp.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.innospots.nexus.console.credential.otp.domain.entity.OtpChallengeEntity;

/**
 * {@link OtpChallengeEntity} 的 MyBatis-Plus Mapper；无自定义 SQL，由
 * {@link com.innospots.nexus.console.credential.otp.service.OtpChallengeService} 通过
 * {@link com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper} 访问。
 *
 * @author Smars
 * @date 2026/09/19
 * @see OtpChallengeEntity
 */
@Mapper
public interface OtpChallengeDao extends BaseMapper<OtpChallengeEntity> {
}
