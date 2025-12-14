package hu.webarticum.miniconnect.generaldocs.examples.holodbcustomized;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hu.webarticum.holodb.core.data.binrel.permutation.ModuloPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.data.source.NullPaddedSortedSource;
import hu.webarticum.holodb.core.data.source.PermutatedIndexedSource;
import hu.webarticum.holodb.core.data.source.SortedSource;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.holodb.core.data.source.UniqueSource;
import hu.webarticum.holodb.spi.config.ColumnLocation;
import hu.webarticum.holodb.spi.config.SourceFactory;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class ItemCodeSourceFactory implements SourceFactory {

    private static final String LENGTH_KEY = "length";

    private static final int DEFAULT_LENGTH = 5;


    @Override
    public Source<?> create(ColumnLocation columnLocation, TreeRandom treeRandom, LargeInteger size, Object data) {
        int codeLength = detectCodeLength(data);
        LargeInteger domainSize = LargeInteger.of(Character.MAX_RADIX).pow(codeLength);
        LargeInteger baseSize = size;
        LargeInteger nullCount = LargeInteger.ZERO;
        if (baseSize.isGreaterThan(domainSize)) {
            baseSize = domainSize;
            nullCount = size.subtract(domainSize);
        }
        IndexedSource<String> source = createBaseSource(baseSize, codeLength, domainSize);
        if (!nullCount.isZero()) {
            source = new NullPaddedSortedSource<>((SortedSource<String>) source, size);
        }
        Permutation permutation = new ModuloPermutation(treeRandom, size);
        return new PermutatedIndexedSource<>(source, permutation);
    }

    private int detectCodeLength(Object data) {
        if (data instanceof Map) {
            Object value = ((Map<?, ?>) data).get(LENGTH_KEY);
            if (value instanceof Number) {
                int intValue = ((Number) value).intValue();
                if (intValue > 0) {
                    return intValue;
                }
            }
        }

        return DEFAULT_LENGTH;
    }

    private UniqueSource<String> createBaseSource(LargeInteger size, int codeLength, LargeInteger domainSize) {
        int sizeInt = size.intValueExact();
        LargeInteger step = domainSize.divide(size);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < sizeInt; i++) {
            values.add(createNthCode(i, codeLength, step));
        }
        return new UniqueSource<>(String.class, values);
    }

    private String createNthCode(int i, int codeLength, LargeInteger step) {
        StringBuilder resultBuilder = new StringBuilder();
        LargeInteger number = LargeInteger.of(i).multiply(step);
        String str = number.toString(Character.MAX_RADIX);
        resultBuilder.append(str);
        int length = str.length();
        for (int j = codeLength; j > length; j--) {
            resultBuilder.insert(0, '0');
        }
        return resultBuilder.toString();
    }

}
