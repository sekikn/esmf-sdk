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

package org.eclipse.esmf.aspectmodel.resolver;

import java.net.URI;
import java.util.Optional;

import org.eclipse.esmf.aspectmodel.ProjectInfo;
import org.eclipse.esmf.aspectmodel.Violation;
import org.eclipse.esmf.aspectmodel.urn.AspectModelUrn;

import io.soabase.recordbuilder.core.RecordBuilder;

/**
 * Represents the failure to load (resolve) a document or a specific element from a document at a
 * given location.
 * 
 * @param element the model element that could not be resolved, if known
 * @param location the location (e.g. URL) that could not be resolved
 * @param message the specific message
 * @param cause the underlying cause, for example a FileNotFoundException
 */
@RecordBuilder
public record ModelResolutionViolation(
      Optional<AspectModelUrn> element,
      URI location,
      String message,
      Optional<Throwable> cause
) implements Violation {
   public static final String ERROR_CODE = "ERR_MODEL_RESOLUTION";

   public ModelResolutionViolation( final URI location, final String message, final Throwable cause ) {
      this( Optional.empty(), location, message, Optional.of( cause ) );
   }

   public ModelResolutionViolation( final AspectModelUrn element, final URI location, final String message ) {
      this( Optional.of( element ), location, message, Optional.empty() );
   }

   public ModelResolutionViolation( final AspectModelUrn element, final URI location, final String message, final Throwable cause ) {
      this( Optional.of( element ), location, message, Optional.of( cause ) );
   }

   @Override
   public Code code() {
      return new Code() {
         @Override
         public String code() {
            return ERROR_CODE;
         }

         @Override
         public Optional<String> href() {
            return Optional.of( ProjectInfo.esmfErrorCodeUrl( ERROR_CODE ) );
         }
      };
   }
}
