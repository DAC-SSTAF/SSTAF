package mil.sstaf.core.features;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying a dependency that must be injected.
 * <p>
 * The annotation can specify the name, major version, minor version and exact match
 * requirement for the Service to be injected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Requires {
    /**
     * Provides for a name or label to be associated with an injection point. This allows for multiple
     * injection points of the same type to be differentiated.
     *
     * @return the name of the field.
     */
    String name() default "";

    /**
     * Specifies the major version of the required Service.
     * <p>
     * If require exact match is true, then the major version of the Service must match this value. If requireExact
     * is false, then the major version of the Service must be at least this value.
     *
     * @return the major version
     */
    int majorVersion() default 0;

    /**
     * Specifies the minor version of the required Service.
     * <p>
     * If require exact match is true, then the minor version of the Service must match this value. If requireExact
     * is false, then the minor version of the Service must be at least this value.
     *
     * @return the major version
     */
    int minorVersion() default 0;

    /**
     * Specifies whether or not the major and minor version of the Service must match exactly.
     *
     * @return true if an exact match is required, false otherwise.
     */
    boolean requireExact() default false;
}
