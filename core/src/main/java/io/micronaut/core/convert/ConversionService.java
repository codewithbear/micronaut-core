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
package io.micronaut.core.convert;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;

import java.util.Optional;

/**
 * A service for allowing conversion from one type to another.
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public interface ConversionService {

    /**
     * The default shared conversion service.
     */
    ConversionService SHARED = new DefaultMutableConversionService();

    /**
     * Attempts to convert the given object to the given target type. If conversion fails or is not possible an empty {@link Optional} is returned.
     *
     * @param object     The object to convert
     * @param targetType The target type
     * @param context    The conversion context
     * @param <T>        The generic type
     * @return The optional
     */
    <T> Optional<T> convert(Object object, Class<T> targetType, ConversionContext context);

    /**
     * Return whether the given source type is convertible to the given target type.
     *
     * @param sourceType The source type
     * @param targetType The target type
     * @param <S>        The generic source type
     * @param <T>        The target source type
     * @return True if it can be converted
     */
    <S, T> boolean canConvert(Class<S> sourceType, Class<T> targetType);

    /**
     * Attempts to convert the given object to the given target type. If conversion fails or is not possible an empty {@link Optional} is returned.
     *
     * @param object     The object to convert
     * @param targetType The target type
     * @param <T>        The generic type
     * @return The optional
     */
    default <T> Optional<T> convert(Object object, Class<T> targetType) {
        return convert(object, targetType, ConversionContext.DEFAULT);
    }

    /**
     * Attempts to convert the given object to the given target type. If conversion fails or is not possible an empty {@link Optional} is returned.
     *
     * @param object     The object to convert
     * @param targetType The target type
     * @param <T>        The generic type
     * @return The optional
     */
    default <T> Optional<T> convert(Object object, Argument<T> targetType) {
        return convert(object, targetType.getType(), ConversionContext.of(targetType));
    }

    /**
     * Attempts to convert the given object to the given target type. If conversion fails or is not possible an empty {@link Optional} is returned.
     *
     * @param object  The object to convert
     * @param context The {@link ArgumentConversionContext}
     * @param <T>     The generic type
     * @return The optional
     */
    default <T> Optional<T> convert(Object object, ArgumentConversionContext<T> context) {
        return convert(object, context.getArgument().getType(), context);
    }

    /**
     * Convert the value to the given type.
     * @param value The value
     * @param type The type
     * @param <T> The generic type
     * @return The converted value
     * @throws ConversionErrorException if the value cannot be converted
     * @since 1.1.4
     */
    default @Nullable <T> T convertRequired(@Nullable Object value, Class<T> type) {
        if (value == null) {
            return null;
        }
        Argument<T> arg = Argument.of(type);
        return convertRequired(value, arg);
    }

    /**
     * Convert the value to the given type.
     * @param value The value
     * @param argument The argument
     * @param <T> The generic type
     * @return The converted value
     * @throws ConversionErrorException if the value cannot be converted
     * @since 1.1.4
     */
    default @Nullable <T> T convertRequired(@Nullable Object value, Argument<T> argument) {
        ArgumentConversionContext<T> context = ConversionContext.of(argument);
        return convertRequired(value, context);
    }

    /**
     * Convert the value to the given type.
     * @param value The value
     * @param context The conversion context
     * @param <T> The generic type
     * @return The converted value
     * @throws ConversionErrorException if the value cannot be converted
     * @since 4.1.0
     */
    default  <T> T convertRequired(Object value, ArgumentConversionContext<T> context) {
        Argument<T> argument = context.getArgument();
        return convert(
            value,
            argument.getType(),
            context
        ).orElseThrow(() -> newConversionError(context, argument, value));
    }

    private static <T> ConversionErrorException newConversionError(ArgumentConversionContext<T> context, Argument<T> argument, Object value) {
        Optional<ConversionError> lastError = context.getLastError();
        return lastError.map(conversionError -> new ConversionErrorException(context.getArgument(), conversionError)).orElseGet(() -> new ConversionErrorException(context.getArgument(), new IllegalArgumentException("Cannot convert type [" + value.getClass() + "] to target type: " + argument.getType() + ". Considering defining a TypeConverter bean to handle this case.")));
    }
}
