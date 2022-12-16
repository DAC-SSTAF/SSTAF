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

package mil.sstaf.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import mil.sstaf.core.json.JsonLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@SuperBuilder
@Jacksonized
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class Unit extends BaseEntity {

    private static final Logger logger = LoggerFactory.getLogger(Unit.class);

    private static final String COMMANDER = "Commander";
    @Singular
    private final List<MemberSoldier> soldiers;
    @Singular
    private final List<MemberUnit> subUnits;
    private final UnitType type;
    @Getter
    @Builder.Default
    @JsonIgnore
    private Map<String, Soldier> soldierMap = null;
    @Builder.Default
    @JsonIgnore
    private Map<String, Unit> subUnitMap = null;
    @Builder.Default
    @JsonIgnore
    private String path = null;

    @Builder.Default
    @JsonIgnore
    private Unit parent = null;

    /**
     * Constructor
     *
     * @param builder the builder
     */
    protected Unit(UnitBuilder<?, ?> builder) {
        super(builder);
        type = builder.type;
        soldiers = builder.soldiers;
        subUnits = builder.subUnits;
        soldierMap = compileSoldiers(builder);
        subUnitMap = compileUnits(builder);
        for (var e : subUnitMap.entrySet()) {
            e.getValue().setName(e.getKey());
        }
        resetSoldiersUnit();
        resetSubUnitsParent();
    }

    public static Unit from(File file) {
        JsonLoader jsonLoader = new JsonLoader();
        return (Unit) jsonLoader.load(Path.of(file.getPath()));
    }

    public static Unit from(String json, Path sourceFile) {
        JsonLoader jsonLoader = new JsonLoader();
        return (Unit) jsonLoader.load(json, sourceFile);
    }

    public static Unit from(JsonNode jsonNode, Path sourceFile) {
        JsonLoader jsonLoader = new JsonLoader();
        return (Unit) jsonLoader.load(jsonNode, sourceFile);
    }

    private Map<String, Soldier> compileSoldiers(UnitBuilder<?, ?> builder) {
        Map<String, Soldier> soldierMap = new HashMap<>();
        if (builder.soldiers != null) {
            for (MemberSoldier sp : builder.soldiers) {
                String position = sp.getPosition();
                Soldier soldier = sp.getSoldier();
                soldier.setUnit(this, position);
                soldierMap.put(position, soldier);
            }
        }
        return soldierMap;
    }

    private Map<String, Unit> compileUnits(UnitBuilder<?, ?> builder) {
        Map<String, Unit> unitMap = new TreeMap<>();
        if (builder.subUnits != null) {
            for (MemberUnit mu : builder.subUnits) {
                Unit unit = mu.unit;
                String label = mu.label;
                unitMap.put(label, unit);
                unit.setName(label);
                unit.parent = this;
            }
        }
        return unitMap;
    }

    /**
     * Performs any Unit-level initialization
     */
    public void init() {
        logger.debug("Initializing Unit {}", getPath());
        super.init();
    }

    private void resetSubUnitsParent() {
        for (Map.Entry<String, Unit> entry : subUnitMap.entrySet()) {
            entry.getValue().attach(this, entry.getKey());
        }
    }

    public void setName(final String name) {
        this.name = name;
        this.path = null;
    }

    public String getPath() {
        if (path == null) {
            path = (parent == null) ? name : parent.getPath() + Entity.ENTITY_PATH_DELIMITER + name;
        }
        return path;
    }

    private void checkString(final String name) {
        Objects.requireNonNull(name, "Name must not be null");
    }

    private void checkSoldier(final Soldier s) {
        Objects.requireNonNull(s, "Soldier must not be null");
    }

    public Soldier getCommander() {
        return soldierMap.get(COMMANDER);
    }

    public void setCommanderByName(final String name) {
        checkString(name);
        Soldier newCommander = getSoldier(name);
        if (newCommander != null) {
            Soldier oldCommander = soldierMap.remove(COMMANDER);
            if (oldCommander != null) {
                addSoldier(newCommander.getPosition(), oldCommander);
            }
            addSoldier(COMMANDER, newCommander);
        }
    }

    private Soldier getSoldier(String name) {
        checkString(name);
        Soldier result = null;
        for (Soldier s : soldierMap.values()) {
            if (s.getName().equals(name)) {
                result = s;
                break;
            }
        }
        return result;
    }

    public void detach() {
        if (parent != null) {
            parent.subUnitMap.remove(name);
        }
        this.parent = null;
        this.path = null;
        resetSoldiersUnit();
        resetSubUnitsParent();
    }

    public void attach(final Unit parent, final String key) {
        checkUnit(parent);
        this.parent = parent;
        this.name = key;
        this.path = null;
        resetSoldiersUnit();
        resetSubUnitsParent();
    }

    private void resetSoldiersUnit() {
        for (Map.Entry<String, Soldier> entry : soldierMap.entrySet()) {
            entry.getValue().setUnit(this, entry.getKey());
        }
    }

    public UnitType getType() {
        return type;
    }

    public Soldier getSoldierByPosition(final String path) {
        checkString(path);
        int firstSeparator = path.indexOf(':');
        if (firstSeparator == 0) {
            //
            // path starts with ':' not sensible
            //
            return null;
        } else if (firstSeparator < 0) {
            //
            // Simple name
            //
            return soldierMap.get(path);
        } else {
            String firstToken = path.substring(0, firstSeparator);
            String remainder = path.substring(firstSeparator + 1);
            if (this.name.equals(firstToken)) {
                return getSoldierByPosition(remainder);
            } else {
                Unit subUnit = subUnitMap.get(firstToken);
                if (subUnit == null) {
                    return null;
                } else {
                    return subUnit.getSoldierByPosition(remainder);
                }
            }
        }
    }

    public Soldier getSoldierByName(final String name) {
        checkString(name);
        Soldier result = getSoldier(name);
        for (Iterator<Unit> iterator = subUnitMap.values().iterator();
             result == null && iterator.hasNext(); ) {
            Unit unit = iterator.next();
            result = unit.getSoldierByName(name);
        }
        return result;
    }

    public Soldier removeSoldierByName(final String name) {
        checkString(name);
        Soldier s = getSoldierByName(name);
        if (s != null) {
            Unit u = s.getUnit();
            String position = s.getPosition();
            if (u == null || position == null) {
                throw new IllegalStateException("A Soldier in a unit must have non-null Unit and Position values");
            }
            s = u.soldierMap.remove(position);
            s.setUnit(null, null);
            return s;
        }
        return null;
    }

    public Map<String, Soldier> getAllMembers() {
        Map<String, Soldier> members = new TreeMap<>();

        for (Map.Entry<String, Soldier> entry : soldierMap.entrySet()) {
            members.put(entry.getValue().getPath(), entry.getValue());
        }

        for (Unit unit : subUnitMap.values()) {
            members.putAll(unit.getAllMembers());
        }
        return members;
    }

    public Map<String, Soldier> getDirectMembers() {
        Map<String, Soldier> members = new TreeMap<>();

        for (Map.Entry<String, Soldier> entry : soldierMap.entrySet()) {
            members.put(entry.getValue().getPath(), entry.getValue());
        }
        return members;
    }

    public Map<String, Unit> getSubUnitMap() {
        return Collections.unmodifiableMap(subUnitMap);
    }

    public Map<String, Unit> getAllUnits() {
        Map<String, Unit> units = new TreeMap<>();
        units.put(this.getPath(), this);
        for (Unit unit : subUnitMap.values()) {
            units.putAll(unit.getAllUnits());
        }
        return units;
    }

    public int getNumMembers() {
        int numMembers = soldierMap.size();
        for (Unit unit : subUnitMap.values()) {
            numMembers += unit.getNumMembers();
        }
        return numMembers;
    }

    String getPositionForSoldierByName(final String name) {
        checkString(name);
        Soldier s = getSoldierByName(name);
        return (s == null) ? null : s.getPosition();
    }

    void transferSoldierTo(final String name, final Unit receiver, final String position) {
        checkString(name);
        checkString(position);
        checkUnit(receiver);
        Soldier s = removeSoldierByName(name);
        if (s != null) {
            receiver.addSoldier(position, s);
        }
    }

    public void addSoldier(final Soldier soldier) {
        checkSoldier(soldier);
        addSoldier(soldier.getName(), soldier);
    }

    public void addSoldier(final String position, final Soldier soldier) {
        checkString(position);
        checkSoldier(soldier);
        soldierMap.putIfAbsent(position, soldier);
        soldier.setUnit(this, position);
    }

    public void addUnit(final String label, final Unit unit) {
        checkUnit(unit);
        subUnitMap.putIfAbsent(label, unit);
        unit.attach(this, unit.getName());
    }

    private void checkUnit(final Unit unit) {
        Objects.requireNonNull(unit, "Unit must not be null");
    }

    /**
     * Defines the job a Soldier has in a unit.
     */
    @Builder
    @Jacksonized
    static public final class MemberSoldier {

        @Getter
        @NonNull
        String position;

        @Getter
        @NonNull
        Soldier soldier;

        public static MemberSoldier of(String position, Soldier soldier) {
            Objects.requireNonNull(position);
            Objects.requireNonNull(soldier);
            return builder().position(position).soldier(soldier).build();
        }
    }

    @Builder
    @Jacksonized
    static public final class MemberUnit {

        @Getter
        @NonNull
        String label;

        @Getter
        @NonNull
        Unit unit;

        public static MemberUnit of(String label, Unit definition) {
            Objects.requireNonNull(label);
            Objects.requireNonNull(definition);
            return builder().label(label).unit(definition).build();
        }
    }

}

