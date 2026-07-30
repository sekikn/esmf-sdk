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

package org.eclipse.esmf.aspectmodel;

import java.net.URI;

/**
 * A violation that refers to a specific location within a document. This interface extends the
 * base Violation interface and adds methods to retrieve the source location of the document and
 * the specific location within that document that the diagnostic refers to.
 */
public interface DocumentViolation extends Violation {
   Location WHOLE_DOCUMENT = new Location( 0, 0, 0, 0 );

   /**
    * Identifier for the location of the document which this violation refers to
    *
    * @return the document source location
    */
   URI sourceDocument();

   /**
    * The location within the document this violation refers to
    *
    * @return the location within the document
    */
   Location location();

   /**
    * Indicated whether the diagnostic applies to the whole document rather than a specific location
    *
    * @return true if the diagnostic is about the document as a whole
    */
   default boolean appliesToWholeDocument() {
      return location() == WHOLE_DOCUMENT;
   }
}
