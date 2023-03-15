package mil.devcom_dac.equipment.handler;

import mil.devcom_dac.equipment.api.EquipmentConfiguration;
import mil.devcom_dac.equipment.api.EquipmentManagement;
import mil.devcom_dac.equipment.messages.GetInventory;
import mil.sstaf.core.entity.Address;
import mil.sstaf.core.entity.EntityHandle;
import mil.sstaf.core.features.Loaders;
import mil.sstaf.core.json.JsonLoader;
import mil.sstaf.core.util.Injector;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class EquipmentHandlerTest {
    public static Path basepath = Path.of("src", "test", "resources");
    static EquipmentConfiguration configuration;
    EquipmentManagement equipmentManagement;

    @BeforeAll
    static void beginning() {
        configuration = new JsonLoader().load(Path.of(basepath.toString(), "TestConfig.json"),
                EquipmentConfiguration.class);
    }

    @BeforeEach
    void setUp() {
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
        public void test2() {
            assertNotNull(equipmentManagement);
            GetInventory msg = GetInventory.builder().build();
            var x = equipmentManagement.process(msg, 1000, 1000,
                    Address.NOWHERE, 1 , Address.NOWHERE);
            assertNotNull(x);
            assertEquals(1, x.messages.size());
        }
    }
}
