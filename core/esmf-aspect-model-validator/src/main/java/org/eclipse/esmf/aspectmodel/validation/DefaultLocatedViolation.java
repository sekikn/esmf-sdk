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

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.eclipse.esmf.aspectmodel.DocumentViolation;
import org.eclipse.esmf.aspectmodel.Location;
import org.eclipse.esmf.aspectmodel.RdfElementViolation;
import org.eclipse.esmf.aspectmodel.resolver.parser.SmartToken;
import org.eclipse.esmf.aspectmodel.resolver.parser.TokenRegistry;
import org.eclipse.esmf.aspectmodel.shacl.ShaclValidationException;
import org.eclipse.esmf.aspectmodel.shacl.fix.Fix;

public abstract class DefaultLocatedViolation implements RdfElementViolation {
   @Override
   public Location location() {
      return TokenRegistry.getToken( highlight().asNode() )
            .map( SmartToken::location ).orElse( DocumentViolation.WHOLE_DOCUMENT );
   }

   @Override
   public URI sourceDocument() {
      return TokenRegistry.getToken( highlight().asNode() )
            .flatMap( token -> Optional.ofNullable( token.getSourceDocument() ) )
            .orElseThrow( () -> new ShaclValidationException( "Could not determine source document for element " + highlight() ) );
   }

   public List<Fix> fixes() {
      return List.of();
   }
}
