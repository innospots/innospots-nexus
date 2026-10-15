package com.innospots.nexus.sample.spring.platform.dictionary;

import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer;
import com.innospots.nexus.sample.spring.platform.support.PlatformDatabaseJsonSnapshot;
import com.innospots.nexus.sample.spring.platform.support.PlatformEndpointHttpClient;
import com.innospots.nexus.sample.spring.platform.support.PlatformIntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 与 {@code dictionary-type-list.yaml} 对齐：类型分页/详情/选项/状态，及 items 接口完整维护字典项。
 */
@SpringBootTest(
        classes = SamplePlatformConsoleServer.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=" + PlatformIntegrationTestSupport.H2_URL_DICTIONARY_IT,
        "spring.config.import=" + PlatformIntegrationTestSupport.SPRING_CONFIG_IMPORT
})
class PlatformDictionaryEndpointIntegrationTest {

    private static final String OUTPUT_DIR = "sample-platform-dictionary";

    private static final String TYPE_CODE = "it_region";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PlatformEndpointHttpClient http;

    @BeforeEach
    void setUp() {
        PlatformIntegrationTestSupport.cleanDictionary(jdbcTemplate);
        http = new PlatformEndpointHttpClient(port);
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "00-clean",
                Map.of("nx_dictionary_type", "type_code", "nx_dictionary_item", "type_code"));
    }

    @Test
    void dictionaryEndpointsMatchConsoleUiFlows() throws Exception {
        String createTypeBody = """
                {
                  "typeCode": "it_region",
                  "typeName": "IT Region",
                  "sortOrder": 10
                }
                """;
        HttpResponse<String> createdType = http.postJson(PlatformConstant.DICTIONARY_TYPES_PATH, createTypeBody);
        assertThat(createdType.statusCode()).as(createdType.body()).isEqualTo(200);
        assertThat(createdType.body()).contains("it_region");
        assertThat(createdType.body()).contains("\"securityRealm\":\"PLATFORM\"");
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "01-type-created",
                Map.of("nx_dictionary_type", "type_code"));

        String typeId = jdbcTemplate.queryForObject(
                "SELECT dictionary_type_id FROM nx_dictionary_type WHERE type_code = ?",
                String.class,
                TYPE_CODE);

        HttpResponse<String> gotType = http.get(PlatformConstant.DICTIONARY_TYPES_PATH + "/" + typeId);
        assertThat(gotType.statusCode()).isEqualTo(200);
        assertThat(gotType.body()).contains("IT Region");

        String typePagePath = PlatformConstant.DICTIONARY_TYPES_PATH
                + "?pageNo=1&pageSize=20&status=ENABLED&input="
                + URLEncoder.encode("it_region", StandardCharsets.UTF_8);
        HttpResponse<String> typePage = http.get(typePagePath);
        assertThat(typePage.statusCode()).as(typePage.body()).isEqualTo(200);
        assertThat(typePage.body()).contains(typeId);
        assertThat(typePage.body()).contains("\"total\"");

        HttpResponse<String> typeOptions = http.get(PlatformConstant.DICTIONARY_TYPES_PATH + "/options?status=ENABLED");
        assertThat(typeOptions.statusCode()).as(typeOptions.body()).isEqualTo(200);
        assertThat(typeOptions.body()).contains(TYPE_CODE);

        String updateTypeBody = """
                {
                  "typeName": "IT Region Updated",
                  "sortOrder": 20
                }
                """;
        HttpResponse<String> updatedType = http.putJson(
                PlatformConstant.DICTIONARY_TYPES_PATH + "/" + typeId,
                updateTypeBody);
        assertThat(updatedType.statusCode()).as(updatedType.body()).isEqualTo(200);

        String itemsPath = PlatformConstant.DICTIONARY_ITEMS_PATH.replace("{typeCode}", TYPE_CODE);
        String createItemBody = """
                {
                  "itemValue": "east",
                  "itemName": "East",
                  "sortOrder": 1
                }
                """;
        HttpResponse<String> createdItem = http.postJson(itemsPath, createItemBody);
        assertThat(createdItem.statusCode()).as(createdItem.body()).isEqualTo(200);
        String itemId = jdbcTemplate.queryForObject(
                "SELECT dictionary_item_id FROM nx_dictionary_item WHERE type_code = ? AND item_value = ?",
                String.class,
                TYPE_CODE,
                "east");

        HttpResponse<String> pagedItems = http.get(itemsPath + "?pageNo=1&pageSize=20");
        assertThat(pagedItems.statusCode()).isEqualTo(200);
        assertThat(pagedItems.body()).contains("east");

        String updateItemBody = """
                {
                  "itemName": "East Updated",
                  "sortOrder": 2
                }
                """;
        HttpResponse<String> updatedItem = http.putJson(itemsPath + "/" + itemId, updateItemBody);
        assertThat(updatedItem.statusCode()).as(updatedItem.body()).isEqualTo(200);
        assertThat(updatedItem.body()).contains("East Updated");

        HttpResponse<String> disabledItem = http.putJson(
                itemsPath + "/" + itemId + "/status",
                "{\"status\":\"DISABLED\"}");
        assertThat(disabledItem.statusCode()).as(disabledItem.body()).isEqualTo(200);

        HttpResponse<String> enabledItem = http.putJson(
                itemsPath + "/" + itemId + "/status",
                "{\"status\":\"ENABLED\"}");
        assertThat(enabledItem.statusCode()).as(enabledItem.body()).isEqualTo(200);

        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "02-item-updated",
                Map.of("nx_dictionary_type", "type_code", "nx_dictionary_item", "item_value"));

        HttpResponse<String> disabledType = http.putJson(
                PlatformConstant.DICTIONARY_TYPES_PATH + "/" + typeId + "/status",
                "{\"status\":\"DISABLED\"}");
        assertThat(disabledType.statusCode()).as(disabledType.body()).isEqualTo(200);

        String disabledPagePath = PlatformConstant.DICTIONARY_TYPES_PATH + "?pageNo=1&pageSize=20&status=DISABLED";
        HttpResponse<String> disabledTypePage = http.get(disabledPagePath);
        assertThat(disabledTypePage.statusCode()).isEqualTo(200);
        assertThat(disabledTypePage.body()).contains(typeId);

        HttpResponse<String> deletedItem = http.delete(itemsPath + "/" + itemId);
        assertThat(deletedItem.statusCode()).as(deletedItem.body()).isEqualTo(200);

        HttpResponse<String> deletedType = http.delete(PlatformConstant.DICTIONARY_TYPES_PATH + "/" + typeId);
        assertThat(deletedType.statusCode()).as(deletedType.body()).isEqualTo(200);

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nx_dictionary_type", Long.class)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nx_dictionary_item", Long.class)).isZero();
        PlatformDatabaseJsonSnapshot.logAndPersist(
                jdbcTemplate,
                OUTPUT_DIR,
                "03-after-delete",
                Map.of("nx_dictionary_type", "type_code", "nx_dictionary_item", "type_code"));
    }
}
