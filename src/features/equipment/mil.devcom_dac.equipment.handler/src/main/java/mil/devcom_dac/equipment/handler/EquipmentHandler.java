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

package mil.devcom_dac.equipment.handler;

import mil.devcom_dac.equipment.api.*;
import mil.devcom_dac.equipment.messages.*;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.Message;
import mil.sstaf.core.features.BaseHandler;
import mil.sstaf.core.features.FeatureConfiguration;
import mil.sstaf.core.features.HandlerContent;
import mil.sstaf.core.features.ProcessingResult;
import mil.sstaf.core.util.SSTAFException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EquipmentHandler extends BaseHandler implements EquipmentManagement {
    public static final String FEATURE_NAME = "Kit Manager";
    public static final int MAJOR_VERSION = 0;
    public static final int MINOR_VERSION = 1;
    public static final int PATCH_VERSION = 0;

    private static final Logger logger = LoggerFactory.getLogger(EquipmentHandler.class);

    private Kit kit;

    private Gun currentGun;


    public EquipmentHandler() {
        super(FEATURE_NAME, MAJOR_VERSION, MINOR_VERSION, PATCH_VERSION,
                true, "Handler for military equipment loading");
    }

    @Override
    public List<Class<? extends HandlerContent>> contentHandled() {
        return List.of(GetInventory.class, Shoot.class,
                Reload.class, SetGun.class);
    }

    @Override
    public void init() {
        super.init();
        if (currentGun.getMagazine() == null) {
            kit.reload(currentGun);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(FeatureConfiguration configuration) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}:{} - Configuring with {}", ownerHandle.getPath(), featureName, configuration);
        }
        super.configure(configuration);
        if (configuration instanceof EquipmentConfiguration) {
            EquipmentConfiguration ec = (EquipmentConfiguration) configuration;
            kit = ec.getKit();
            if (ec.getCurrentGun() != null) {
                currentGun = kit.getGunByName(ec.getCurrentGun());
            } else {
                currentGun = kit.getGuns().values().iterator().next();
            }
        }
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public Item getItemByName(String itemName) {
        Objects.requireNonNull(itemName, "Item name");
        return kit.getItemByName(itemName);
    }

    private void setGun(String gunName) {
         Gun gun = getGunByName(gunName);
        if (gun == null) {
            throw new SSTAFException("Gun " + gunName + " was not found");
        } else {
            currentGun = gun;
        }
    }

@Override
    public ProcessingResult process(HandlerContent arg, long scheduledTime_ms, long currentTime_ms, Address from, long id, Address respondTo) {
        ProcessingResult rv;
        if (arg instanceof Shoot) {
            Shoot shootMessage = (Shoot) arg;
            if (shootMessage.getGun() != null) {
                setGun(shootMessage.getGun());
            }
            int numShot = currentGun.shoot(shootMessage.getNumToShoot());
            int remaining = currentGun.getMagazine().getCurrentLoad();
            GunState response = GunState.builder()
                    .numberShot(numShot)
                    .roundsInCurrentGun(remaining)
                    .currentGun(currentGun.getName())
                    .build();
            Message m = buildNormalResponse(response, id, respondTo);
            rv = ProcessingResult.of(m);
        } else if (arg instanceof GetInventory) {
            Inventory ir = buildInventory();
            Message m = buildNormalResponse(ir, id, respondTo);
            rv = ProcessingResult.of(m);
        } else if (arg instanceof Reload) {
            Reload rm = (Reload) arg;
            if (rm.getGun() != null) {
                setGun(rm.getGun());
            }
            reload(currentGun);
            GunState gs = GunState.builder()
                    .currentGun(currentGun.getName())
                    .roundsInCurrentGun(currentGun.getMagazine().getCurrentLoad())
                    .build();
            Message m = buildNormalResponse(gs, id, respondTo);
            rv = ProcessingResult.of(m);
        } else if (arg instanceof SetGun) {
            SetGun sg = (SetGun)arg;
            setGun(sg.getGun());
            GunState gs = GunState.builder()
                    .currentGun(currentGun.getName())
                    .roundsInCurrentGun(currentGun.getMagazine().getCurrentLoad())
                    .build();
            Message m = buildNormalResponse(gs, id, respondTo);
            rv = ProcessingResult.of(m);
        } else {
            rv = buildUnsupportedMessageResponse(arg, id, respondTo, new Throwable());
        }
        return rv;
}

    private Inventory buildInventory() {
        var builder = Inventory.builder();
        if (currentGun == null) {
            builder.currentGun("None");
            builder.roundsInCurrentGun(0);
        } else if (currentGun.getMagazine() == null){
            builder.currentGun(currentGun.getName());
            builder.roundsInCurrentGun(0);
        } else {
            builder.currentGun(currentGun.getName());
            builder.roundsInCurrentGun(currentGun.getMagazine().getCurrentLoad());
        }

        Map<String, Integer> mpt = new TreeMap<>();
        Map<String, Integer> rpt = new TreeMap<>();
        Set<String> magTypes = new TreeSet<>();

        Map<String, Double> guns = new TreeMap<>();
        for (Gun gun : kit.getGuns().values()) {
            guns.put(gun.getName(), getMass());
            magTypes.add(gun.getMagazineType());
        }
        builder.guns(guns);

        for (String type : magTypes) {
            List<Magazine> magazines = kit.getMagazines(type);
            int nr = 0;
            for (Magazine mag : magazines) {
                nr += mag.getCurrentLoad();
            }
            mpt.put(type, magazines.size());
            rpt.put(type, nr);
        }
        builder.roundsPerType(rpt);
        builder.magazinesPerType(mpt);

        Map<String, Double> packs = new TreeMap<>();
        for (Pack pack : kit.getPacks().values()) {
            packs.put(pack.getName(), pack.getMass_kg());
        }
        builder.packs(packs);

        builder.totalCarriedMass(kit.getMass());

        return builder.build();
    }

    @Override
    public Gun getGunByName(final String gunName) {
        Objects.requireNonNull(gunName, "gun name");
        return kit.getGunByName(gunName);
    }

    @Override
    public Map<String, Gun> getGuns() {
        return kit.getGuns();
    }

    @Override
    public Map<String, Pack> getPacks() {
        return kit.getPacks();
    }

    @Override
    public List<Magazine> getMagazines(String magazineType) {
        return kit.getMagazines(magazineType);
    }

    @Override
    public double getMass() {
        return kit.getMass();
    }

    @Override
    public boolean canReload(Gun gun) {
        return kit.canReload(gun);
    }

    @Override
    public boolean reload(Gun gun) {
        return kit.reload(gun);
    }

    @Override
    public void drop(Gun gun) {
        kit.drop(gun);
    }

    @Override
    public void drop(Pack pack) {
        kit.drop(pack);
    }

    @Override
    public void add(Gun gun) {
        kit.add(gun);
    }

    @Override
    public void add(Pack pack) {
        kit.add(pack);
    }

    @Override
    public void add(Magazine mag) {
        kit.add(mag);
    }

    @Override
    public void drop(Magazine mag) {
        kit.drop(mag);
    }
}

