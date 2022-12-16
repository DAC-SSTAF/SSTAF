/*
 * Copyright (c) 2022
 * United States Government as represented by the U.S. Army DEVCOM Analysis Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mil.devcom_dac.equipment.api;

import java.util.List;
import java.util.Map;

public interface SoldierKit {

    Item getItemByName(String itemName);

    Gun getGunByName(String gunName);

    Map<String, Gun> getGuns();

    Map<String, Pack> getPacks();

    List<Magazine> getMagazines(String magazineType);

    double getMass();

    boolean canReload(Gun gun);

    boolean reload(Gun gun);

    void drop(Gun gun);

    void drop(Pack pack);

    void add(Gun gun);

    void add(Pack pack);

    void add(Magazine mag);

    void drop(Magazine mag);
}

