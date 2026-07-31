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
import org.eclipse.esmf.aspectmodel.ValueParsingException;
import org.eclipse.esmf.aspectmodel.Violation;
import org.eclipse.esmf.aspectmodel.ViolationReport;
import org.eclipse.esmf.aspectmodel.resolver.exceptions.ParserException;
import org.eclipse.esmf.aspectmodel.validation.InvalidLexicalValueViolation;
import org.eclipse.esmf.aspectmodel.validation.InvalidSyntaxViolation;
import org.eclipse.esmf.aspectmodel.validation.ProcessingViolation;
import org.eclipse.esmf.treesitterturtle.TurtleDocumentViolation;
import org.eclipse.esmf.treesitterturtle.TurtleViolationCode;

public class AspectViolationDiagnosticMapper implements Function<List<Violation>, ViolationReport> {
   public static final String PROCESSING_ERROR_MESSAGE = "Model validation failed. See language server logs for details.";
   private static final String ERROR_CODES_DOC_LINK = "https://eclipse-esmf.github.io/esmf-developer-guide/tooling-guide/error-codes.html#";

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
//   public DiagnosticReport processingFailureReport() {
//      return new DiagnosticReport(
//            new AspectDiagnostic( PROCESSING_ERROR_MESSAGE,
//                  new AspectDiagnosticCode( ProcessingViolation.ERROR_CODE,
//                        Optional.of( ERROR_CODES_DOC_LINK + ProcessingViolation.ERROR_CODE.toUpperCase().replace( "_", "-" ) ) ) ) );
   }

   private Optional<Violation> mapViolation( final Violation violation ) {
      if ( violation instanceof InvalidSyntaxViolation ) {
         return Optional.empty();
      }
      return Optional.of( violation );
   }

   private Violation mapLexicalViolation( final InvalidLexicalValueViolation violation ) {
      final AspectDiagnosticCode code = new AspectDiagnosticCode( InvalidLexicalValueViolation.ERROR_CODE );
      final Location diagnosticsLocation = new Location( Math.max( 0, violation.location().fromLine() ),
            Math.max( 0, violation.location().fromColumn() ),
            Math.max( 0, violation.location().fromLine() ),
            Math.max( 0, violation.location().fromColumn() + 1 ) );
      return new AspectDocumentViolation( violation.message(), code, violation.sourceDocument(), diagnosticsLocation );
   // private Diagnostic<AspectDiagnosticCode> mapLexicalViolation( final InvalidLexicalValueViolation violation ) {
   //    final AspectDiagnosticCode code = new AspectDiagnosticCode( InvalidLexicalValueViolation.ERROR_CODE, Optional.empty() );
   //    final Location diagnosticsLocation = new Location( Math.max( 0, violation.line() - 1 ),
   //          Math.max( 0, violation.column() - 1 ),
   //          Math.max( 0, violation.line() - 1 ),
   //          Math.max( 0, violation.column() ) );
   //    return Optional.ofNullable( violation.location() )
   //          .<Diagnostic<AspectDiagnosticCode>>map( location -> new AspectDocumentDiagnostic(
   //                violation.message(),
   //                code,
   //                location.toString(),
   //                diagnosticsLocation
   //          ) )
   //          .orElseGet( () -> new AspectDiagnostic( violation.message(), code ) );
   }

   private Diagnostic<AspectDiagnosticCode> mapProcessingViolation( final ProcessingViolation violation ) {
      return mapViolationWithOptionalLocation( violation,
            new AspectDiagnosticCode( ProcessingViolation.ERROR_CODE,
                  Optional.of( ERROR_CODES_DOC_LINK + violation.errorCode().toUpperCase().replace( "_", "-" ) ) ) );
   }

   private Diagnostic<AspectDiagnosticCode> mapSemanticViolation( final Violation violation ) {
      return mapViolationWithOptionalLocation( violation, new AspectDiagnosticCode( violation.errorCode(), Optional.empty() ) );
   }

   private Diagnostic<AspectDiagnosticCode> mapViolationWithOptionalLocation( final Violation violation, final AspectDiagnosticCode code ) {
      final Location location = Optional.ofNullable( violation.highlight() )
            .map( RDFNode::asNode )
            .flatMap( TokenRegistry::getToken )
            .map( SmartToken::location )
            .orElse( null );
      if ( location == null ) {
         return new AspectDiagnostic( violation.message(), code );
      }
      return violation.sourceLocation()
            .map( URI::toString )
            .<Diagnostic<AspectDiagnosticCode>>map( sourceLocation -> new AspectDocumentDiagnostic(
                  violation.message(),
                  code,
                  sourceLocation,
                  location
            ) )
            .orElseGet( () -> new AspectDiagnostic( violation.message(), code ) );
>>>>>>> main
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
