package com.innospots.nexus.console.credential.password.operator;

import java.util.Objects;
import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.transaction.Transactional;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.credential.ownership.CredentialOwnershipResolver;
import com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithmRegistry;
import com.innospots.nexus.console.credential.password.algorithm.EncodedCredential;
import com.innospots.nexus.console.credential.password.dao.UserCredentialDao;
import com.innospots.nexus.console.credential.password.domain.entity.UserCredentialEntity;
import com.innospots.nexus.console.credential.password.CredentialKind;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * {@code nx_user_credential} 的读写与证明校验；同一表按 {@link CredentialKind} 存密码、TOTP 等行。
 * 查询均带 {@link ConsoleOwnershipScope} 隔离。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.credential.password.domain.entity.UserCredentialEntity
 * @see com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithmRegistry
 */
public final class UserCredentialOperator {

    private final UserCredentialDao credentialDao;
    private final CredentialAlgorithmRegistry algorithmRegistry;

    /**
     * 注入 DAO 与算法注册表。
     *
     * @param credentialDao       {@code nx_user_credential} Mapper
     * @param algorithmRegistry   密码等可插拔验证算法注册表
     */
    public UserCredentialOperator(UserCredentialDao credentialDao, CredentialAlgorithmRegistry algorithmRegistry) {
        this.credentialDao = Checks.notNull(credentialDao, "credentialDao");
        this.algorithmRegistry = Checks.notNull(algorithmRegistry, "algorithmRegistry");
    }

    /**
     * 加载登录用密码凭据快照。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.password.service.CredentialService} 登录认证与改密前读取当前行。</p>
     *
     * @param realm     平台或租户安全域
     * @param subjectId 用户主体 ID（与 {@code nx_user_credential.subject_id} 一致）
     * @return 存在且归属匹配时返回 opaque 记录
     */
    public Optional<CredentialRecord> findPassword(SecurityRealm realm, String subjectId) {
        return findCredential(realm, CredentialKind.PASSWORD, subjectId);
    }

    /**
     * 按安全域与凭据类型加载 opaque {@link CredentialRecord}。
     * <p>调用场景：TOTP 注册/MFA（{@link CredentialKind#TOTP}）、扩展凭据类型查询。</p>
     *
     * @param realm     安全域
     * @param kind      凭据类型
     * @param subjectId 用户主体 ID
     * @return 匹配行；无行或 {@code subjectId} 空白时为空
     */
    public Optional<CredentialRecord> findCredential(SecurityRealm realm, CredentialKind kind, String subjectId) {
        return find(CredentialOwnershipResolver.ownershipForRealm(realm), kind, subjectId).map(this::toRecord);
    }

    /**
     * 在显式归属下查询实体行（供需要写库字段的内部流程使用）。
     * <p>调用场景：{@link #saveVerifier}、租户级扩展归属 {@link CredentialOwnershipResolver#tenantResourceOwnership}。</p>
     *
     * @param ownership 归属三元组
     * @param kind        凭据类型
     * @param subjectId   用户主体 ID
     * @return 实体；校验失败或不存在时为空
     */
    public Optional<UserCredentialEntity> find(
            ConsoleOwnership ownership,
            CredentialKind kind,
            String subjectId
    ) {
        Checks.notNull(ownership, "ownership");
        Checks.notNull(kind, "kind");
        if (subjectId == null || subjectId.isBlank()) {
            return Optional.empty();
        }
        UserCredentialEntity entity = credentialDao.selectOne(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<UserCredentialEntity>()
                        .eq(UserCredentialEntity::getCredentialKind, kind.name())
                        .eq(UserCredentialEntity::getSubjectId, subjectId),
                ownership));
        if (entity == null) {
            return Optional.empty();
        }
        ConsoleOwnershipScope.assertOwnership(entity, ownership);
        return Optional.of(entity);
    }

    /**
     * 注册或轮换密码哈希（BCrypt 等），明文仅在内存中参与编码。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.password.service.CredentialService} 注册/改密/重置成功后写入。</p>
     *
     * @param ownership   凭据行归属
     * @param subjectId   用户主体 ID
     * @param rawPassword 明文密码（不得落库）
     */
    @Transactional
    public void enrollPassword(ConsoleOwnership ownership, String subjectId, String rawPassword) {
        Objects.requireNonNull(rawPassword, "rawPassword");
        Checks.notBlank(subjectId, "subjectId");
        EncodedCredential encoded = algorithmRegistry.defaultAlgorithm().encode(rawPassword);
        saveVerifier(
                ownership,
                subjectId,
                CredentialKind.PASSWORD,
                encoded.algorithm(),
                encoded.verifier(),
                encoded.verifierParams());
    }

    /**
     * 写入或轮换已编码的验证材料（禁止传入明文密码）。
     * <p>调用场景：TOTP 共享密钥 AES 密文落库（{@link CredentialKind#TOTP}）、自定义算法扩展。</p>
     *
     * @param ownership      凭据行归属
     * @param subjectId      用户主体 ID
     * @param kind           凭据类型
     * @param algorithm      算法 ID
     * @param verifier       opaque 验证材料
     * @param verifierParams 扩展参数（如 TOTP 注册待确认标记）
     */
    @Transactional
    public void saveVerifier(
            ConsoleOwnership ownership,
            String subjectId,
            CredentialKind kind,
            String algorithm,
            String verifier,
            String verifierParams
    ) {
        Checks.notBlank(subjectId, "subjectId");
        Checks.notBlank(algorithm, "algorithm");
        Checks.notBlank(verifier, "verifier");
        UserCredentialEntity entity = find(ownership, kind, subjectId).orElseGet(UserCredentialEntity::new);
        if (entity.getCredentialId() == null) {
            ConsoleOwnershipScope.stamp(entity, ownership);
            entity.setSubjectId(subjectId);
            entity.setCredentialKind(kind.name());
            entity.setCredentialVersion(1);
            entity.setForceReset(false);
            entity.setFailedAttempts(0);
        }
        entity.setAlgorithm(algorithm);
        entity.setVerifier(verifier);
        entity.setVerifierParams(verifierParams);
        if (entity.getCredentialId() == null) {
            credentialDao.insert(entity);
        } else {
            entity.setCredentialVersion(entity.getCredentialVersion() + 1);
            credentialDao.updateById(entity);
        }
    }

    /**
     * 更新密码行的锁定、失败次数与可选 verifier 轮换结果。
     * <p>调用场景：{@link com.innospots.nexus.console.auth.service.AuthFacade} 登录失败累加次数；
     * {@link com.innospots.nexus.console.credential.password.service.CredentialService#resetPassword} 清除锁定。</p>
     *
     * @param realm  安全域
     * @param record 含 {@code subjectId} 与待持久化状态字段的记录
     */
    @Transactional
    public void saveState(SecurityRealm realm, CredentialRecord record) {
        ConsoleOwnership ownership = CredentialOwnershipResolver.ownershipForRealm(realm);
        UserCredentialEntity entity = find(ownership, CredentialKind.PASSWORD, record.subjectId())
                .orElseThrow(() -> new IllegalStateException("Credential not found: " + record.subjectId()));
        entity.setFailedAttempts(record.failedAttempts() == null ? 0 : record.failedAttempts());
        entity.setLockedUntil(record.lockedUntil());
        entity.setForceReset(Boolean.TRUE.equals(record.forceReset()));
        if (record.verifier() != null) {
            entity.setAlgorithm(record.algorithm());
            entity.setVerifier(record.verifier());
            entity.setVerifierParams(record.verifierParams());
            entity.setCredentialVersion(record.credentialVersion() == null
                    ? entity.getCredentialVersion()
                    : record.credentialVersion());
        }
        credentialDao.updateById(entity);
    }

    /**
     * 清空 {@code verifierParams}（例如 TOTP 注册确认后去掉待确认标记）。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.totp.service.DefaultTotpEnrollmentService#confirmEnrollment}。</p>
     *
     * @param realm     安全域
     * @param kind      凭据类型
     * @param subjectId 用户主体 ID
     */
    @Transactional
    public void clearVerifierParams(SecurityRealm realm, CredentialKind kind, String subjectId) {
        ConsoleOwnership ownership = CredentialOwnershipResolver.ownershipForRealm(realm);
        UserCredentialEntity entity = find(ownership, kind, subjectId)
                .orElseThrow(() -> new IllegalStateException("Credential not found: " + subjectId));
        entity.setVerifierParams(null);
        credentialDao.updateById(entity);
    }

    /**
     * 删除指定类型的凭据行。
     * <p>调用场景：用户解绑 TOTP（{@link com.innospots.nexus.console.credential.totp.service.TotpEnrollmentService#removeEnrollment}）。</p>
     *
     * @param realm     安全域
     * @param kind      凭据类型
     * @param subjectId 用户主体 ID
     */
    @Transactional
    public void deleteCredential(SecurityRealm realm, CredentialKind kind, String subjectId) {
        ConsoleOwnership ownership = CredentialOwnershipResolver.ownershipForRealm(realm);
        find(ownership, kind, subjectId)
                .ifPresent(entity -> credentialDao.deleteById(entity.getCredentialId()));
    }

    /**
     * 用记录上的算法校验明文证明（密码或算法支持的证明类型）。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.password.service.CredentialService#authenticate}、
     * 已登录用户改密时校验旧密码。</p>
     *
     * @param record   已加载的凭据记录（含 algorithm 与 verifier）
     * @param rawProof 用户提交的明文证明
     * @return 校验通过时为 {@code true}
     */
    public boolean verify(CredentialRecord record, String rawProof) {
        return algorithmRegistry.require(record.algorithm()).verify(
                record.verifier(),
                record.verifierParams(),
                rawProof);
    }

    private CredentialRecord toRecord(UserCredentialEntity entity) {
        return new CredentialRecord(
                entity.getSubjectId(),
                entity.getCredentialKind(),
                entity.getAlgorithm(),
                entity.getVerifier(),
                entity.getVerifierParams(),
                entity.getCredentialVersion(),
                entity.getFailedAttempts(),
                entity.getLockedUntil(),
                entity.getForceReset(),
                entity.getExpiredAt());
    }
}
