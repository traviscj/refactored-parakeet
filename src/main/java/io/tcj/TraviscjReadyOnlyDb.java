package io.tcj;

import kotlin.annotation.AnnotationTarget;
import kotlin.annotation.Target;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier
//@Target({ElementType.FIELD, ElementType.PARAMETER})
@Target(allowedTargets = {AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION})
public @interface TraviscjReadyOnlyDb {
}
