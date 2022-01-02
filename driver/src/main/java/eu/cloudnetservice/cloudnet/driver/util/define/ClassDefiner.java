/*
 * Copyright 2019-2022 CloudNetService team & contributors
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

package eu.cloudnetservice.cloudnet.driver.util.define;

import lombok.NonNull;

/**
 * Internal utility class to define classes in the runtime.
 *
 * @author Pasqual K.
 * @since 1.0
 */
@FunctionalInterface
public interface ClassDefiner {

  /**
   * Defines the given class {@code bytecode} and return the constructed class object.
   *
   * @param name     the name of the class to construct.
   * @param parent   the parent class of this class as we are assuming to define an anonymous class.
   * @param bytecode the bytecode of the class to define.
   * @return the constructed class object from the given {@code bytecode}.
   * @throws IllegalStateException if the class defining failed.
   */
  @NonNull Class<?> defineClass(@NonNull String name, @NonNull Class<?> parent, byte[] bytecode);
}
