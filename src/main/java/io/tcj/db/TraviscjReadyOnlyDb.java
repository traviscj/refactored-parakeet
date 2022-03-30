package io.tcj.db;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;
import kotlin.annotation.AnnotationTarget;
import kotlin.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Qualifier
// @Target({ElementType.FIELD, ElementType.PARAMETER})
@Target(
    allowedTargets = {
      AnnotationTarget.FIELD,
      AnnotationTarget.VALUE_PARAMETER,
      AnnotationTarget.FUNCTION
    })
public @interface TraviscjReadyOnlyDb {}
