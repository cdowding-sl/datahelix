/*
 * Copyright 2019 Scott Logic Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scottlogic.deg.profile.reader.file;

import com.scottlogic.deg.generator.fieldspecs.whitelist.DistributedList;
import com.scottlogic.deg.generator.fieldspecs.whitelist.WeightedElement;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvInputStreamReader {

    private CsvInputStreamReader() {
        throw new UnsupportedOperationException("No instantiation of static class");
    }

    public static DistributedList<String> retrieveLines(InputStream stream) {
        List<CSVRecord> records = parse(stream);
        return new DistributedList<>(records.stream()
            .map(CsvInputStreamReader::createWeightedElement)
            .collect(Collectors.toList()));
    }

    private static WeightedElement<String> createWeightedElement(CSVRecord record) {
        if (record.size() > 1) {
            return new WeightedElement<>(record.get(0), Double.parseDouble(record.get(1)));
        } else {
            return WeightedElement.withDefaultWeight(record.get(0));
        }
    }

    private static List<CSVRecord> parse(InputStream stream) {
        try {
            CSVParser parser = CSVParser.parse(stream, Charset.defaultCharset(), CSVFormat.DEFAULT);
            return parser.getRecords();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
