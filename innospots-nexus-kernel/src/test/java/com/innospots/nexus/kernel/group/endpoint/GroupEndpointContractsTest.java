package com.innospots.nexus.kernel.group.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.kernel.group.domain.request.GroupCreateRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupLeaderUpdateRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupMemberAssignRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupMemberPageRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupMemberTagAddRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupOrderRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupStatusUpdateRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupTreeRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupUpdateRequest;
import com.innospots.nexus.kernel.group.domain.request.TagCreateRequest;
import com.innospots.nexus.kernel.group.domain.request.TagStatusUpdateRequest;
import com.innospots.nexus.kernel.group.domain.request.TagUpdateRequest;
import com.innospots.nexus.kernel.group.domain.vo.GroupMemberTagVo;
import com.innospots.nexus.kernel.group.domain.vo.GroupMemberVo;
import com.innospots.nexus.kernel.group.domain.vo.GroupOptionVo;
import com.innospots.nexus.kernel.group.domain.vo.GroupVo;
import com.innospots.nexus.kernel.group.domain.vo.TagVo;
import com.innospots.nexus.kernel.user.domain.enums.UserStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupEndpointContractsTest {

    @Test
    void groupEndpointOwnsHierarchyLifecycle() throws NoSuchMethodException {
        assertEndpoint(GroupEndpoint.class, "/console/groups");
        assertHttpMethod(GroupEndpoint.class, "listGroupTree", GET.class, GroupTreeRequest.class);
        assertHttpMethod(GroupEndpoint.class, "getGroup", GET.class, String.class);
        assertHttpMethod(GroupEndpoint.class, "createGroup", POST.class, GroupCreateRequest.class);
        assertHttpMethod(GroupEndpoint.class, "updateGroup", PUT.class,
                String.class, GroupUpdateRequest.class);
        assertHttpMethod(GroupEndpoint.class, "updateGroupStatus", PUT.class,
                String.class, GroupStatusUpdateRequest.class);
        assertHttpMethod(GroupEndpoint.class, "reorderGroups", PUT.class, GroupOrderRequest.class);
        assertHttpMethod(GroupEndpoint.class, "deleteGroup", DELETE.class, String.class);
        assertHttpMethod(GroupEndpoint.class, "listGroupOptions", GET.class);
    }

    @Test
    void memberTagEndpointsRemainSeparateBoundaries() throws NoSuchMethodException {
        assertEndpoint(GroupMemberEndpoint.class, "/console/groups/{groupId}/members");
        assertHttpMethod(GroupMemberEndpoint.class, "pageGroupMembers", GET.class,
                String.class, GroupMemberPageRequest.class);
        assertHttpMethod(GroupMemberEndpoint.class, "assignGroupMembers", PUT.class,
                String.class, GroupMemberAssignRequest.class);
        assertHttpMethod(GroupMemberEndpoint.class, "removeGroupMember", DELETE.class,
                String.class, String.class);
        assertHttpMethod(GroupMemberEndpoint.class, "updateGroupLeader", PUT.class,
                String.class, String.class, GroupLeaderUpdateRequest.class);

        assertEndpoint(TagEndpoint.class, "/console/tags");
        assertHttpMethod(TagEndpoint.class, "listTags", GET.class, BasicStatus.class);
        assertHttpMethod(TagEndpoint.class, "createTag", POST.class, TagCreateRequest.class);
        assertHttpMethod(TagEndpoint.class, "updateTag", PUT.class,
                String.class, TagUpdateRequest.class);
        assertHttpMethod(TagEndpoint.class, "updateTagStatus", PUT.class,
                String.class, TagStatusUpdateRequest.class);
        assertHttpMethod(TagEndpoint.class, "deleteTag", DELETE.class, String.class);

        assertEndpoint(GroupMemberTagEndpoint.class,
                "/console/groups/{groupId}/members/{userId}/tags");
        assertHttpMethod(GroupMemberTagEndpoint.class, "listMemberTags", GET.class,
                String.class, String.class);
        assertHttpMethod(GroupMemberTagEndpoint.class, "addMemberTags", POST.class,
                String.class, String.class, GroupMemberTagAddRequest.class);
        assertHttpMethod(GroupMemberTagEndpoint.class, "removeMemberTag", DELETE.class,
                String.class, String.class, String.class);
    }

    @Test
    void requestsAndViewsUseImmutableRecords() {
        assertRecordComponents(GroupCreateRequest.class,
                "parentId", "groupCode", "groupName", "description", "sortOrder");
        assertRecordComponents(GroupUpdateRequest.class,
                "parentId", "groupName", "description", "sortOrder");
        assertRecordComponents(GroupStatusUpdateRequest.class, "status");
        assertRecordComponents(GroupOrderRequest.class, "parentId", "groupIds");
        assertRecordComponents(GroupTreeRequest.class, "input", "status");
        assertRecordComponents(GroupMemberPageRequest.class,
                "input", "leader", "pageNo", "pageSize");
        assertRecordComponents(GroupMemberAssignRequest.class, "userIds");
        assertRecordComponents(GroupLeaderUpdateRequest.class, "leader");
        assertRecordComponents(TagCreateRequest.class, "tagName", "description");
        assertRecordComponents(TagUpdateRequest.class, "tagName", "description");
        assertRecordComponents(TagStatusUpdateRequest.class, "status");
        assertRecordComponents(GroupMemberTagAddRequest.class, "tagNames");

        assertThat(GroupVo.class.isRecord()).isTrue();
        assertThat(GroupOptionVo.class.isRecord()).isTrue();
        assertThat(GroupMemberVo.class.isRecord()).isTrue();
        assertThat(TagVo.class.isRecord()).isTrue();
        assertThat(GroupMemberTagVo.class.isRecord()).isTrue();
    }

    @Test
    void collectionContractsDefensivelyCopyInput() {
        assertThat(new GroupOrderRequest(null, null).groupIds()).isEmpty();
        assertThat(new GroupMemberAssignRequest(null).userIds()).isEmpty();
        assertThat(new GroupMemberTagAddRequest(null).tagNames()).isEmpty();
        assertThat(sampleGroupVo().children()).isEmpty();
        assertThat(sampleMemberVo().tags()).isEmpty();
    }

    @Test
    void endpointMethodsRemainExplicitlyUnimplemented() {
        GroupEndpoint groupEndpoint = new GroupEndpoint();
        GroupMemberEndpoint memberEndpoint = new GroupMemberEndpoint();
        TagEndpoint tagEndpoint = new TagEndpoint();
        GroupMemberTagEndpoint memberTagEndpoint = new GroupMemberTagEndpoint();

        assertThatThrownBy(() -> groupEndpoint.listGroupTree(new GroupTreeRequest()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> memberEndpoint.assignGroupMembers(
                "group-1", new GroupMemberAssignRequest(List.of("user-1"))))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> tagEndpoint.createTag(new TagCreateRequest("reviewer", null)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> memberTagEndpoint.addMemberTags(
                "group-1", "user-1", new GroupMemberTagAddRequest(List.of("reviewer"))))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static GroupVo sampleGroupVo() {
        return new GroupVo(
                "group-1", null, "platform", "Platform", null,
                BasicStatus.ENABLED, 1, false, 0L, 0L, null, null, null);
    }

    private static GroupMemberVo sampleMemberVo() {
        return new GroupMemberVo(
                "user-1", "alice", "Alice", null, null, null,
                UserStatus.ACTIVE, false, null, null);
    }

    private static void assertEndpoint(Class<?> endpointType, String path) {
        assertThat(endpointType.isInterface()).isFalse();
        assertThat(endpointType.getAnnotation(Path.class).value()).isEqualTo(path);
    }

    private static void assertHttpMethod(
            Class<?> endpointType,
            String methodName,
            Class<? extends java.lang.annotation.Annotation> httpAnnotation,
            Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        Method method = endpointType.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(httpAnnotation)).isNotNull();
    }

    private static void assertRecordComponents(Class<?> recordType, String... names) {
        assertThat(recordType.isRecord()).isTrue();
        assertThat(Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(names);
    }
}
