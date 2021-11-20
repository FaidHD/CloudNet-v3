/*
 * Copyright 2019-2021 CloudNetService team & contributors
 *
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

package eu.cloudnetservice.cloudnet.ext.syncproxy.node;

import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.cluster.sync.DataSyncHandler;
import de.dytanic.cloudnet.common.document.gson.JsonDocument;
import de.dytanic.cloudnet.driver.module.ModuleLifeCycle;
import de.dytanic.cloudnet.driver.module.ModuleTask;
import de.dytanic.cloudnet.driver.module.driver.DriverModule;
import eu.cloudnetservice.cloudnet.ext.syncproxy.config.SyncProxyConfiguration;
import eu.cloudnetservice.cloudnet.ext.syncproxy.node.command.CommandSyncProxy;
import eu.cloudnetservice.cloudnet.ext.syncproxy.node.listener.IncludePluginListener;
import eu.cloudnetservice.cloudnet.ext.syncproxy.node.listener.NodeSyncProxyChannelMessageListener;
import java.nio.file.Files;

public final class CloudNetSyncProxyModule extends DriverModule {

  private static CloudNetSyncProxyModule instance;

  private NodeSyncProxyManagement nodeSyncProxyManagement;

  public CloudNetSyncProxyModule() {
    instance = this;
  }

  public static CloudNetSyncProxyModule getInstance() {
    return CloudNetSyncProxyModule.instance;
  }

  @ModuleTask(order = 127, event = ModuleLifeCycle.LOADED)
  public void convertConfig() {
    if (Files.exists(this.getConfigPath())) {
      // the old config is located in a document with the key "config", extract the actual config
      JsonDocument document = this.readConfig().getDocument("config", null);
      // check if there is an old config
      if (document != null) {
        // write the extracted part to the file
        this.writeConfig(document);
      }
    }
  }

  @ModuleTask(order = 126, event = ModuleLifeCycle.LOADED)
  public void initManagement() {
    // check if we need to create a default config
    if (Files.notExists(this.getConfigPath())) {
      // create default config and write to the file
      this.writeConfig(JsonDocument.newDocument(SyncProxyConfiguration.createDefault("Proxy")));
    }
    // read the config from the file
    SyncProxyConfiguration configuration = this.readConfig().toInstanceOf(SyncProxyConfiguration.class);
    this.nodeSyncProxyManagement = new NodeSyncProxyManagement(this, configuration, this.getRPCFactory());
    // register the SyncProxyManagement to the ServiceRegistry
    this.nodeSyncProxyManagement.registerService(this.getServiceRegistry());
    // sync the config of the module into the cluster
    CloudNet.getInstance().getDataSyncRegistry().registerHandler(
      DataSyncHandler.<SyncProxyConfiguration>builder()
        .key("syncproxy-config")
        .nameExtractor($ -> "SyncProxy Config")
        .convertObject(SyncProxyConfiguration.class)
        .writer(this.nodeSyncProxyManagement::setConfiguration)
        .singletonCollector(this.nodeSyncProxyManagement::getConfiguration)
        .currentGetter($ -> this.nodeSyncProxyManagement.getConfiguration())
        .build());
  }

  @ModuleTask(order = 64, event = ModuleLifeCycle.LOADED)
  public void initListeners() {
    // register the listeners
    this.registerListener(new IncludePluginListener(this.nodeSyncProxyManagement),
      new NodeSyncProxyChannelMessageListener(this.nodeSyncProxyManagement, this.getEventManager()));
  }

  @ModuleTask(order = 60, event = ModuleLifeCycle.LOADED)
  public void registerCommands() {
    // register the syncproxy command to provide config management
    CloudNet.getInstance().getCommandProvider().register(new CommandSyncProxy(this.nodeSyncProxyManagement));
  }
}