/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-07 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id: NodeSerializer.java 9694 2009-08-07 20:29:30Z dizzzz $
 */
package org.exist.validation.internal.node;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.log4j.Logger;
import org.exist.storage.io.ExistIOException;
import org.exist.storage.serializers.Serializer;
import org.exist.util.serializer.SAXSerializer;
import org.exist.util.serializer.SerializerPool;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.SequenceIterator;

/**
 * @author Dannes Wessels (dizzzz@exist-db.org)
 */
public class NodeSerializer {
    
    private final static Logger LOG = Logger.getLogger(NodeSerializer.class);
      
    public static void serialize(Serializer serializer, NodeValue node,
        Properties outputProperties, OutputStream os) throws IOException {
        
        LOG.debug("Serializing started.");
        SAXSerializer sax = (SAXSerializer) SerializerPool.getInstance().borrowObject(SAXSerializer.class);
        try {
            String encoding = outputProperties.getProperty(OutputKeys.ENCODING, "UTF-8");
            Writer writer = new OutputStreamWriter(os, encoding);
            sax.setOutput(writer, outputProperties);

            serializer.reset();
            serializer.setProperties(outputProperties);
            serializer.setSAXHandlers(sax, sax);

            
            sax.startDocument();
            serializer.toSAX(node);
            
//            while(node.hasNext()) {
//                NodeValue next = (NodeValue)node.nextItem();
//                serializer.toSAX(next);
//            }
            
            sax.endDocument();
            writer.close();
            
        } catch(Exception e) {
            String txt = "A problem ocurred while serializing the node set";
            LOG.debug(txt+".", e);
            throw new ExistIOException(txt+": " + e.getMessage(), e);
        
        } finally {
            LOG.debug("Serializing done.");
            SerializerPool.getInstance().returnObject(sax);
        }
    }
    
    
}
