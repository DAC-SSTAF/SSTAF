package mil.devcom_dac.equipment.handler;

import mil.devcom_dac.equipment.api.EquipmentConfiguration;
import mil.devcom_dac.equipment.api.EquipmentManagement;
import mil.devcom_dac.equipment.messages.*;
import mil.devcom_sc.ansur.messages.ValueKey;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.Loaders;
import mil.sstaf.core.features.ProcessingResult;
import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.core.util.Injector;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class EquipmentHandlerTest {
    public static Path basepath = Path.of("src", "test", "resources");
    static EquipmentConfiguration configuration;
    EquipmentManagement equipmentManagement;

    @BeforeEach
    void setUp() {
        // Re-configure for each test, so effect of previous test is not included in configuration
        configuration = new JsonLoader().load(Path.of(basepath.toString(), "TestConfig.json"),
                EquipmentConfiguration.class);

        System.out.println(new File(".").getName());
        try {
            System.setProperty("sstaf.preloadFeatureClasses", "true");
            Loaders.preloadFeatureClasses("mil.devcom_dac.equipment.handler.EquipmentHandler");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            fail();
        }

        Optional<EquipmentManagement> optAgent = Loaders.load(EquipmentManagement.class, "Kit Manager", 0, 0);
        if (optAgent.isPresent()) {
            equipmentManagement = optAgent.get();
            equipmentManagement.configure(configuration);
        } else {
            Assertions.fail();
        }

        EntityHandle owner = EntityHandle.makeDummyHandle();
        Injector.inject(equipmentManagement, owner);
        equipmentManagement.init();
    }

    @Nested
    @DisplayName("Test the happy path scenarios")
    class HappyTests {
        @Test
        @DisplayName("Confirm that the EquipmentHandler can be loaded successfully")
        public void test1() {
            assertNotNull(equipmentManagement);
            assertNotNull(equipmentManagement.getGuns().get("M16A1"));
        }

        @Test
        @DisplayName("Confirm that GetInventory message works")
        public void getInventoryTest() {
            assertNotNull(equipmentManagement);
            GetInventory msg = GetInventory.builder().build();
            var x = equipmentManagement.process(msg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
            Inventory inv = (Inventory) x.messages.get(0).getContent();
            assertEquals("M16A1", inv.getCurrentGun());
            assertEquals(0, inv.getRoundsInCurrentGun()); // not loaded
            assertEquals(14.46, inv.getTotalCarriedMass());
            assertEquals(2, inv.getGuns().size());
            assertTrue(inv.getGuns().containsKey("M16A1"));
            assertTrue(inv.getGuns().containsKey("M16A2"));
            assertEquals(1, inv.getMagazinesPerType().size());
            assertTrue(inv.getMagazinesPerType().containsKey("5.56mm STANAG"));
            assertEquals(4, inv.getMagazinesPerType().get("5.56mm STANAG")); // not loaded, should be 4!
            assertEquals(1, inv.getRoundsPerType().size());
            assertTrue(inv.getRoundsPerType().containsKey("5.56mm STANAG"));
            assertEquals(1, inv.getPacks().size());
            assertTrue(inv.getPacks().containsKey("Fanny Pack"));
        }

        @Test
        @DisplayName("Confirm that Shoot message works")
        public void shootTest() {
            assertNotNull(equipmentManagement);
            Reload reloadMsg = Reload.builder().gun("M16A1").build();
            ProcessingResult x = equipmentManagement.process(reloadMsg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
            GunState gs = (GunState) x.messages.get(0).getContent();
            assertEquals(0, gs.getNumberShot());
            assertEquals("M16A1", gs.getCurrentGun());
            assertEquals(30, gs.getRoundsInCurrentGun());  // gun now loaded

            Shoot msg = Shoot.builder().numToShoot(1).gun("M16A1").build();
            x = equipmentManagement.process(msg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
            gs = (GunState) x.messages.get(0).getContent();
            assertEquals(1, gs.getNumberShot());
            assertEquals("M16A1", gs.getCurrentGun());
            assertEquals(29, gs.getRoundsInCurrentGun());
        }

        @Test
        @DisplayName("Confirm that Reload message works")
        public void reloadTest() {
            assertNotNull(equipmentManagement);
            Reload msg = Reload.builder().gun("M16A1").build();
            // Reload, no gun is set.
            ProcessingResult x = equipmentManagement.process(msg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
            GunState gs = (GunState) x.messages.get(0).getContent();
            assertEquals(0, gs.getNumberShot());
            assertEquals("M16A1", gs.getCurrentGun());
            assertEquals(30, gs.getRoundsInCurrentGun());

            // Reload again, this time gun is set from the previous reload
            x = equipmentManagement.process(msg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
            gs = (GunState) x.messages.get(0).getContent();
            assertEquals(0, gs.getNumberShot());
            assertEquals("M16A1", gs.getCurrentGun());
            assertEquals(30, gs.getRoundsInCurrentGun());
        }

        @Test
        @DisplayName("Confirm that SetGun message works")
        public void setGunTest() {
            assertNotNull(equipmentManagement);
            SetGun msg = SetGun.builder().gun("M16A2").build();
            ProcessingResult x = equipmentManagement.process(msg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
            GunState gs = (GunState) x.messages.get(0).getContent();
            assertEquals(0, gs.getNumberShot());
            assertEquals("M16A2", gs.getCurrentGun());
            assertEquals(0, gs.getRoundsInCurrentGun()); // Gun not loaded by default
        }
    }

    @Nested
    @DisplayName("Test invalid path scenarios")
    class InvalidTests {
        @Test
        @DisplayName("Attempt Shoot on gun that does not exist")
        public void shootInvalidGunTest() {
            assertNotNull(equipmentManagement);
            Shoot msg = Shoot.builder().numToShoot(1).gun("M8A1").build();
            Exception ex = assertThrows(SSTAFException.class, () -> {
                ProcessingResult x = equipmentManagement.process(msg, 1000, 1000,
                        Address.NOWHERE, 1 , Address.NOWHERE);
            });
            assertTrue(ex.getMessage().contains("Gun M8A1 was not found"));
        }

        @Test
        @DisplayName("Confirm that Shoot message works without a current gun set (selected gun is unloaded)")
        public void shootUnloadedTest() {
            assertNotNull(equipmentManagement);
            Shoot msg = Shoot.builder().numToShoot(1).gun("M16A1").build();
            ProcessingResult x = equipmentManagement.process(msg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
            GunState gs = (GunState) x.messages.get(0).getContent();
            assertEquals(0, gs.getNumberShot()); // gun not loaded
            assertEquals("M16A1", gs.getCurrentGun());
            assertEquals(0, gs.getRoundsInCurrentGun());
        }

        @Test
        @DisplayName("Confirm that Shoot message works if gun empties out")
        public void shootTillEmptyTest() {
            assertNotNull(equipmentManagement);
            Reload reloadMsg = Reload.builder().gun("M16A1").build();
            ProcessingResult x = equipmentManagement.process(reloadMsg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());

            Shoot msg = Shoot.builder().numToShoot(31).gun("M16A1").build();
            x = equipmentManagement.process(msg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
            GunState gs = (GunState) x.messages.get(0).getContent();
            assertEquals(30, gs.getNumberShot());
            assertEquals("M16A1", gs.getCurrentGun());
            assertEquals(0, gs.getRoundsInCurrentGun());
        }

        @Test
        @DisplayName("Attempt to reload with no magazines left")
        public void reloadTest() {
            assertNotNull(equipmentManagement);
            Reload msg = Reload.builder().gun("M16A1").build();
            // Reload, magazines available.
            for (int i =0; i<4; i++) {
                ProcessingResult x = equipmentManagement.process(msg, 1000, 1000,
                        Address.NOWHERE, 1, Address.NOWHERE);
                assertNotNull(x);
                assertEquals(1, x.messages.size());
                GunState gs = (GunState) x.messages.get(0).getContent();
                assertEquals(0, gs.getNumberShot());
                assertEquals("M16A1", gs.getCurrentGun());
                assertEquals(30, gs.getRoundsInCurrentGun());
            }

            // Reload again, this time no magazines left
            Exception ex = assertThrows(SSTAFException.class, () -> {
                ProcessingResult x2 = equipmentManagement.process(msg, 1000, 1000,
                        Address.NOWHERE, 1 , Address.NOWHERE);
            });
            assertTrue(ex.getMessage().contains("Gun M16A1 reload failed, no magazine of type 5.56mm STANAG available."));
        }

        @Test
        @DisplayName("Attempt to send an unsupported message")
        public void invalidMessageTest() {
            assertNotNull(equipmentManagement);
            mil.devcom_sc.ansur.messages.GetValueMessage msg =
                    mil.devcom_sc.ansur.messages.GetValueMessage.builder().key(ValueKey.AGE).build();
               ProcessingResult x = equipmentManagement.process(msg, 1000, 1000,
                        Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
        }
    }
}
