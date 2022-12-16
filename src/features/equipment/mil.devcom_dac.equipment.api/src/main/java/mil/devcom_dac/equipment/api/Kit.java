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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.*;

@SuperBuilder
@Jacksonized
@JsonIgnoreProperties(value={"class"})
public class Kit implements SoldierKit {

    private List<Magazine> magazines;
    private List<Gun> guns;
    private List<Pack> packs;

    @Builder.Default
    @JsonIgnore
    private Map<String, List<Magazine>> magazineMap = null;

    @Builder.Default
    @JsonIgnore
    private Map<String, Gun> gunMap = null;
    @Builder.Default
    @JsonIgnore
    private Map<String, Pack> packMap = null;

    @Builder.Default
    @JsonIgnore
    private List<Item> allItems = null;

    public Kit(KitBuilder<?, ?> builder) {
        // 2022.04.21 : RAB : Packs, Guns and Magazines are created as
        //                    modifiable collections.
        this.gunMap = new HashMap<>();
        if (builder.guns != null) {
            for (Gun g : builder.guns) {
                this.gunMap.put(g.getName(), g);
            }
        }

        this.packMap = new HashMap<>();
        if (builder.packs != null) {
            for (Pack p : builder.packs) {
                this.packMap.put(p.getName(), p);
            }
        }
        this.magazineMap = new HashMap<>();
        if (builder.magazines != null) {
            for (var mag : builder.magazines) {
                List<Magazine> magList;
                if (this.magazineMap.containsKey(mag.getMagazineType())) {
                    magList = magazineMap.get(mag.getMagazineType());
                } else {
                    magList = new ArrayList<>();
                    magazineMap.put(mag.getMagazineType(), magList);
                }
                magList.add(mag);
            }
        }

        this.allItems = new ArrayList<>();
        for (var x : this.gunMap.entrySet()) {
            allItems.add(x.getValue());
        }
        for (var x : this.packMap.entrySet()) {
            allItems.add(x.getValue());
        }
        for (var es : this.magazineMap.entrySet()) {
            allItems.addAll(es.getValue());
        }
    }

    static private void checkNullGun(Gun gun) {
        if (gun == null) {
            throw new IllegalArgumentException("Gun must not be null.");
        }
    }

    static private void checkNullMagazine(Magazine magazine) {
        if (magazine == null) {
            throw new IllegalArgumentException("Magazine must not be null.");
        }
    }

    static private void checkNullPack(Pack pack) {
        if (pack == null) {
            throw new IllegalArgumentException("Pack must not be null.");
        }
    }

    public Item getItemByName(String name) {
        Objects.requireNonNull(name, "Item name was null");
        Item found = null;
        for (Item item : allItems) {
            if (name.equals(item.getName())) {
                found = item;
            }
        }
        return found;
    }

    /**
     * Provides the gun with the given name,if it exists
     *
     * @param gunName the name of the {@code Gun}
     * @return the Gun or null
     */
    @Override
    public Gun getGunByName(final String gunName) {
        return gunMap.get(Objects.requireNonNull(gunName, "Gun name was null"));
    }

    @Override
    public Map<String, Gun> getGuns() {
        return Collections.unmodifiableMap(gunMap);
    }

    @Override
    public Map<String, Pack> getPacks() {
        return Collections.unmodifiableMap(packMap);
    }

    @Override
    public List<Magazine> getMagazines(String magazineType) {
        return magazineMap.getOrDefault(magazineType, Collections.emptyList());
    }

    @Override
    public double getMass() {
        double mass = 0.0;

        for (Gun gun : gunMap.values()) {
            mass += gun.getMass_kg();
        }
        for (List<Magazine> magSets : magazineMap.values()) {
            for (Magazine mag : magSets) {
                mass += mag.getMass_kg();
            }
        }
        for (Pack pack : packMap.values()) {
            mass += pack.getMass_kg();
        }
        return mass;
    }

    @Override
    public boolean canReload(Gun gun) {
        return !getMagazines(gun.getMagazineType()).isEmpty();
    }

    @Override
    public boolean reload(Gun gun) {
        List<Magazine> mags = getMagazines(gun.getMagazineType());
        if (mags.isEmpty()) {
            return false;
        } else {
            Magazine mag = mags.remove(mags.size() - 1);
            gun.loadMagazine(mag);
            return true;
        }
    }

    @Override
    public void drop(Gun gun) {
        checkNullGun(gun);
        gunMap.remove(gun.getName());
    }

    @Override
    public void drop(Pack pack) {
        checkNullPack(pack);
        packMap.remove(pack.getName());
    }

    @Override
    public void add(Gun gun) {
        checkNullGun(gun);
        gunMap.put(gun.getName(), gun);
        allItems.add(gun);
    }

    @Override
    public void add(Pack pack) {
        checkNullPack(pack);
        packMap.put(pack.getName(), pack);
        allItems.add(pack);
    }

    @Override
    public void add(Magazine mag) {
        checkNullMagazine(mag);
        List<Magazine> magazines = this.magazineMap.getOrDefault(mag.getMagazineType(), new ArrayList<>(10));
        magazines.add(mag);
        this.magazineMap.put(mag.getMagazineType(), magazines);
        allItems.add(mag);
    }

    @Override
    public void drop(Magazine mag) {
        checkNullMagazine(mag);
        if (this.magazineMap.containsKey(mag.getMagazineType())) {
            List<Magazine> mags = this.magazineMap.get(mag.getMagazineType());
            mags.remove(mag);
        }
    }
}

