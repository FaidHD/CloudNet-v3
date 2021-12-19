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

package de.dytanic.cloudnet.template.install.execute.defaults;

import com.google.gson.reflect.TypeToken;
import de.dytanic.cloudnet.common.io.FileUtils;
import de.dytanic.cloudnet.template.install.InstallInformation;
import de.dytanic.cloudnet.template.install.execute.InstallStepExecutor;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import lombok.NonNull;

public class ZipFileFilterStepExecutor implements InstallStepExecutor {

  private static final Type COL_STRINGS = TypeToken.getParameterized(Collection.class, String.class).getType();

  @Override
  public @NonNull Set<Path> execute(
    @NonNull InstallInformation info,
    @NonNull Path workingDirectory,
    @NonNull Set<Path> files
  ) throws IOException {
    Collection<String> filesToRemove = info.serviceVersion().properties().get("filteredFiles", COL_STRINGS);
    Collection<Path> jarFiles = files.stream().filter(path -> path.getFileName().toString().endsWith(".jar")).toList();
    if (filesToRemove != null && !filesToRemove.isEmpty()) {
      // remove these files
      jarFiles.forEach(path -> FileUtils.openZipFileSystem(path, fs -> filesToRemove.forEach(file -> {
        var filePath = fs.getPath(file);
        FileUtils.delete(filePath);
      })));
    }
    // continue with the input files
    return files;
  }
}