/*
 * Copyright (c) 2025 Robert Bosch Manufacturing Solutions GmbH
 *
 * See the AUTHORS file(s) distributed with this work for additional
 * information regarding authorship.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.eclipse.esmf.aspectmodel.validation;

import java.net.URI;

import org.eclipse.esmf.aspectmodel.DocumentViolation;
import org.eclipse.esmf.aspectmodel.Location;

/**
 * Meta violation: Syntax error in source file
 *
 * @param message the error message
 * @param source the source code of the source document
 * @param location the location in the source file
 * @param sourceDocument the source location of the violation
 */
public record InvalidSyntaxViolation(
      String message, String source, Location location, URI sourceDocument
) implements DocumentViolation {
   public static final String ERROR_CODE = "ERR_SYNTAX";

   @Override
   public Code code() {
      return () -> ERROR_CODE;
   }
}
