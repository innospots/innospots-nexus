package com.innospots.nexus.console.credential.otp.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.transaction.Transactional;

import com.innospots.nexus.base.events.EventBus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.ownership.CredentialOwnershipResolver;
import com.innospots.nexus.console.credential.password.VerificationType;
import com.innospots.nexus.console.credential.otp.dao.OtpChallengeDao;
import com.innospots.nexus.console.credential.otp.domain.OtpIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.OtpVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.entity.OtpChallengeEntity;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent;
import com.innospots.nexus.console.credential.otp.policy.OtpPolicy;
import com.innospots.nexus.console.credential.otp.status.OtpStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * OTP 挑战的唯一样式入口：发放、校验、作废，并在发放后发布 {@link OtpSendRequestedEvent}。
 * <p>持久化仅保存 {@link OtpChallengeEntity#getCodeVerifier()} 的 BCrypt 哈希；明文码只出现在进程内事件中，
 * 由邮件/SMS 等 adapter 订阅下发。校验成功默认不自动消费挑战，改密成功后须调用 {@link #invalidate} 或
 * {@link #expirePasswordResetCode} 作废，避免验证码被重复使用。</p>
 *
 * @author Smars
 * @date 2026/09/19
 * @see OtpPasswordVerificationOperator
 * @see com.innospots.nexus.console.credential.otp.domain.entity.OtpChallengeEntity
 * @see com.innospots.nexus.console.credential.otp.policy.OtpPolicy
 */
public class OtpChallengeService {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 写入 {@link OtpChallengeEntity#setAlgorithm(String)} 的哈希方案标识（与密码 BCrypt 共用工具，语义独立）。
     */
    private static final String CODE_HASH_ALGORITHM = "otp-bcrypt@v1";

    private final OtpChallengeDao challengeDao;
    private final OtpPolicy policy;

    /**
     * 使用 {@link OtpPolicy#DEFAULT}。
     *
     * @param challengeDao {@code nx_otp_challenge} Mapper
     */
    public OtpChallengeService(OtpChallengeDao challengeDao) {
        this(challengeDao, OtpPolicy.DEFAULT);
    }

    /**
     * 注入自定义策略（通常仅单测缩短 TTL/冷却时间）。
     *
     * @param challengeDao 挑战表 Mapper
     * @param policy       码长、有效期、重发冷却、最大错误次数
     */
    public OtpChallengeService(OtpChallengeDao challengeDao, OtpPolicy policy) {
        this.challengeDao = Checks.notNull(challengeDao, "challengeDao");
        this.policy = Checks.notNull(policy, "policy");
    }

    /**
     * 创建新挑战、入库哈希，并同步发布发送事件。
     * <p>调用场景：自定义 OTP 流程（step-up、绑定联系方式等）在 API 层校验入参后调用。</p>
     * <p>同一归属 + purpose + channel + 规范化 destination 下，冷却期内重复调用抛出
     * {@link OtpStatusCode#RESEND_TOO_FREQUENT}；否则作废仍有效的旧挑战再发新码。</p>
     *
     * @param command 安全域、用途、通道、原始地址与模板语言
     * @return 新插入行的 {@code challengeId}（与事件中一致）
     * @throws NexusException 重发过于频繁时 {@link OtpStatusCode#RESEND_TOO_FREQUENT}
     */
    @Transactional
    public String issue(OtpIssueCommand command) {
        Checks.notNull(command, "command");
        String rawCode = generateNumericCode();
        return persistChallenge(command, rawCode, true);
    }

    /**
     * 持久化外部生成的验证码（图形 captcha）；不发布发送事件。
     *
     * @param command 须使用 {@link com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel#CAPTCHA}
     * @param rawCode 明文验证码（图形码在入库前会规范化大小写）
     * @return 新挑战 ID
     */
    @Transactional
    public String issueCaptchaCode(OtpIssueCommand command, String rawCode) {
        Checks.notNull(command, "command");
        Checks.notBlank(rawCode, "rawCode");
        if (command.channel() != OtpChannel.CAPTCHA) {
            throw new IllegalArgumentException("issueCaptchaCode requires CAPTCHA channel");
        }
        return persistChallenge(command, normalizeCaptchaCode(rawCode), false);
    }

    /**
     * 查询挑战过期时间，供图形验证码 API 返回。
     *
     * @param challengeId 挑战主键
     * @return 过期时间
     */
    public LocalDateTime findChallengeExpiresAt(String challengeId) {
        Checks.notBlank(challengeId, "challengeId");
        OtpChallengeEntity entity = challengeDao.selectById(challengeId);
        if (entity == null) {
            throw NexusException.build(OtpStatusCode.CHALLENGE_NOT_FOUND);
        }
        return entity.getExpiresAt();
    }

    private String persistChallenge(OtpIssueCommand command, String rawCode, boolean publishSendEvent) {
        ConsoleOwnership ownership = CredentialOwnershipResolver.ownershipForRealm(command.securityRealm());
        String destination = command.channel().normalizeDestination(command.destination());
        OtpChallengeEntity active = findActive(ownership, command.purpose(), command.channel(), destination);
        if (active != null && isWithinCooldown(active)) {
            throw NexusException.build(OtpStatusCode.RESEND_TOO_FREQUENT);
        }
        if (active != null) {
            markConsumed(active);
        }
        OtpChallengeEntity entity = new OtpChallengeEntity();
        ConsoleOwnershipScope.stamp(entity, ownership);
        entity.setPurpose(command.purpose().name());
        entity.setChannel(command.channel().name());
        entity.setDestination(destination);
        entity.setAlgorithm(CODE_HASH_ALGORITHM);
        entity.setCodeVerifier(hashCode(rawCode));
        entity.setExpiresAt(LocalDateTime.now().plus(policy.ttl()));
        entity.setAttemptCount(0);
        entity.setMaxAttempts(policy.maxVerifyAttempts());
        entity.setResendCount(active == null ? 0 : active.getResendCount() + 1);
        challengeDao.insert(entity);
        if (publishSendEvent) {
            EventBus.publish(new OtpSendRequestedEvent(
                    entity.getChallengeId(),
                    command.purpose(),
                    command.channel(),
                    destination,
                    rawCode,
                    templateId(command.purpose(), command.channel()),
                    command.locale()));
        }
        return entity.getChallengeId();
    }

    /**
     * 校验用户提交的验证码；任何业务失败均吞掉异常并返回 {@code false}。
     * <p>调用场景：需要布尔结果的门面；明确错误码时请用 {@link #verifyOrThrow}。</p>
     *
     * @param command 须与发放时相同的 realm、purpose、channel 与地址（规范化规则一致）
     * @return 校验通过时为 {@code true}
     */
    public boolean verify(OtpVerifyCommand command) {
        try {
            verifyOrThrow(command);
            return true;
        } catch (NexusException ex) {
            return false;
        }
    }

    /**
     * 校验验证码；失败抛出对应 {@link OtpStatusCode}。
     * <p>调用场景：REST 或需要向客户端返回精确错误码的路径。</p>
     * <p>码错误时递增 {@link OtpChallengeEntity#getAttemptCount()}，达到 {@link OtpChallengeEntity#getMaxAttempts()}
     * 后作废挑战；校验成功不修改 {@code consumedAt}。</p>
     *
     * @param command 校验命令
     * @throws NexusException 挑战不存在、已消费、已过期、次数用尽或码不匹配
     */
    @Transactional
    public void verifyOrThrow(OtpVerifyCommand command) {
        Checks.notNull(command, "command");
        OtpChallengeEntity entity = requireActiveChallenge(command);
        String submittedCode = command.channel() == OtpChannel.CAPTCHA
                ? normalizeCaptchaCode(command.code())
                : command.code();
        if (!matchesCode(entity.getCodeVerifier(), submittedCode)) {
            entity.setAttemptCount(entity.getAttemptCount() + 1);
            challengeDao.updateById(entity);
            if (entity.getAttemptCount() >= entity.getMaxAttempts()) {
                markConsumed(entity);
            }
            throw NexusException.build(OtpStatusCode.CODE_INVALID);
        }
    }

    /**
     * 将当前「未消费」的有效挑战标记为已使用，不再校验码本身。
     * <p>调用场景：业务已成功完成（如密码已写入）后防止 OTP 被二次使用。</p>
     *
     * @param securityRealm 与发放一致的安全域
     * @param purpose       与发放一致的用途
     * @param channel       与发放一致的通道
     * @param destination   原始或规范化前的地址（内部会按通道规范化）
     */
    @Transactional
    public void invalidate(SecurityRealm securityRealm, OtpPurpose purpose, OtpChannel channel, String destination) {
        ConsoleOwnership ownership = CredentialOwnershipResolver.ownershipForRealm(securityRealm);
        String normalized = channel.normalizeDestination(destination);
        OtpChallengeEntity entity = findActive(ownership, purpose, channel, normalized);
        if (entity != null && entity.getConsumedAt() == null) {
            markConsumed(entity);
        }
    }

    /**
     * 向邮箱或手机发送「忘记密码」验证码。
     * <p>调用场景：{@link OtpPasswordVerificationOperator#sendVerificationCode}。</p>
     *
     * @param realm    通常为 {@link SecurityRealm#TENANT}
     * @param identity 邮箱或手机号
     * @param type     {@link VerificationType#EMAIL} 或 {@link VerificationType#MOBILE}
     * @param locale   消息模板语言，可为 null
     */
    public void sendPasswordResetCode(SecurityRealm realm, String identity, VerificationType type, String locale) {
        issue(new OtpIssueCommand(
                realm,
                OtpPurpose.PASSWORD_RESET,
                OtpChannel.fromVerificationType(type),
                identity,
                locale));
    }

    /**
     * 校验忘记密码场景下的验证码。
     * <p>调用场景：portal 写新密码前的 OTP 校验。</p>
     *
     * @param realm    安全域
     * @param identity 与发送时相同的邮箱或手机
     * @param type     通道类型
     * @param code     用户输入
     * @return 有效且未过期、未作废时为 {@code true}
     */
    public boolean verifyPasswordResetCode(
            SecurityRealm realm,
            String identity,
            VerificationType type,
            String code
    ) {
        return verify(new OtpVerifyCommand(
                realm,
                OtpPurpose.PASSWORD_RESET,
                OtpChannel.fromVerificationType(type),
                identity,
                code));
    }

    /**
     * 作废当前忘记密码挑战。
     * <p>调用场景：密码重置成功写入后，{@link OtpPasswordVerificationOperator#expireVerificationCode}。</p>
     *
     * @param realm    安全域
     * @param identity 邮箱或手机
     * @param type     通道类型
     */
    public void expirePasswordResetCode(SecurityRealm realm, String identity, VerificationType type) {
        invalidate(
                realm,
                OtpPurpose.PASSWORD_RESET,
                OtpChannel.fromVerificationType(type),
                identity);
    }

    /**
     * 加载可校验的挑战行，并断言未消费、未过期、未达尝试上限。
     */
    private OtpChallengeEntity requireActiveChallenge(OtpVerifyCommand command) {
        ConsoleOwnership ownership = CredentialOwnershipResolver.ownershipForRealm(command.securityRealm());
        String destination = command.channel().normalizeDestination(command.destination());
        OtpChallengeEntity entity = findActive(ownership, command.purpose(), command.channel(), destination);
        if (entity == null) {
            throw NexusException.build(OtpStatusCode.CHALLENGE_NOT_FOUND);
        }
        if (entity.getConsumedAt() != null) {
            throw NexusException.build(OtpStatusCode.CHALLENGE_CONSUMED);
        }
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw NexusException.build(OtpStatusCode.CHALLENGE_EXPIRED);
        }
        // 已达上限的挑战在 findActive 仍可能被查到（刚更新 attempt 后），统一按无效码拒绝
        if (entity.getAttemptCount() >= entity.getMaxAttempts()) {
            throw NexusException.build(OtpStatusCode.CODE_INVALID);
        }
        return entity;
    }

    /**
     * 查询同一投递键下最近一条未消费挑战（按创建时间倒序取首条）。
     */
    private OtpChallengeEntity findActive(
            ConsoleOwnership ownership,
            OtpPurpose purpose,
            OtpChannel channel,
            String destination
    ) {
        List<OtpChallengeEntity> rows = challengeDao.selectList(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<OtpChallengeEntity>()
                        .eq(OtpChallengeEntity::getPurpose, purpose.name())
                        .eq(OtpChallengeEntity::getChannel, channel.name())
                        .eq(OtpChallengeEntity::getDestination, destination)
                        .isNull(OtpChallengeEntity::getConsumedAt)
                        .orderByDesc(OtpChallengeEntity::getCreatedAt),
                ownership));
        if (rows.isEmpty()) {
            return null;
        }
        return rows.getFirst();
    }

    /**
     * 判断距上次创建是否仍处在 {@link OtpPolicy#resendCooldown()} 窗口内。
     */
    private boolean isWithinCooldown(OtpChallengeEntity entity) {
        if (entity.getCreatedAt() == null) {
            return false;
        }
        LocalDateTime earliest = entity.getCreatedAt().plus(policy.resendCooldown());
        return LocalDateTime.now().isBefore(earliest);
    }

    /**
     * 设置 {@link OtpChallengeEntity#setConsumedAt(LocalDateTime)} 为当前时间并回写数据库。
     */
    private void markConsumed(OtpChallengeEntity entity) {
        Objects.requireNonNull(entity, "entity");
        entity.setConsumedAt(LocalDateTime.now());
        challengeDao.updateById(entity);
    }

    /**
     * 按 {@link OtpPolicy#codeLength()} 生成均匀分布的数字串（含前导零）。
     */
    private String generateNumericCode() {
        int length = policy.codeLength();
        int bound = (int) Math.pow(10, length);
        int value = RANDOM.nextInt(bound);
        return String.format("%0" + length + "d", value);
    }

    private static String hashCode(String rawCode) {
        return CryptoUtils.encryptPassword(rawCode);
    }

    private static boolean matchesCode(String verifier, String rawCode) {
        return CryptoUtils.matchesPassword(rawCode, verifier);
    }

    private static String normalizeCaptchaCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim().toUpperCase();
    }

    /**
     * 事件与模板系统共用的稳定模板键：{@code credential.otp.<purpose>.<channel>}。
     */
    private static String templateId(OtpPurpose purpose, OtpChannel channel) {
        return "credential.otp." + purpose.name().toLowerCase() + "." + channel.name().toLowerCase();
    }
}
