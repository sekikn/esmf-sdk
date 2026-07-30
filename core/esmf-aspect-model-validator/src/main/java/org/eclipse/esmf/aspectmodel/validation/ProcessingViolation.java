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

import org.eclipse.esmf.aspectmodel.Violation;

/**
 * Generic violation: The validation was unsuccessful, for example because the model could not be
 * loaded or not be resolved
 *
 * @param message the detailed message
 * @param cause the cause
 */
public record ProcessingViolation(
      String message, Throwable cause
) implements Violation {
   public static final String ERROR_CODE = "ERR_PROCESSING";

   @Override
   public Code code() {
      return () -> ERROR_CODE;
   }

   // TODO
   // @Override
   // public @Nullable RDFNode highlight() {
   // return cause instanceof final AspectLoadingException aspectLoadingException ?
   // aspectLoadingException.highlightElement() : null;
   // }
}
