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

package com.scottlogic.deg.output.writer;

import com.scottlogic.deg.common.util.FileUtils;
import com.scottlogic.deg.output.OutputPath;
import com.scottlogic.deg.output.outputtarget.FileOutputTarget;
import com.scottlogic.deg.output.outputtarget.OutputTargetValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class FileOutputTargetTests {
    @Mock
    private Path mockFilePath;
    @Mock
    private FileUtils mockFileUtils;
    @Mock
    private OutputWriterFactory mockOutputFormat;
    @Mock
    private Path mockParentPath;

    @Test
    public void validate_generateOutputFileIsADirectory_throwsException() {
        Mockito.when(mockFileUtils.isDirectory(Matchers.any())).thenReturn(true);
        Mockito.when(mockFilePath.getParent()).thenReturn(mockParentPath);
        OutputPath outputPath = new OutputPath(mockFilePath);
        FileOutputTarget outputTarget = new FileOutputTarget(outputPath, mockOutputFormat, false, mockFileUtils);

        assertThrows(OutputTargetValidationException.class, outputTarget::validate, "Expected OutputTargetValidationException to throw, but didn't");
    }

    @Test
    public void validate_generateOutputFileAlreadyExistsNoOverwrite_throwsException() {
        Mockito.when(mockFileUtils.isDirectory(mockFilePath)).thenReturn(false);
        Mockito.when(mockFileUtils.isDirectory(mockParentPath)).thenReturn(true);
        Mockito.when(mockFilePath.getParent()).thenReturn(mockParentPath);
        Mockito.when(mockFileUtils.exists(Matchers.any())).thenReturn(true);
        OutputPath outputPath = new OutputPath(mockFilePath);
        FileOutputTarget outputTarget = new FileOutputTarget(outputPath, mockOutputFormat, false, mockFileUtils);

        assertThrows(OutputTargetValidationException.class, outputTarget::validate, "Expected OutputTargetValidationException to throw, but didn't");
    }

    @Test
    public void validate_generateOutputFileAlreadyExistsOverwrite_doesntThrow() {
        Mockito.when(mockFileUtils.isDirectory(mockFilePath)).thenReturn(false);
        Mockito.when(mockFileUtils.isDirectory(mockParentPath)).thenReturn(true);
        Mockito.when(mockFilePath.getParent()).thenReturn(mockParentPath);
        Mockito.when(mockFileUtils.exists(Matchers.any())).thenReturn(true);
        OutputPath outputPath = new OutputPath(mockFilePath);
        FileOutputTarget outputTarget = new FileOutputTarget(outputPath, mockOutputFormat, true, mockFileUtils);

        assertDoesNotThrow(outputTarget::validate,"Expected no exception, but one was thrown");
    }

    @Test
    public void validate_generateOutputFileDoesntExist_doesntThrow() throws IOException {
        Mockito.when(mockFilePath.getParent()).thenReturn(mockParentPath);
        Mockito.when(mockFilePath.toAbsolutePath()).thenReturn(mockFilePath);
        Mockito.when(mockParentPath.resolve(mockFilePath.getFileName())).thenReturn(mockFilePath);
        Mockito.when(mockFileUtils.isDirectory(mockFilePath)).thenReturn(false);
        Mockito.when(mockFileUtils.createDirectories(Matchers.any())).thenReturn(true);
        OutputPath outputPath = new OutputPath(mockFilePath);
        FileOutputTarget outputTarget = new FileOutputTarget(outputPath, mockOutputFormat, true, mockFileUtils);

        assertDoesNotThrow(outputTarget::validate,"Expected no exception, but one was thrown");
    }

    @Test
    public void validate_generateOutputFileParentDirIsExistingFile_throwsException(){
        Mockito.when(mockFilePath.getParent()).thenReturn(mockParentPath);
        Mockito.when(mockFilePath.toAbsolutePath()).thenReturn(mockFilePath);
        Mockito.when(mockParentPath.resolve(mockFilePath.getFileName())).thenReturn(mockFilePath);
        Mockito.when(mockFileUtils.isDirectory(mockFilePath)).thenReturn(false);
        Mockito.when(mockFileUtils.isDirectory(mockParentPath)).thenReturn(false);
        OutputPath outputPath = new OutputPath(mockFilePath);
        FileOutputTarget outputTarget = new FileOutputTarget(outputPath, mockOutputFormat, false, mockFileUtils);

        assertThrows(OutputTargetValidationException.class, outputTarget::validate,"Expected OutputTargetValidationException to throw, but didn't");
    }
}

