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

import org.apache.jena.rdf.model.Resource;

import org.eclipse.esmf.aspectmodel.DocumentViolation;
import org.eclipse.esmf.aspectmodel.Location;
import org.eclipse.esmf.aspectmodel.RdfUtil;

/**
 * A value (literal) value was given with an invalid value, e.g., "9999"^^xsd:byte
 *
 * @param type the URI of the type
 * @param value the invalid value
 * @param location the source location of the violation
 * @param sourceLine the line in the source document containing the problem
 * @param sourceDocument the source location of the violation
 */
public record InvalidLexicalValueViolation(
      Resource type, Object value, Location location, String sourceLine, URI sourceDocument
) implements DocumentViolation {
   public static final String ERROR_CODE = "ERR_INVALID_LEXICAL_VALUE";

   public InvalidLexicalValueViolation( final Resource type, final Object value, final int fromLine, final int toLine,
         final String sourceLine, final URI sourceDocument ) {
      this( type, value, new Location( fromLine, toLine ), sourceLine, sourceDocument );
   }

   @Override
   public Code code() {
      return () -> ERROR_CODE;
   }

   @Override
   public String message() {
      return "'%s' is no valid value for type %s".formatted( value, RdfUtil.curie( type.getURI() ) );
   }
}
