package io.kontur.insightsapi.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.Assert;
import graphql.language.*;
import graphql.scalars.util.Kit;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Based on {@link graphql.scalars.object.ObjectScalar}
 */
public class JacksonJsonCoercing implements Coercing<Object, String> {

    private final ObjectMapper objectMapper;

    public JacksonJsonCoercing(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
        return dataFetcherResult.toString();
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        return input;
    }

    @Override
    public Object parseLiteral(Object input) throws CoercingParseLiteralException {
        try {
            return objectMapper.writeValueAsString(this.parseLiteralInternal(input, Collections.emptyMap()));
        } catch (JsonProcessingException e) {
            throw new CoercingParseLiteralException();
        }
    }

    public Object parseLiteralInternal(Object input, Map<String, Object> variables) throws CoercingParseLiteralException {
        if (!(input instanceof Value)) {
            throw new CoercingParseLiteralException("Expected AST type 'StringValue' but was '" + Kit.typeName(input) + "'.");
        }
        if (input instanceof ArrayValue) {
            return parseArrayValue(input, variables);
        }
        if (input instanceof ObjectValue) {
            return parseObjectValue(input, variables);
        }
        return parsePrimitive(input, variables);
    }

    private Object parseArrayValue(Object input, Map<String, Object> variables) {
        var values = ((ArrayValue) input).getValues();
        return values.stream().map((v) -> {
            return this.parseLiteralInternal(v, variables);
        }).collect(Collectors.toList());
    }

    private Object parseObjectValue(Object input, Map<String, Object> variables) {
        var values = ((ObjectValue) input).getObjectFields();
        Map<String, Object> parsedValues = new LinkedHashMap<>();
        values.forEach(fld -> {
            Object parsedValue = this.parseLiteralInternal(fld.getValue(), variables);
            parsedValues.put(fld.getName(), parsedValue);
        });
        return parsedValues;
    }

    private Object parsePrimitive(Object input, Map<String, Object> variables) {
        if (input instanceof NullValue) {
            return null;
        }
        if (input instanceof FloatValue) {
            return ((FloatValue) input).getValue();
        }
        if (input instanceof StringValue) {
            return ((StringValue) input).getValue();
        }
        if (input instanceof IntValue) {
            return ((IntValue) input).getValue();
        }
        if (input instanceof BooleanValue) {
            return ((BooleanValue) input).isValue();
        }
        if (input instanceof EnumValue) {
            return ((EnumValue) input).getName();
        }
        if (input instanceof VariableReference) {
            String varName = ((VariableReference) input).getName();
            return variables.get(varName);
        }
        return Assert.assertShouldNeverHappen("We have covered all Value types", new Object[0]);
    }
}
