/*
 * Copyright (c) 2026 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */

package org.eclipse.esmf;

/**
 * Generic diagnostic interface representing a problem found during document processing.
 */
public interface Diagnostic {
   /**
    * The code identifying this diagnostic
    *
    * @return the code
    */
   Diagnostic.Code code();

   /**
    * The human-readable message for this diagnostic
    *
    * @return the message
    */
   String message();

   /**
    * The severity of this diagnostic
    *
    * @return the severity
    */
   Severity severity();

   /**
    * Identifies the kind of problem.
    */
   interface Code {
      String code();

      String description();
   }

   enum Severity {
      ERROR,
      WARNING,
      INFO,
      HINT
   }
}
