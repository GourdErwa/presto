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
package com.facebook.presto.server.catalog;


import com.facebook.airlift.discovery.client.Announcer;
import com.facebook.airlift.discovery.client.ServiceAnnouncement;
import com.facebook.airlift.log.Logger;
import com.facebook.presto.client.catalog.*;
import com.facebook.presto.connector.ConnectorManager;
import com.facebook.presto.execution.scheduler.NodeSchedulerConfig;
import com.facebook.presto.metadata.Catalog;
import com.facebook.presto.metadata.CatalogManager;
import com.facebook.presto.metadata.DiscoveryNodeManager;
import com.facebook.presto.metadata.InternalNode;
import com.facebook.presto.server.CoordinatorModule;
import com.facebook.presto.server.ServerConfig;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.facebook.airlift.discovery.client.ServiceAnnouncement.serviceAnnouncement;
import static com.facebook.presto.server.security.RoleType.ADMIN;
import static com.facebook.presto.server.security.RoleType.USER;
import static java.util.Objects.requireNonNull;

/**
 * 二次开发
 * 连接器管理，需要在 {@link CoordinatorModule}setup(Binder) 方法中注入
 * <pre>jaxrsBinder(binder).bind(CatalogResource.class);</pre>
 *
 * @author Li.Wei by 2022/11/7
 * @see CatalogResourceClientV1
 */
@Path("/v1/catalog")
@RolesAllowed({USER, ADMIN})
public class CatalogResource {
    private static final Logger LOG = Logger.get(CatalogResource.class);

    private final CatalogManager catalogManager;
    private final ConnectorManager connectorManager;
    private final DiscoveryNodeManager discoveryNodeManager;

    private final Announcer announcer;
    private final ServerConfig serverConfig;
    private final NodeSchedulerConfig schedulerConfig;

    @Inject
    public CatalogResource(CatalogManager catalogManager,
                           ConnectorManager connectorManager,
                           DiscoveryNodeManager discoveryNodeManager,
                           Announcer announcer,
                           ServerConfig serverConfig,
                           NodeSchedulerConfig schedulerConfig
    ) {
        this.catalogManager = requireNonNull(catalogManager, "catalogManager is null");
        this.connectorManager = requireNonNull(connectorManager, "connectorManager is null");
        this.discoveryNodeManager = requireNonNull(discoveryNodeManager, "discoveryNodeManager is null");

        this.announcer = requireNonNull(announcer, "announcer is null");
        this.serverConfig = requireNonNull(serverConfig, "serverConfig is null");
        this.schedulerConfig = requireNonNull(schedulerConfig, "schedulerConfig is null");
    }

    // for Test
    @GET
    @Path("test/")
    public void getAllTest(@Context UriInfo uriInfo,
                           @Suspended AsyncResponse asyncResponse) {
        LOG.info("-- exec getAllTest --");
        // 模拟测试数据

        final Map<String, String> map = ImmutableMap.of("k1", "v1", "k2", "v2");
        final CatalogEntityVo catalogEntityVo = new CatalogEntityVo("test-catalog", "mysql", map, LocalDateTime.now());

        InternalNodeVo internalNodeVo = new InternalNodeVo("ffff-ffff", "http://IP:18080", "v1", true);
        CatalogEntityActiveNode node = new CatalogEntityActiveNode(catalogEntityVo, Lists.newArrayList(internalNodeVo));

        asyncResponse.resume(Response.ok(
                new InternalResult<>(Lists.newArrayList(node))
        ).build());
    }

    @GET
    public void getAll(@Context UriInfo uriInfo,
                       @Suspended AsyncResponse asyncResponse) {
        LOG.info("-- exec getAll --");
        asyncResponse.resume(Response.ok(
                new InternalResult<>(catalogManager.getCatalogs().stream()
                        .map(this::biCatalogInfo)
                        .collect(Collectors.toList()))
        ).build());
    }

    @GET
    @Path("info/{catalogName}")
    public void getInfo(
            @PathParam("catalogName") String catalogName,
            @Context UriInfo uriInfo,
            @Suspended AsyncResponse asyncResponse) {
        LOG.info("-- exec getInfo catalogName:{}--", catalogName);
        asyncResponse.resume(Response.ok(new InternalResult<>(biCatalogInfo(catalogName))).build());
    }

    @GET
    @Path("info_like/{catalogNamePrefix}")
    public void getInfoByPrefix(
            @PathParam("catalogNamePrefix") String catalogNamePrefix,
            @Context UriInfo uriInfo,
            @Suspended AsyncResponse asyncResponse) {
        LOG.info("-- exec getInfoByPrefix catalogNamePrefix:{}--", catalogNamePrefix);
        asyncResponse.resume(Response.ok(
                new InternalResult<>(catalogManager.getCatalogs().stream()
                        .filter(s -> CatalogEntity.checkPrefix(s.getCatalogName(), catalogNamePrefix))
                        .map(this::biCatalogInfo)
                        .collect(Collectors.toList()))
        ).build());
    }

    @DELETE
    @Path("{catalogName}")
    public void delete(
            @PathParam("catalogName") String catalogName,
            @Context UriInfo uriInfo,
            @Suspended AsyncResponse asyncResponse) {
        LOG.info("-- exec delete catalogName:{}--", catalogName);
        try {
            connectorManager.dropConnection(catalogName);
            refreshConnectorIds();
            asyncResponse.resume(Response.ok().build());
        } catch (Exception e) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build());
            LOG.error("delete catalogName [{}], error:{}", catalogName, e.getMessage());
        }
    }

    /**
     * 添加 catalog ，存在时执行替换操作
     *
     * @param catalogEntity catalogEntity
     */
    @POST
    @Path("/add")
    public void createCatalog(List<CatalogEntity> catalogEntity,
                              @Suspended AsyncResponse asyncResponse) {
        requireNonNull(catalogEntity, "catalogEntity is null");
        LOG.info("-- exec createCatalog catalogEntity:{}--", catalogEntity.toString());
        // 批量创建，存在则移除后新建
        final List<CatalogLogVo> r = catalogEntity.stream()
                .map(catalog -> {
                    final CatalogLogVo logVo = CatalogLogVo.create(catalog);
                    final String catalogName = catalog.getCatalogName();
                    try {
                        if (catalogManager.getCatalog(catalogName).isPresent()) {
                            connectorManager.dropConnection(catalogName);
                            logVo.appendMsg("存在该 catalog ，移除完成。");
                        }
                        connectorManager.createConnection(catalogName, catalog.getConnectorName(), catalog.getProperties());
                        logVo.appendMsg("创建 catalog 完成。");
                    } catch (Exception e) {
                        logVo.setState(false).appendMsg("创建过程异常：" + e.getMessage());
                        LOG.error("create catalogName [{}], error:{}", catalogName, e.getMessage());
                    }
                    return logVo;
                }).collect(Collectors.toList());
        refreshConnectorIds();
        asyncResponse.resume(Response.ok().entity(r).build());
    }

    /**
     * 通过 CatalogName 获取对应 {@link CatalogEntityActiveNode} 信息，仅返回 bi_* catalogName相关信息
     *
     * @param catalogName catalogName
     * @return CatalogEntityActiveNode
     */
    private CatalogEntityActiveNode biCatalogInfo(String catalogName) {
        return catalogManager.getCatalog(catalogName).map(this::biCatalogInfo).orElse(null);
    }

    private CatalogEntityActiveNode biCatalogInfo(Catalog catalog) {
        return new CatalogEntityActiveNode(CatalogResource.create(catalog),
                discoveryNodeManager.getActiveConnectorNodes(catalog.getConnectorId())
                        .stream().map(CatalogResource::create).collect(Collectors.toList()));
    }

    /**
     * 复制 PrestoServer#updateConnectorIds 方法修改，支持动态更新。
     * 广播刷新后连接信息
     * TODO 监听器注册待确定是否需要动态管理
     */
    private void refreshConnectorIds() {
        ServiceAnnouncement announcement = getPrestoAnnouncement(announcer.getServiceAnnouncements());
        Set<String> connectorIds = new LinkedHashSet<>();
        List<Catalog> catalogs = catalogManager.getCatalogs();
        // if this is a dedicated coordinator, only add jmx
        if (serverConfig.isCoordinator() && !schedulerConfig.isIncludeCoordinator()) {
            catalogs.stream()
                    .map(Catalog::getConnectorId)
                    .filter(connectorId -> connectorId.getCatalogName().equals("jmx"))
                    .map(Object::toString)
                    .forEach(connectorIds::add);
        } else {
            catalogs.stream()
                    .map(Catalog::getConnectorId)
                    .map(Object::toString)
                    .forEach(connectorIds::add);
        }

        // build announcement with updated sources
        ServiceAnnouncement.ServiceAnnouncementBuilder builder = serviceAnnouncement(announcement.getType());
        for (Map.Entry<String, String> entry : announcement.getProperties().entrySet()) {
            if (!entry.getKey().equals("connectorIds")) {
                builder.addProperty(entry.getKey(), entry.getValue());
            }
        }
        builder.addProperty("connectorIds", Joiner.on(',').join(connectorIds));

        // update announcement
        announcer.removeServiceAnnouncement(announcement.getId());
        announcer.addServiceAnnouncement(builder.build());
        //强制出发广播
        announcer.forceAnnounce();
    }

    private static ServiceAnnouncement getPrestoAnnouncement(Set<ServiceAnnouncement> announcements) {
        for (ServiceAnnouncement announcement : announcements) {
            if (announcement.getType().equals("presto")) {
                return announcement;
            }
        }
        throw new IllegalArgumentException("Presto announcement not found: " + announcements);
    }

    public static CatalogEntityVo create(Catalog catalog) {
        return new CatalogEntityVo(
                catalog.getCatalogName(),
                catalog.getConnectorName(),
                catalog.getConfig(),
                catalog.getCreateTime());
    }

    public static InternalNodeVo create(InternalNode node) {
        return new InternalNodeVo(node.getNodeIdentifier(), node.getInternalUri().toString(), node.getVersion(), node.isCoordinator());
    }
}
