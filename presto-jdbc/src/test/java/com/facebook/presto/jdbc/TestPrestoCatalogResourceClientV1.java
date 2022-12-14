/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.jdbc;

import com.facebook.airlift.log.Logging;
import com.facebook.presto.client.catalog.CatalogResourceClientV1;
import com.facebook.presto.client.catalog.CatalogEntityActiveNode;
import com.facebook.presto.client.catalog.InternalResult;
import com.facebook.presto.metadata.Catalog;
import com.facebook.presto.metadata.SessionPropertyManager;
import com.facebook.presto.plugin.blackhole.BlackHolePlugin;
import com.facebook.presto.server.testing.TestingPrestoServer;
import com.facebook.presto.tpch.TpchPlugin;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.facebook.presto.jdbc.TestPrestoDriver.closeQuietly;
import static com.facebook.presto.jdbc.TestPrestoDriver.waitForNodeRefresh;
import static com.facebook.presto.testing.TestingSession.TESTING_CATALOG;
import static com.facebook.presto.testing.TestingSession.createBogusTestingCatalog;
import static com.facebook.presto.tests.AbstractTestQueries.TEST_CATALOG_PROPERTIES;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

/**
 * NORN 二次开发。测试类
 */
public class TestPrestoCatalogResourceClientV1 {
    private static final String TEST_CATALOG = "test_catalog";
    private TestingPrestoServer server;

    @BeforeClass
    public void setup()
            throws Exception {
        Logging.initialize();
        server = new TestingPrestoServer();
        server.installPlugin(new TpchPlugin());
        server.createCatalog(TEST_CATALOG, "tpch");
        server.installPlugin(new BlackHolePlugin());
        server.createCatalog("blackhole", "blackhole");
        Catalog bogusTestingCatalog = createBogusTestingCatalog(TESTING_CATALOG);
        server.getCatalogManager().registerCatalog(bogusTestingCatalog);
        SessionPropertyManager sessionPropertyManager = server.getMetadata().getSessionPropertyManager();
        sessionPropertyManager.addConnectorSessionProperties(bogusTestingCatalog.getConnectorId(), TEST_CATALOG_PROPERTIES);
        waitForNodeRefresh(server);
        setupTestTables();
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        closeQuietly(server);
    }

    private void setupTestTables()
            throws SQLException {
        try (Connection connection = createConnection("blackhole", "blackhole");
             Statement statement = connection.createStatement()) {
            assertEquals(statement.executeUpdate("CREATE SCHEMA blackhole.blackhole"), 0);
            assertEquals(statement.executeUpdate("CREATE TABLE test_table (x bigint)"), 0);

            assertEquals(statement.executeUpdate("CREATE TABLE slow_test_table (x bigint) " +
                    "WITH (" +
                    "   split_count = 1, " +
                    "   pages_per_split = 1, " +
                    "   rows_per_page = 1, " +
                    "   page_processing_delay = '1m'" +
                    ")"), 0);
        }
    }

    private Connection createConnection(String catalog, String schema)
            throws SQLException {
        String url = format("jdbc:presto://%s/%s/%s", server.getAddress(), catalog, schema);
        return DriverManager.getConnection(url, "test", null);
    }

    @Test
    public void testDriverManager() throws Exception {

        try (Connection connection = createConnection("blackhole", "blackhole")) {
            final CatalogResourceClientV1 catalogResourceClientV1 = ((PrestoConnection) connection).getCatalogClientV1();
            final InternalResult<List<CatalogEntityActiveNode>> allBiConnectorInfo = catalogResourceClientV1.getAllTest();
            System.out.println(allBiConnectorInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
