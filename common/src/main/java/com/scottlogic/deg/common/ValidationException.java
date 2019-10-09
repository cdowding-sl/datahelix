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

package com.scottlogic.deg.common;

import java.util.Collections;
import java.util.List;

public class ValidationException extends RuntimeException {
    public final List<String> errorMessages;

    public ValidationException(String msg) {
        super(msg);
        errorMessages = Collections.singletonList(msg);
    }

    public ValidationException(List<String> errorMessages){
        super(String.join("\n", errorMessages));
        this.errorMessages = errorMessages;
    }

}
