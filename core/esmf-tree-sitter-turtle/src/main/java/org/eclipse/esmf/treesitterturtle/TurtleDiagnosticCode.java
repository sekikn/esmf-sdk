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

import org.eclipse.esmf.Diagnostic;

public enum TurtleDiagnosticCode implements Diagnostic.Code {
   ERR_UNCATEGORIZED( "No more info available" ),
   ERR_SYNTAX( "Syntax error" ),
   ERR_MISSING_TOKEN( "Missing token" );

   private final String description;

   TurtleDiagnosticCode( final String description ) {
      this.description = description;
   }

   @Override
   public String code() {
      return name();
   }

   @Override
   public String description() {
      return description;
   }
}
