/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package name.mordue.tools.jms;

public class ServiceException extends Exception
{
    public ServiceException() {
        super();
    }
    
    public ServiceException(String m) {
        super(m);
    }
    
    public ServiceException(Throwable t) {
        super(t);
    }
    
    public ServiceException(String m, Throwable t) {
        super(m, t);
    }
}


