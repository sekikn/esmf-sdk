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

package org.eclipse.esmf.aspectmodel.versionupdate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.esmf.aspectmodel.urn.AspectModelUrn;
import org.eclipse.esmf.aspectmodel.urn.ElementType;
import org.eclipse.esmf.samm.KnownVersion;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

/**
 * Checks an Aspect Model file against the SAMM meta model version it declares.
 *
 * <p>
 * The check must run on the parsed but not yet migrated RDF graph: migration rewrites meta model
 * URIs to the latest version without verifying that the rewritten term existed in the source
 * version,
 * so afterwards a file that declared an older version is indistinguishable from a correct one.
 *
 * <p>
 * Two rules are enforced:
 * <ol>
 * <li>Every meta model term the file uses must be defined in the version the file declares.</li>
 * <li>Every meta model URI must carry the declared version, so that a single file does not mix
 * namespaces of different meta model versions.</li>
 * </ol>
 */
final class MetaModelVersionCheck {
   private MetaModelVersionCheck() {}

   /**
    * Checks the given model against the meta model version it declares.
    *
    * @param model the parsed, not yet migrated RDF model of a single Aspect Model file
    * @param declaredVersion the meta model version the file declares
    * @param sourceLocation the human readable location of the file, used in the messages
    * @return the messages describing the inconsistencies, empty if the file is consistent
    */
   static List<String> check( final Model model, final KnownVersion declaredVersion, final String sourceLocation ) {
      // Keyed by term URI so that a term used many times is only reported once
      final Map<String, String> messages = new LinkedHashMap<>();
      for ( final RDFNode node : metaModelNodes( model ) ) {
         AspectModelUrn.from( node.asResource().getURI() ).toJavaOptional()
               .filter( MetaModelTerms::isMetaModelTerm )
               .flatMap( urn -> checkTerm( urn, declaredVersion, sourceLocation )
                     .map( message -> Map.entry( urn.getUrn().toString(), message ) ) )
               .ifPresent( entry -> messages.putIfAbsent( entry.getKey(), entry.getValue() ) );
      }
      return List.copyOf( messages.values() );
   }

   private static Optional<String> checkTerm( final AspectModelUrn urn, final KnownVersion declaredVersion,
         final String sourceLocation ) {
      if ( !declaredVersion.toVersionString().equals( urn.getVersion() ) ) {
         return Optional.of( String.format(
               "%s: this file declares SAMM %s but uses %s from SAMM %s. All SAMM namespaces in a file must use the same "
                     + "meta model version.",
               sourceLocation, declaredVersion.toVersionString(), urn.getUrn(), urn.getVersion() ) );
      }

      // Unit definitions are not part of the term inventory, see MetaModelTerms
      if ( urn.getElementType() == ElementType.UNIT || MetaModelTerms.isDefinedIn( urn, declaredVersion ) ) {
         return Optional.empty();
      }

      // Only a term that a *newer* meta model version defines indicates that the file declares too low a
      // version. A term that no version knows is either a typo, which SHACL reports with a more specific
      // message, or an element a model legitimately adds to one of the SAMM namespaces. A term that only
      // an *older* version defines was removed rather than added, which the migrators handle: samm:name,
      // for example, exists in SAMM 1.0.0 and is dropped by SammRemoveSammNameMigrator.
      return MetaModelTerms.firstVersionDefining( urn )
            .filter( introducedIn -> introducedIn.isNewerThan( declaredVersion ) )
            .map( introducedIn -> String.format(
                  "%s: %s is not defined in SAMM %s (introduced in %s). Change the meta model version declared in this "
                        + "file to at least %s.",
                  sourceLocation, urn.getUrn(), declaredVersion.toVersionString(), introducedIn.toVersionString(),
                  introducedIn.toVersionString() ) );
   }

   /**
    * Collects all nodes of the model that may address a meta model term. Datatypes of literals are
    * included because they are versioned as well and are rewritten during migration, see
    * {@link AbstractUriRewriter#updateLiteral}.
    */
   private static List<RDFNode> metaModelNodes( final Model model ) {
      final List<RDFNode> nodes = new ArrayList<>();
      for ( final Statement statement : model.listStatements().toList() ) {
         addIfUriResource( statement.getSubject(), nodes );
         addIfUriResource( statement.getPredicate(), nodes );
         final RDFNode object = statement.getObject();
         if ( object.isLiteral() ) {
            Optional.ofNullable( object.asLiteral().getDatatypeURI() )
                  .map( model::createResource )
                  .ifPresent( nodes::add );
         } else {
            addIfUriResource( object, nodes );
         }
      }
      return nodes;
   }

   private static void addIfUriResource( final RDFNode node, final List<RDFNode> nodes ) {
      if ( node.isURIResource() ) {
         nodes.add( node );
      }
   }
}
