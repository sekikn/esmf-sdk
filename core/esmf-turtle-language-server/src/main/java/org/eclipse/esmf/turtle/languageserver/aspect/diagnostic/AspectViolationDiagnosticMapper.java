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

package org.eclipse.esmf.turtle.languageserver.aspect.diagnostic;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.esmf.aspectmodel.Location;
import org.eclipse.esmf.aspectmodel.Violation;
import org.eclipse.esmf.aspectmodel.ViolationReport;
import org.eclipse.esmf.aspectmodel.ValueParsingException;
import org.eclipse.esmf.aspectmodel.resolver.exceptions.ParserException;
import org.eclipse.esmf.aspectmodel.validation.InvalidLexicalValueViolation;
import org.eclipse.esmf.aspectmodel.validation.InvalidSyntaxViolation;
import org.eclipse.esmf.aspectmodel.validation.ProcessingViolation;
import org.eclipse.esmf.treesitterturtle.TurtleDocumentViolation;
import org.eclipse.esmf.treesitterturtle.TurtleViolationCode;

public class AspectViolationDiagnosticMapper implements Function<List<Violation>, ViolationReport> {
   public static final String PROCESSING_ERROR_MESSAGE = "Model validation failed. See language server logs for details.";

   @Override
   public ViolationReport apply( final List<Violation> violations ) {
      return mapValidationViolations( violations );
   }

   public ViolationReport mapValidationViolations( final List<Violation> violations ) {
      return new ViolationReport( violations.stream()
            .flatMap( violation -> mapViolation( violation ).stream() )
            .toList() );
   }

   public ViolationReport mapParserException( final ParserException exception, final URI sourceLocation ) {
      return new ViolationReport( new TurtleDocumentViolation( exception.getMessage(), TurtleViolationCode.ERR_SYNTAX,
            sourceLocation, new Location( line( exception ), column( exception ), line( exception ), column( exception ) + 1 ) ) );
   }

   public ViolationReport mapValueParsingException( final ValueParsingException exception ) {
      return new ViolationReport( mapLexicalViolation( lexicalViolation( exception ) ) );
   }

   public ViolationReport processingFailureReport( final Exception cause ) {
      return new ViolationReport( new ProcessingViolation( PROCESSING_ERROR_MESSAGE, cause ) );
   }

   private Optional<Violation> mapViolation( final Violation violation ) {
      if ( violation instanceof InvalidSyntaxViolation ) {
         return Optional.empty();
      }
      return Optional.of( violation );
      // if ( violation instanceof final InvalidLexicalValueViolation lexicalViolation ) {
      // return Optional.of( mapLexicalViolation( lexicalViolation ) );
      // }
      // if ( violation instanceof final ProcessingViolation processingViolation ) {
      // return Optional.of( mapProcessingViolation( processingViolation ) );
      // }
      // return Optional.of( mapSemanticViolation( violation ) );
   }

   private Violation mapLexicalViolation( final InvalidLexicalValueViolation violation ) {
      final AspectDiagnosticCode code = new AspectDiagnosticCode( InvalidLexicalValueViolation.ERROR_CODE );
      final Location diagnosticsLocation = new Location( Math.max( 0, violation.location().fromLine() ),
            Math.max( 0, violation.location().fromColumn() ),
            Math.max( 0, violation.location().fromLine() ),
            Math.max( 0, violation.location().fromColumn() + 1 ) );
      return new AspectDocumentViolation( violation.message(), code, violation.sourceDocument(), diagnosticsLocation );
      // return Optional.of( violation.location() )
      // .<Diagnostic>map(
      // _ -> new AspectDocumentDiagnostic( violation.message(), code, violation.sourceDocument(),
      // diagnosticsLocation ) )
      // .orElseGet( () -> new AspectDiagnostic( violation.message(), code ) );
   }

   private InvalidLexicalValueViolation lexicalViolation( final ValueParsingException exception ) {
      return new InvalidLexicalValueViolation(
            exception.getType(),
            exception.getValue(),
            new Location( (int) exception.getLine(), (int) exception.getColumn() ),
            sourceLine( exception ),
            exception.getSourceLocation() );
   }

   private String sourceLine( final ValueParsingException exception ) {
      if ( exception.getSourceDocument() == null || exception.getLine() < 1 ) {
         return "";
      }
      return exception.getSourceDocument().lines()
            .skip( exception.getLine() - 1 )
            .findFirst()
            .orElse( "" );
   }

   private int line( final ParserException exception ) {
      return Math.max( 0, (int) exception.getLine() - 1 );
   }

   private int column( final ParserException exception ) {
      return Math.max( 0, (int) exception.getColumn() - 1 );
   }
}
