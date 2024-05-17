/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.expression.function.scalar.math;

import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;

import org.elasticsearch.xpack.esql.expression.function.AbstractFunctionTestCase;
import org.elasticsearch.xpack.esql.expression.function.TestCaseSupplier;
import org.elasticsearch.xpack.ql.expression.Expression;
import org.elasticsearch.xpack.ql.tree.Source;
import org.elasticsearch.xpack.ql.type.DataTypes;
import org.elasticsearch.xpack.ql.util.NumericUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.elasticsearch.xpack.esql.type.EsqlDataTypeConverter.unsignedLongToDouble;

public class CbrtTests extends AbstractFunctionTestCase {
    public CbrtTests(@Name("TestCase") Supplier<TestCaseSupplier.TestCase> testCaseSupplier) {
        this.testCase = testCaseSupplier.get();
    }

    @ParametersFactory
    public static Iterable<Object[]> parameters() {
        String read = "Attribute[channel=0]";
        List<TestCaseSupplier> suppliers = new ArrayList<>();
        // Valid values
        TestCaseSupplier.forUnaryInt(
            suppliers,
            "CbrtIntEvaluator[val=" + read + "]",
            DataTypes.DOUBLE,
            Math::cbrt,
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            List.of()
        );
        TestCaseSupplier.forUnaryLong(
            suppliers,
            "CbrtLongEvaluator[val=" + read + "]",
            DataTypes.DOUBLE,
            Math::cbrt,
            Long.MIN_VALUE,
            Long.MAX_VALUE,
            List.of()
        );
        TestCaseSupplier.forUnaryUnsignedLong(
            suppliers,
            "CbrtUnsignedLongEvaluator[val=" + read + "]",
            DataTypes.DOUBLE,
            ul -> Math.cbrt(unsignedLongToDouble(NumericUtils.asLongUnsigned(ul))),
            BigInteger.ZERO,
            UNSIGNED_LONG_MAX,
            List.of()
        );
        TestCaseSupplier.forUnaryDouble(
            suppliers,
            "CbrtDoubleEvaluator[val=" + read + "]",
            DataTypes.DOUBLE,
            Math::cbrt,
            Double.MIN_VALUE,
            Double.MAX_VALUE,
            List.of()
        );
        suppliers = anyNullIsNull(true, suppliers);

        return parameterSuppliersFromTypedData(errorsForCasesWithoutExamples(suppliers));
    }

    @Override
    protected Expression build(Source source, List<Expression> args) {
        return new Cbrt(source, args.get(0));
    }
}