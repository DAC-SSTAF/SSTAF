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

import lombok.experimental.SuperBuilder;
import mil.sstaf.core.features.*;
import mil.sstaf.core.mocks.*;
import mil.sstaf.core.util.Injector;
import mil.sstaf.core.util.SSTAFException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FeatureManagerTest {

    FakeEntity fakeEntity;
    EntityHandle eh;

    @BeforeEach
    void setup() {
        fakeEntity = FakeEntity.builder().build();
        fakeEntity.setForce(Force.BLUE);
        eh = fakeEntity.getHandle();
    }

    @Test
    void canRegisterAHandlerByClass() {
        FeatureManager featureManager = fakeEntity.getFeatureManager();
        Injector.inject(featureManager, eh);
        final Handler h1 = new Handler1();
        final Handler h2 = new Handler2();
        featureManager.register(h1);
        featureManager.register(h2);
        featureManager.getHandlerForContent(IntContent.builder().intValue(3).build()).ifPresentOrElse(s -> assertEquals(h2, s), () -> fail("Not found"));
        featureManager.getHandlerForContent(StringContent.of("string")).ifPresentOrElse(s -> assertEquals(h1, s), () -> fail("Not found"));
        featureManager.getHandlerForContent(Command1.builder().build()).ifPresentOrElse(s -> assertEquals(h1, s), () -> fail("Not found"));

        assertEquals(h1, featureManager.getHandler(StringContent.class, "Handler1"));
        assertEquals(h1, featureManager.getHandler(Command1.class, "Handler1"));
        assertEquals(h1, featureManager.getHandler(StringContent.class, null));
        assertEquals(h1, featureManager.getHandler(Command1.class, null));

        assertEquals(h2, featureManager.getHandler(IntContent.class, "Handler2"));
        assertEquals(h2, featureManager.getHandler(LongContent.class, "Handler2"));
        assertEquals(h2, featureManager.getHandler(IntContent.class, null));
        assertEquals(h2, featureManager.getHandler(LongContent.class, null));

        assertNull(featureManager.getHandler(UnsupportedCommand.class, "Handler1"));
        assertNull(featureManager.getHandler(UnsupportedCommand.class, "Handler2"));

        assertThrows(NullPointerException.class,
                () -> featureManager.getHandler(null, "Handler1"));
    }

    @Test
    void unregisteredClassYieldsEmptyOptional() {
        FeatureManager featureManager = fakeEntity.getFeatureManager();
        Injector.inject(featureManager, eh);
        assertTrue(featureManager.getHandlerForContent(Double.class).isEmpty());
    }

    @Test
    void canRegisterAHandlerAndDispatchContentToIt() {
        FeatureManager featureManager = fakeEntity.getFeatureManager();
        Injector.inject(featureManager, eh);

        Handler1 handler1 = new Handler1();
        featureManager.register(handler1);
        FeatureConfiguration fc = FeatureConfiguration.builder().build();
        fc.setSeed(1234);
        handler1.configure(fc);
        PinkyProvider pinkyProvider = new Pinky();
        pinkyProvider.configure(fc);
        featureManager.injectAll(pinkyProvider);

        Command1 tc1 = new Command1();
        assertFalse(tc1.done);
        featureManager.init();

        var b = EntityEvent.builder();
        b.destination(Address.makeExternalAddress(eh));
        b.source(Address.makeExternalAddress(eh));
        b.respondTo(Address.makeExternalAddress(eh));
        b.content(tc1);
        b.eventTime_ms(1);
        EntityEvent ee = b.build();

        ProcessingResult pr = featureManager.process(ee, 10);
        assertNotNull(pr);
        assertNotNull(pr.messages);
        assertEquals(1, pr.messages.size());
        assertTrue(pr.messages.get(0).getContent() instanceof Command1);
        Command1 tc2 = (Command1) pr.messages.get(0).getContent();
        assertTrue(tc2.done);
    }

    @Test
    void processingBeforeInitializationThrows() {
        FeatureManager featureManager = fakeEntity.getFeatureManager();
        Injector.inject(featureManager, eh);
        FeatureConfiguration fc = FeatureConfiguration.builder().build();
        fc.setSeed(1234);
        Handler1 handler1 = new Handler1();
        featureManager.register(handler1);
        handler1.configure(fc);

        var b = EntityEvent.builder();
        b.destination(Address.makeExternalAddress(eh));
        b.source(Address.makeExternalAddress(eh));
        b.respondTo(Address.makeExternalAddress(eh));
        b.content(new Command1());
        b.eventTime_ms(100);
        EntityEvent ee = b.build();

        assertThrows(SSTAFException.class,
                () -> featureManager.process(ee, 100));
    }

    @Test
    void processingUnregisteredContentThrows() {
        FeatureManager featureManager = fakeEntity.getFeatureManager();
        Injector.inject(featureManager, eh);
        FeatureConfiguration fc = FeatureConfiguration.builder().build();
        fc.setSeed(1234);
        Handler1 h1 = new Handler1();
        h1.configure(fc);
        Pinky pinky = new Pinky();
        pinky.configure(fc);
        Injector.inject(h1, pinky);
        featureManager.register(h1);
        featureManager.init();

        var b = EntityEvent.builder();
        b.destination(Address.makeExternalAddress(eh));
        b.source(Address.makeExternalAddress(eh));
        b.respondTo(Address.makeExternalAddress(eh));
        b.content(IntContent.builder().intValue(3).build());
        b.eventTime_ms(100);
        EntityEvent ee = b.build();

        ProcessingResult pr = featureManager.process(ee, 100);
        assertTrue(pr.messages.get(0) instanceof ErrorResponse);
    }

    @Test
    void injectionAndInitializationWorks() {
        FeatureConfiguration fc = FeatureConfiguration.builder().build();
        fc.setSeed(1234);
        FeatureManager featureManager = fakeEntity.getFeatureManager();
        Injector.inject(featureManager, eh);
        Handler1 h1 = new Handler1();
        h1.configure(fc);
        Handler2 h2 = new Handler2();
        h2.configure(fc);
        featureManager.register(h1);
        featureManager.register(h2);

        PinkyProvider pinky = new Pinky();
        pinky.configure(fc);
        BrainProvider brain = new Brain();
        brain.configure(fc);
        featureManager.register(pinky);
        featureManager.register(brain);

        String stringVal = "Jessica";
        Long posVal = 315444L;
        Long negVal = -454545L;
        UUID bob = UUID.randomUUID();

        featureManager.injectAll(stringVal, bob, pinky, brain);
        featureManager.injectNamed("+", posVal);
        featureManager.injectNamed("-", negVal);
        featureManager.init();

        assertEquals(h1.myString, stringVal);
        assertEquals(h1.posLong, posVal);
        assertEquals(h1.negLong, negVal);
        assertEquals(h1.pinky, pinky);

        assertEquals(h2.string, stringVal);
        assertEquals(h2.uuid, bob);
        assertEquals(h2.brain, brain);
        assertTrue(h2.isInitialized());
    }

    @Test
    void handlersLoadedViaRequiresCanBeAccessed() {
        Loaders.registerClass(Handler1.class);
        Loaders.registerClass(Handler2.class);
        Loaders.registerClass(Handler3.class);
        Loaders.registerClass(Pinky.class);
        Loaders.registerClass(Brain.class);

        var fakeEntityBuilder2 = FakeEntity.builder();
        FeatureSpecification fs = FeatureSpecification.builder()
                .featureName("Handler3").build();
        fakeEntityBuilder2.features(List.of(fs));
        FakeEntity fakeEntity2 = fakeEntityBuilder2.build();
        fakeEntity2.setForce(Force.BLUE);
        EntityHandle eh2 = fakeEntity2.getHandle();

        FeatureManager featureManager = fakeEntity2.getFeatureManager();
        Injector.inject(featureManager, eh2);

        featureManager.init();

        assertDoesNotThrow(() -> {
            assertEquals("Handler1", featureManager.getHandler(StringContent.class, "Handler1").getName());
            assertEquals("Handler1", featureManager.getHandler(Command1.class, "Handler1").getName());
            assertEquals("Handler1", featureManager.getHandler(StringContent.class, null).getName());
            assertEquals("Handler1", featureManager.getHandler(Command1.class, null).getName());

            assertEquals("Handler2", featureManager.getHandler(IntContent.class, "Handler2").getName());
            assertEquals("Handler2", featureManager.getHandler(LongContent.class, "Handler2").getName());
            assertEquals("Handler2", featureManager.getHandler(IntContent.class, null).getName());
            assertEquals("Handler2", featureManager.getHandler(LongContent.class, null).getName());

            assertEquals("Handler3", featureManager.getHandler(BlobContent.class, null).getName());
        });
    }

    @SuperBuilder
    static class FakeEntity extends BaseEntity {

        @Override
        public String getPath() {
            return "PATH1:PATH2:" + getName();
        }

    }


    @DisplayName("Test the happy paths")
    @Nested
    class HappyTests {
        @Test
        void canRegisterAHandlerByClass() {
            FeatureManager featureManager = fakeEntity.getFeatureManager();
            Injector.inject(featureManager, eh);
            final Handler h1 = new Handler1();
            final Handler h2 = new Handler2();
            featureManager.register(h1);
            featureManager.register(h2);
            featureManager.getHandlerForContent(IntContent.builder().build()).ifPresentOrElse(s -> assertEquals(h2, s), () -> fail("Not found"));
            featureManager.getHandlerForContent(StringContent.of("string")).ifPresentOrElse(s -> assertEquals(h1, s), () -> fail("Not found"));
            featureManager.getHandlerForContent(new Command1()).ifPresentOrElse(s -> assertEquals(h1, s), () -> fail("Not found"));

            assertEquals(h1, featureManager.getHandler(StringContent.class, "Handler1"));
            assertEquals(h1, featureManager.getHandler(Command1.class, "Handler1"));
            assertEquals(h1, featureManager.getHandler(StringContent.class, null));
            assertEquals(h1, featureManager.getHandler(Command1.class, null));

            assertEquals(h2, featureManager.getHandler(IntContent.class, "Handler2"));
            assertEquals(h2, featureManager.getHandler(LongContent.class, "Handler2"));
            assertEquals(h2, featureManager.getHandler(IntContent.class, null));
            assertEquals(h2, featureManager.getHandler(LongContent.class, null));

            assertNull(featureManager.getHandler(UnsupportedCommand.class, "Handler1"));
            assertNull(featureManager.getHandler(UnsupportedCommand.class, "Handler2"));

            assertThrows(NullPointerException.class,
                    () -> featureManager.getHandler(null, "Handler1"));
        }

        @Test
        void unregisteredClassYieldsEmptyOptional() {
            FeatureManager featureManager = fakeEntity.getFeatureManager();
            Injector.inject(featureManager, eh);
            assertTrue(featureManager.getHandlerForContent(Double.class).isEmpty());
        }

        @Test
        void canRegisterAHandlerAndDispatchContentToIt() {
            FeatureConfiguration fc = FeatureConfiguration.builder().build();
            fc.setSeed(1234);
            FeatureManager featureManager = fakeEntity.getFeatureManager();
            Injector.inject(featureManager, eh);

            Handler1 handler1 = new Handler1();
            featureManager.register(handler1);
            handler1.configure(fc);
            PinkyProvider pinkyProvider = new Pinky();
            pinkyProvider.configure(fc);
            featureManager.injectAll(pinkyProvider);

            Command1 tc1 = new Command1();
            assertFalse(tc1.done);
            featureManager.init();

            var b = EntityEvent.builder();
            b.destination(Address.makeExternalAddress(eh));
            b.source(Address.makeExternalAddress(eh));
            b.respondTo(Address.makeExternalAddress(eh));
            b.content(tc1);
            b.eventTime_ms(1);
            EntityEvent ee = b.build();

            ProcessingResult pr = featureManager.process(ee, 10);
            assertNotNull(pr);
            assertNotNull(pr.messages);
            assertEquals(1, pr.messages.size());
            assertTrue(pr.messages.get(0).getContent() instanceof Command1);
            Command1 tc2 = (Command1) pr.messages.get(0).getContent();
            assertTrue(tc2.done);
        }

        @Test
        void injectionAndInitializationWorks() {
            FeatureConfiguration fc = FeatureConfiguration.builder().build();
            fc.setSeed(1234);
            FeatureManager featureManager = fakeEntity.getFeatureManager();
            Injector.inject(featureManager, eh);
            Handler1 h1 = new Handler1();
            h1.configure(fc);
            Handler2 h2 = new Handler2();
            h2.configure(fc);
            featureManager.register(h1);
            featureManager.register(h2);

            PinkyProvider pinky = new Pinky();
            pinky.configure(fc);
            BrainProvider brain = new Brain();
            brain.configure(fc);
            featureManager.register(pinky);
            featureManager.register(brain);

            String stringVal = "Jessica";
            Long posVal = 315444L;
            Long negVal = -454545L;
            UUID bob = UUID.randomUUID();

            featureManager.injectAll(stringVal, bob, pinky, brain);
            featureManager.injectNamed("+", posVal);
            featureManager.injectNamed("-", negVal);
            featureManager.init();

            assertEquals(h1.myString, stringVal);
            assertEquals(h1.posLong, posVal);
            assertEquals(h1.negLong, negVal);
            assertEquals(h1.pinky, pinky);

            assertEquals(h2.string, stringVal);
            assertEquals(h2.uuid, bob);
            assertEquals(h2.brain, brain);
            assertTrue(h2.isInitialized());
        }

        @Test
        void handlersLoadedViaRequiresCanBeAccessed() {
            Loaders.registerClass(Handler1.class);
            Loaders.registerClass(Handler2.class);
            Loaders.registerClass(Handler3.class);
            Loaders.registerClass(Pinky.class);
            Loaders.registerClass(Brain.class);

            var fakeEntityBuilder2 = FakeEntity.builder();
            FeatureSpecification fs = FeatureSpecification.builder()
                    .featureName("Handler3").build();
            fakeEntityBuilder2.feature(fs);

            FakeEntity fakeEntity2 = fakeEntityBuilder2.build();
            fakeEntity2.setForce(Force.BLUE);
            EntityHandle eh2 = fakeEntity2.getHandle();

            FeatureManager featureManager = fakeEntity2.getFeatureManager();
            Injector.inject(featureManager, eh2);

            featureManager.init();

            assertDoesNotThrow(() -> {
                assertEquals("Handler1", featureManager.getHandler(StringContent.class, "Handler1").getName());
                assertEquals("Handler1", featureManager.getHandler(Command1.class, "Handler1").getName());
                assertEquals("Handler1", featureManager.getHandler(StringContent.class, null).getName());
                assertEquals("Handler1", featureManager.getHandler(Command1.class, null).getName());

                assertEquals("Handler2", featureManager.getHandler(IntContent.class, "Handler2").getName());
                assertEquals("Handler2", featureManager.getHandler(LongContent.class, "Handler2").getName());
                assertEquals("Handler2", featureManager.getHandler(IntContent.class, null).getName());
                assertEquals("Handler2", featureManager.getHandler(LongContent.class, null).getName());

                assertEquals("Handler3", featureManager.getHandler(BlobContent.class, null).getName());
            });
        }
    }

    @DisplayName("Test the failure modes")
    @Nested
    class FailureTests {
        @Test
        void processingBeforeInitializationThrows() {
            FeatureConfiguration fc = FeatureConfiguration.builder().build();
            fc.setSeed(1234);
            FeatureManager featureManager = fakeEntity.getFeatureManager();
            Injector.inject(featureManager, eh);

            Handler1 handler1 = new Handler1();
            featureManager.register(handler1);
            handler1.configure(fc);

            var b = EntityEvent.builder();
            b.destination(Address.makeExternalAddress(eh));
            b.source(Address.makeExternalAddress(eh));
            b.respondTo(Address.makeExternalAddress(eh));
            b.content(new Command1());
            b.eventTime_ms(100);
            EntityEvent ee = b.build();

            assertThrows(SSTAFException.class,
                    () -> featureManager.process(ee, 100));
        }

        @Test
        void processingUnregisteredContentThrows() {
            FeatureConfiguration fc = FeatureConfiguration.builder().build();
            fc.setSeed(1234);
            FeatureManager featureManager = fakeEntity.getFeatureManager();
            Injector.inject(featureManager, eh);
            Handler1 h1 = new Handler1();
            h1.configure(fc);
            Pinky pinky = new Pinky();
            pinky.configure(fc);
            Injector.inject(h1, pinky);
            featureManager.register(h1);
            featureManager.init();

            var b = EntityEvent.builder();
            b.destination(Address.makeExternalAddress(eh));
            b.source(Address.makeExternalAddress(eh));
            b.respondTo(Address.makeExternalAddress(eh));
            b.content(IntContent.builder().intValue(3).build());
            b.eventTime_ms(100);
            EntityEvent ee = b.build();

            ProcessingResult pr = featureManager.process(ee, 100);
            assertTrue(pr.messages.get(0) instanceof ErrorResponse);
        }
    }
}

