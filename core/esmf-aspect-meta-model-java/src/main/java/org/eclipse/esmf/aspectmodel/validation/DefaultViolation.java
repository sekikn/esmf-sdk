/*
 * Copyright (c) 2026 Robert Bosch Manufacturing Solutions GmbH
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

import org.eclipse.esmf.aspectmodel.Violation;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record DefaultViolation(
      String message,
      Violation.Code code,
      Violation.Severity severity
) implements Violation {
   public DefaultViolation {
      if ( severity == null ) {
         severity = Severity.ERROR;
      }
   }
}
