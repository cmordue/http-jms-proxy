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

import javax.jms.DeliveryMode;
import javax.jms.Session;

public class JmsConfig
{
    private int ackMode = Session.AUTO_ACKNOWLEDGE;
    private int deliveryMode = DeliveryMode.NON_PERSISTENT;
    private String queueName = "rr";
    private boolean transacted = false;

    public int getAckMode()
    {
        return ackMode;
    }
    
    public int getDeliveryMode()
    {
        return deliveryMode;
    }
    
    public String getQueueName()
    {
        return queueName;
    }
    
    public boolean isTransacted()
    {
        return transacted;
    }
}


