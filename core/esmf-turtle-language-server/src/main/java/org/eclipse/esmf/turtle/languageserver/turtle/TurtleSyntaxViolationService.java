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

package org.eclipse.esmf.turtle.languageserver.turtle;

import java.net.URI;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.esmf.aspectmodel.Violation;
import org.eclipse.esmf.aspectmodel.ViolationReport;
import org.eclipse.esmf.aspectmodel.Location;
import org.eclipse.esmf.treesitterturtle.TurtleViolationCode;
import org.eclipse.esmf.treesitterturtle.TurtleDocumentViolation;
import org.eclipse.esmf.turtle.languageserver.lsp.diagnostic.ViolationProvider;
import org.eclipse.esmf.turtle.languageserver.lsp.text.ParsedDocument;

import org.treesitter.TSNode;

public class TurtleSyntaxViolationService implements ViolationProvider {
   @Override
   public ViolationReport validate( final ParsedDocument parsedDocument ) {
      return new ViolationReport( checkNode( parsedDocument.concreteSyntaxTree().getRootNode(),
            parsedDocument.sourceDocument().uri() ).toList() );
   }

   private Stream<Violation> checkNode( final TSNode node, final URI sourceLocation ) {
      return Stream.concat( node.isError() || node.isMissing() ? Stream.of( violationForNode( node, sourceLocation ) ) : Stream.empty(),
            IntStream.range( 0, node.getChildCount() ).boxed().map( node::getChild )
                  .flatMap( child -> checkNode( child, sourceLocation ) ) );
   }

   private TurtleDocumentViolation violationForNode( final TSNode node, final URI sourceLocation ) {
      final String message;
      if ( node.isMissing() ) {
         message = "Syntax error: Missing '" + node.getGrammarType() + "'";
      } else {
         message = "Syntax error";
      }
      final Location location = new Location( node.getStartPoint().getRow(), node.getStartPoint().getColumn(), node.getEndPoint().getRow(),
            node.getEndPoint().getColumn() );
      return new TurtleDocumentViolation( message, TurtleViolationCode.ERR_SYNTAX, sourceLocation, location );
   }
}
