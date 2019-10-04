package com.scottlogic.deg.profile.reader.atomic;

import com.google.inject.Inject;
import com.scottlogic.deg.common.ValidationException;
import com.scottlogic.deg.common.profile.DataType;
import com.scottlogic.deg.common.profile.constraintdetail.AtomicConstraintType;
import com.scottlogic.deg.common.util.NumberUtils;
import com.scottlogic.deg.generator.fieldspecs.whitelist.DistributedList;
import com.scottlogic.deg.profile.dtos.constraints.ConstraintDTO;
import com.scottlogic.deg.profile.reader.InvalidProfileException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AtomicConstraintValueReader {

    private final FromFileReader fromFileReader;

    @Inject
    public AtomicConstraintValueReader(FromFileReader fromFileReader) {
        this.fromFileReader = fromFileReader;
    }


    public Object getValue(ConstraintDTO dto, DataType type){
        try {
            return tryGetValue(dto, type);
        } catch (IllegalArgumentException | ValidationException e){
            throw new InvalidProfileException(String.format("Field [%s]: %s", dto.field, e.getMessage()));
        }
    }

    private Object tryGetValue(ConstraintDTO dto, DataType type){
        if (dto.values != null){
            return getSet(dto.values, type);
        }

        if (dto.file != null && dto.is.equals(AtomicConstraintType.IS_IN_SET.getText())){
            return fromFileReader.setFromFile(dto.file);
        }

        if (dto.file != null && dto.is.equals(AtomicConstraintType.IS_IN_MAP.getText())){
            return fromFileReader.listFromMapFile(dto.file, dto.key);
        }

        return getValue(dto.value, type);
    }

    private DistributedList getSet(Collection<Object> values, DataType type) {
        List collect = values.stream()
            .map(val -> getValue(val, type))
            .distinct()
            .collect(Collectors.toList());
        return DistributedList.uniform(collect);
    }

    private Object getValue(Object value, DataType type) {
        if (value instanceof Map){
            return getDate((Map) value);
        }
        if (type == DataType.NUMERIC){
            return getBigDecimal(value);
        }

        return value;
    }

    private Object getBigDecimal(Object value) {
        BigDecimal bigDecimal = NumberUtils.coerceToBigDecimal(value);
        return bigDecimal == null ? value : bigDecimal;
    }


    private OffsetDateTime getDate(Map value) {
        return ConstraintReaderHelpers.parseDate((String) (value).get("date"));
    }
}
