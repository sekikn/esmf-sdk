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
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.esmf.aspectmodel.DocumentLocationViolation;
import org.eclipse.esmf.aspectmodel.ElementFocussedViolation;
import org.eclipse.esmf.aspectmodel.Location;
import org.eclipse.esmf.aspectmodel.resolver.parser.SmartToken;
import org.eclipse.esmf.aspectmodel.resolver.parser.TokenRegistry;
import org.eclipse.esmf.aspectmodel.shacl.ShaclValidationException;

/**
 * Base implementation for and RdfElementViolation that is based on information stored in the
 * {@link TokenRegistry}.
 */
public abstract class AbstractElementFocussedViolation implements ElementFocussedViolation {
   @Override
   public Location location() {
      return TokenRegistry.getToken( highlight().asNode() )
            .map( SmartToken::location ).orElse( DocumentLocationViolation.WHOLE_DOCUMENT );
   }

   @Override
   public URI sourceDocument() {
      return TokenRegistry.getToken( highlight().asNode() )
            .flatMap( token -> Optional.ofNullable( token.getSourceDocument() ) )
            .orElseThrow( () -> new ShaclValidationException( "Could not determine source document for element " + highlight() ) );
   }

   @Override
   public Supplier<String> documentContent() {
      return () -> TokenRegistry.getToken( highlight().asNode() )
            .map( token -> token.getOriginatingFile().sourceRepresentation() )
            .orElseThrow( () -> new ShaclValidationException( "Can not determine source document for element " + highlight() ) );
   }
}
