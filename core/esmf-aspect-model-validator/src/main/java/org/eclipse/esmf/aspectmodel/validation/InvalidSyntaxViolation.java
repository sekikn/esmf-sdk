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
import java.util.Optional;

import org.eclipse.esmf.DocumentDiagnostic;
import org.eclipse.esmf.Location;
import org.eclipse.esmf.aspectmodel.shacl.violation.EvaluationContext;
import org.eclipse.esmf.aspectmodel.shacl.violation.Violation;

import org.jspecify.annotations.Nullable;

/**
 * Meta violation: Syntax error in source file
 *
 * @param violationSpecificMessage the message for this violation
 * @param source the source code of the source document
 * @param location the location in the source file
 * @param sourceDocument the source location of the violation
 */
public record InvalidSyntaxViolation(
      String violationSpecificMessage, String source, Location location, URI sourceDocument
) implements Violation, DocumentDiagnostic {
   public static final String ERROR_CODE = "ERR_SYNTAX";

   @Override
   public String errorCode() {
      return ERROR_CODE;
   }

   @Override
   public @Nullable EvaluationContext context() {
      return null;
   }

   @Override
   public String message() {
      return "Syntax error";
   }

   @Override
   public Optional<URI> sourceLocation() {
      return Optional.of( sourceDocument() );
   }

   @Override
   public <T> T accept( final Visitor<T> visitor ) {
      return visitor.visitInvalidSyntaxViolation( this );
   }
}
