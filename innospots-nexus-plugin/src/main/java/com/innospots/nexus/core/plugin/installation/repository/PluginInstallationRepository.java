package com.innospots.nexus.core.plugin.installation.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.domain.entity.PluginInstallationEntity;
import com.innospots.nexus.core.plugin.installation.domain.enums.PluginPresence;
import com.innospots.nexus.core.plugin.installation.domain.enums.PluginSourceType;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginDefinitionSnapshot;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginDefinitionSnapshotMapper;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginInstallation;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 安装表领域仓储，维护登记、对账、MISSING 和管理员意图。 */
public final class PluginInstallationRepository {

    private static final Logger logger = LoggerFactory.getLogger(PluginInstallationRepository.class);

    private final PluginInstallationDao dao;

    /**
     * 创建单表仓储。
     *
     * @param dao 安装表 DAO
     * @throws NexusException {@code dao} 为 {@code null} 时
     */
    public PluginInstallationRepository(PluginInstallationDao dao) {
        if (dao == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_PERSISTENCE_FAILED,
                    "plugin installation DAO is required");
        }
        this.dao = dao;
    }

    /**
     * 登记一次有效发现。
     *
     * <p>首次记录按 {@code autoInstall} 决定意图；历史意图保持不变。
     *
     * @param snapshot    插件定义快照
     * @param autoInstall 首次发现时是否自动安装并启用
     * @return 登记后的安装事实
     * @throws NexusException 快照无效或持久化失败时
     */
    public PluginInstallation register(PluginDefinitionSnapshot snapshot, boolean autoInstall) {
        if (snapshot == null) {
            throw persistence("plugin definition snapshot is required");
        }
        LocalDateTime now = LocalDateTime.now();
        PluginInstallationEntity entity = selectByPluginId(snapshot.pluginId());
        if (entity == null) {
            entity = new PluginInstallationEntity();
            entity.setInstallationId(com.innospots.nexus.base.util.IdGenerator.ulid("plg"));
            entity.setPluginId(snapshot.pluginId());
            entity.setPluginVersion(snapshot.version());
            entity.setSourceType(snapshot.sourceType());
            entity.setSourceLocation(snapshot.sourceLocation());
            entity.setPresence(PluginPresence.PRESENT.name());
            entity.setDefinitionSnapshot(PluginDefinitionSnapshotMapper.toJson(snapshot));
            entity.setFirstDiscoveredAt(now);
            entity.setLastDiscoveredAt(now);
            entity.setInstalled(autoInstall);
            entity.setDesiredEnabled(autoInstall);
            if (autoInstall) {
                entity.setInstalledAt(now);
                entity.setEnabledAt(now);
            }
            try {
                insert(entity);
            } catch (NexusException failure) {
                // 唯一索引可能在本次查询后被另一线程抢先写入；重新读取并沿用先写入者的管理意图。
                PluginInstallationEntity concurrent = selectByPluginId(snapshot.pluginId());
                if (concurrent == null) {
                    throw failure;
                }
                return updateRegistered(concurrent, snapshot, now);
            }
            logger.info("Registered new plugin installation: id={}, autoInstall={}",
                    snapshot.pluginId(), autoInstall);
            return toModel(entity);
        }
        logger.debug("Updating plugin installation registration: id={}", snapshot.pluginId());
        return updateRegistered(entity, snapshot, now);
    }

    /** 更新已有登记记录，同时保留管理员安装和启用意图。 */
    private PluginInstallation updateRegistered(
            PluginInstallationEntity entity,
            PluginDefinitionSnapshot snapshot,
            LocalDateTime now
    ) {
        entity.setPluginVersion(snapshot.version());
        entity.setSourceType(snapshot.sourceType());
        entity.setSourceLocation(snapshot.sourceLocation());
        entity.setPresence(PluginPresence.PRESENT.name());
        entity.setDefinitionSnapshot(PluginDefinitionSnapshotMapper.toJson(snapshot));
        entity.setLastDiscoveredAt(now);
        entity.setMissingAt(null);
        validateFact(entity);
        update(entity);
        return toModel(entity);
    }

    /**
     * 将当前目录不存在的历史记录标记为 MISSING。
     *
     * <p>保留安装意图和定义快照。
     *
     * @param pluginIds 当前有效目录中的插件标识
     * @return 本次新标记为 MISSING 的安装事实
     */
    public List<PluginInstallation> markMissing(Collection<String> pluginIds) {
        HashSet<String> present = pluginIds == null ? new HashSet<>() : new HashSet<>(pluginIds);
        List<PluginInstallation> missing = new java.util.ArrayList<>();
        for (PluginInstallationEntity entity : selectAll()) {
            if (!present.contains(entity.getPluginId())
                    && !PluginPresence.MISSING.name().equals(entity.getPresence())) {
                entity.setPresence(PluginPresence.MISSING.name());
                entity.setMissingAt(LocalDateTime.now());
                update(entity);
                missing.add(toModel(entity));
            }
        }
        if (!missing.isEmpty()) {
            logger.info("Marked {} plugin installation(s) as MISSING", missing.size());
        }
        return List.copyOf(missing);
    }

    /**
     * 设置安装和启用意图。
     *
     * @param pluginId       插件标识
     * @param installed      是否已安装
     * @param desiredEnabled 是否期望启用
     * @return 更新后的安装事实
     * @throws NexusException 未安装却期望启用，或安装事实不存在时
     */
    public PluginInstallation setIntent(String pluginId, boolean installed, boolean desiredEnabled) {
        if (!installed && desiredEnabled) {
            throw persistence("uninstalled plugin cannot be desired enabled");
        }
        PluginInstallationEntity entity = requireEntity(pluginId);
        LocalDateTime now = LocalDateTime.now();
        entity.setInstalled(installed);
        entity.setDesiredEnabled(desiredEnabled);
        if (installed && entity.getInstalledAt() == null) {
            entity.setInstalledAt(now);
        }
        if (desiredEnabled) {
            entity.setEnabledAt(now);
        } else {
            entity.setDisabledAt(now);
        }
        validateFact(entity);
        update(entity);
        logger.info("Updated plugin intent: id={}, installed={}, desiredEnabled={}",
                pluginId, installed, desiredEnabled);
        return toModel(entity);
    }

    /**
     * 单独写入最近一次运行状态和错误诊断。
     *
     * @param pluginId     插件标识
     * @param runtimeState 运行状态名称；可为 {@code null}
     * @param error        最近一次错误摘要；可为 {@code null}
     * @return 更新后的安装事实
     * @throws NexusException 安装事实不存在时
     */
    public PluginInstallation updateRuntime(String pluginId, String runtimeState, String error) {
        PluginInstallationEntity entity = requireEntity(pluginId);
        entity.setLastRuntimeState(runtimeState);
        entity.setLastError(error);
        update(entity);
        return toModel(entity);
    }

    /**
     * 查询全部安装事实。
     *
     * @return 不可变安装事实列表
     */
    public List<PluginInstallation> findAll() {
        return selectAll().stream().map(PluginInstallationRepository::toModel).toList();
    }

    /**
     * 按 pluginId 查询安装事实。
     *
     * @param pluginId 插件标识
     * @return 安装事实；不存在时为空
     */
    public Optional<PluginInstallation> find(String pluginId) {
        return Optional.ofNullable(selectByPluginId(pluginId)).map(PluginInstallationRepository::toModel);
    }

    /**
     * 要求安装事实存在。
     *
     * @param pluginId 插件标识
     * @return 安装事实
     * @throws NexusException 安装事实不存在时
     */
    public PluginInstallation require(String pluginId) {
        return toModel(requireEntity(pluginId));
    }

    private PluginInstallationEntity requireEntity(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            throw persistence("pluginId is required");
        }
        PluginInstallationEntity entity = selectByPluginId(pluginId);
        if (entity == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_NOT_INSTALLED,
                    "plugin installation was not found: " + pluginId);
        }
        return entity;
    }

    private static PluginInstallation toModel(PluginInstallationEntity entity) {
        validateFact(entity);
        return new PluginInstallation(
                entity.getInstallationId(), entity.getPluginId(), entity.getPluginVersion(),
                PluginSourceType.from(entity.getSourceType()),
                entity.getSourceLocation(),
                PluginPresence.valueOf(entity.getPresence()),
                entity.isInstalled(), entity.isDesiredEnabled(), entity.getDefinitionSnapshot(),
                entity.getLastRuntimeState(), entity.getLastError(), entity.getFirstDiscoveredAt(),
                entity.getLastDiscoveredAt(), entity.getInstalledAt(), entity.getEnabledAt(),
                entity.getDisabledAt(), entity.getMissingAt());
    }

    private static void validateFact(PluginInstallationEntity entity) {
        if (entity == null || entity.getPluginId() == null || entity.getPluginId().isBlank()
                || entity.getPluginVersion() == null || entity.getSourceType() == null
                || entity.getPresence() == null || entity.getFirstDiscoveredAt() == null
                || entity.getLastDiscoveredAt() == null
                || (!entity.isInstalled() && entity.isDesiredEnabled())) {
            throw persistence("invalid plugin installation fact");
        }
        try {
            PluginPresence.valueOf(entity.getPresence());
            PluginSourceType.from(entity.getSourceType());
        } catch (RuntimeException exception) {
            throw persistence("invalid plugin installation enum value");
        }
    }

    private static NexusException persistence(String message) {
        return NexusException.build(PluginStatusCode.PLUGIN_PERSISTENCE_FAILED, message);
    }

    private PluginInstallationEntity selectByPluginId(String pluginId) {
        return persistenceCall("selectByPluginId", () -> dao.selectByPluginId(pluginId));
    }

    private List<PluginInstallationEntity> selectAll() {
        List<PluginInstallationEntity> result = persistenceCall("selectAll", dao::selectAll);
        if (result == null) {
            throw persistence("plugin installation DAO returned null list");
        }
        return result;
    }

    private void insert(PluginInstallationEntity entity) {
        persistenceRun("insert", () -> dao.insert(entity));
    }

    private void update(PluginInstallationEntity entity) {
        persistenceRun("updateById", () -> dao.updateById(entity));
    }

    private <T> T persistenceCall(String operation, Supplier<T> action) {
        try {
            return action.get();
        } catch (NexusException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_PERSISTENCE_FAILED.fullCode(),
                    "plugin installation " + operation + " failed",
                    exception);
        }
    }

    private void persistenceRun(String operation, Runnable action) {
        persistenceCall(operation, () -> {
            action.run();
            return Boolean.TRUE;
        });
    }
}
