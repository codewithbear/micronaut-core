/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.inject.qualifiers;

import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.naming.NameResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanType;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static io.micronaut.core.util.ArgumentUtils.check;

/**
 * Qualifies using a name.
 *
 * @param <T> The type
 * @author Graeme Rocher
 * @since 1.0
 */
@Internal
class NameQualifier<T> implements Qualifier<T>, io.micronaut.core.naming.Named {

    protected final Class<? extends Annotation> annotationType;
    private final String name;

    NameQualifier(AnnotationMetadata annotationMetadata, String name) {
        this.annotationType = annotationMetadata != null ? annotationMetadata.getAnnotationType(name).orElse(null) : null;
        this.name = Objects.requireNonNull(annotationType == null ? name : annotationType.getSimpleName(), "Argument [name] cannot be null");
    }

    NameQualifier(Class<? extends Annotation> annotationType) {
        this.name = Objects.requireNonNull(annotationType.getSimpleName(), "Argument [name] cannot be null");
        this.annotationType = annotationType;
    }

    @Override
    public <BT extends BeanType<T>> Stream<BT> reduce(Class<T> beanType, Stream<BT> candidates) {
        check("beanType", beanType).notNull();
        check("candidates", candidates).notNull();
        return candidates.filter(candidate -> {
            if (!QualifierUtils.matchType(beanType, candidate)) {
                return false;
            }
            if (QualifierUtils.matchAny(beanType, candidate)) {
                return true;
            }
            AnnotationMetadata annotationMetadata = candidate.getAnnotationMetadata();
            // here we resolved the declared Qualifier of the bean
            String thisName = annotationMetadata
                    .findDeclaredAnnotation(AnnotationUtil.NAMED)
                    .flatMap(AnnotationValue::stringValue)
                    .orElse(null);

            if (thisName == null && candidate instanceof BeanDefinition definition) {
                Qualifier<?> qualifier = definition.getDeclaredQualifier();
                if (qualifier != null && qualifier.contains((Qualifier) this)) {
                    return true;
                }
            }
            if (thisName == null) {
                 if (candidate instanceof NameResolver resolver) {
                    Optional<String> resolvedName = resolver.resolveName();
                    thisName = resolvedName.orElse(candidate.getBeanType().getSimpleName());
                } else {
                     thisName = candidate.getBeanType().getSimpleName();
                 }
            }
            return thisName.equalsIgnoreCase(name) || thisName.equalsIgnoreCase(name + beanType.getSimpleName());
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !NameQualifier.class.isAssignableFrom(o.getClass())) {
            return false;
        }

        NameQualifier<?> that = (NameQualifier<?>) o;

        return name.equals(that.name);
    }

    @Override
    public String toString() {
        return "@Named('" + name + "')";
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String getName() {
        return name;
    }

}
