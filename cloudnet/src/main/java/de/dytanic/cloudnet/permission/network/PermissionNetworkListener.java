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

package de.dytanic.cloudnet.permission.network;

import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.IEventManager;
import de.dytanic.cloudnet.driver.event.events.channel.ChannelMessageReceiveEvent;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionAddGroupEvent;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionAddUserEvent;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionDeleteGroupEvent;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionDeleteUserEvent;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionSetGroupsEvent;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionUpdateGroupEvent;
import de.dytanic.cloudnet.driver.event.events.permission.PermissionUpdateUserEvent;
import de.dytanic.cloudnet.driver.network.def.NetworkConstants;
import de.dytanic.cloudnet.driver.permission.PermissionGroup;
import de.dytanic.cloudnet.driver.permission.PermissionUser;
import de.dytanic.cloudnet.permission.NodePermissionManagement;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public final class PermissionNetworkListener {

  private final IEventManager eventManager;
  private final NodePermissionManagement permissionManagement;

  public PermissionNetworkListener(@NotNull IEventManager eventManager, @NotNull NodePermissionManagement management) {
    this.eventManager = eventManager;
    this.permissionManagement = management;
  }

  @EventListener
  public void handleChannelMessage(@NotNull ChannelMessageReceiveEvent event) {
    if (event.getChannel().equals(NetworkConstants.INTERNAL_MSG_CHANNEL)
      && event.getMessage() != null
      && event.getMessage().startsWith("permissions_")) {
      // permission message - handler
      switch (event.getMessage().replaceFirst("permissions_", "")) {
        // user add
        case "add_user":
          this.eventManager.callEvent(new PermissionAddUserEvent(
            this.permissionManagement,
            event.getContent().readObject(PermissionUser.class)));
          break;
        // user update
        case "update_user":
          this.eventManager.callEvent(new PermissionUpdateUserEvent(
            this.permissionManagement,
            event.getContent().readObject(PermissionUser.class)));
          break;
        // user remove
        case "delete_user":
          this.eventManager.callEvent(new PermissionDeleteUserEvent(
            this.permissionManagement,
            event.getContent().readObject(PermissionUser.class)));
          break;
        // group add
        case "add_group": {
          // read the group
          PermissionGroup group = event.getContent().readObject(PermissionGroup.class);
          // handle
          this.permissionManagement.addGroupSilently(group);
          this.eventManager.callEvent(new PermissionAddGroupEvent(this.permissionManagement, group));
        }
        break;
        // group update
        case "update_group": {
          // read the group
          PermissionGroup group = event.getContent().readObject(PermissionGroup.class);
          // handle
          this.permissionManagement.updateGroupSilently(group);
          this.eventManager.callEvent(new PermissionUpdateGroupEvent(this.permissionManagement, group));
        }
        break;
        // group delete
        case "delete_group": {
          // read the group
          PermissionGroup group = event.getContent().readObject(PermissionGroup.class);
          // handle
          this.permissionManagement.deleteGroupSilently(group);
          this.eventManager.callEvent(new PermissionDeleteGroupEvent(this.permissionManagement, group));
        }
        break;
        // group set
        case "set_groups": {
          // read the group
          Collection<PermissionGroup> groups = event.getContent().readObject(PermissionGroup.COL_GROUPS);
          // handle
          this.permissionManagement.setGroupsSilently(groups);
          this.eventManager.callEvent(new PermissionSetGroupsEvent(this.permissionManagement, groups));
        }
        break;
        default:
          throw new IllegalArgumentException("Unhandled permission message " + event.getMessage());
      }
    }
  }
}