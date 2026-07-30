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

package org.eclipse.esmf.treesitterturtle;

import java.net.URI;

import org.eclipse.esmf.aspectmodel.DocumentViolation;
import org.eclipse.esmf.aspectmodel.Location;
import org.eclipse.esmf.aspectmodel.Violation;

public record TurtleDocumentViolation(
      String message,
      TurtleViolationCode code,
      URI sourceDocument,
      Location location,
      Violation.Severity severity
) implements DocumentViolation {
   public TurtleDocumentViolation( final String message, final TurtleViolationCode code,
         final URI sourceDocument, final Location location ) {
      this( message, code, sourceDocument, location, Violation.Severity.ERROR );
   }
}
