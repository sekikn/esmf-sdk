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

import java.util.Optional;

import org.apache.jena.rdf.model.RDFNode;

import org.eclipse.esmf.aspectmodel.resolver.parser.TokenRegistry;
import org.eclipse.esmf.aspectmodel.shacl.violation.EvaluationContext;

/**
 * Violation for regular expressions that are too complex to automatically generate example values.
 *
 */
public final class RegularExpressionConstraintViolation extends DefaultLocatedViolation {
   public static final String ERROR_CODE = "ERR_INVALID_REGEX";
   private final EvaluationContext context;
   private final String regexp;

   /**
    * @param context the evaluation context
    * @param regexp the problematic regular expression
    */
   public RegularExpressionConstraintViolation( final EvaluationContext context, final String regexp ) {
      this.context = context;
      this.regexp = regexp;
   }

   @Override
   public Code code() {
      return () -> ERROR_CODE;
   }

   @Override
   public String message() {
      return "Regular expression on %s is invalid: '%s'.".formatted( context.value( context.element() ), regexp );
   }

   @Override
   public RDFNode highlight() {
      return Optional.ofNullable( context.element() )
            .filter( property -> TokenRegistry.getToken( property.asNode() ).isPresent() )
            .map( resource -> resource.as( RDFNode.class ) )
            .orElse( context.element() );
   }

   public String regexp() {
      return regexp;
   }
}
