/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2001-2009 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 *  $Id: FunId.java 9882 2009-08-25 13:20:52Z ellefj $
 */
package org.exist.xquery.functions;

import org.apache.log4j.Logger;

import org.exist.dom.DefaultDocumentSet;
import org.exist.dom.DocumentSet;
import org.exist.dom.ExtArrayNodeSet;
import org.exist.dom.MutableDocumentSet;
import org.exist.dom.NodeProxy;
import org.exist.dom.NodeSet;
import org.exist.dom.QName;
import org.exist.memtree.DocumentImpl;
import org.exist.memtree.NodeImpl;
import org.exist.util.XMLChar;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Constants;
import org.exist.xquery.Dependency;
import org.exist.xquery.Expression;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.Profiler;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 *
 * @author wolf
 * @author perig
 *
 */
public class FunId extends Function {
	protected static final Logger logger = Logger.getLogger(FunId.class);
	public final static FunctionSignature signature[] = {
			new FunctionSignature(
				new QName("id", Function.BUILTIN_FUNCTION_NS),
				"Returns the sequence of element nodes that have an ID value " +
				"matching the value of one or more of the IDREF values supplied in $idrefs. " +
				"If none is matching or $idrefs is the empty sequence, returns the empty sequence.",
				new SequenceType[] {
                    new FunctionParameterSequenceType("idrefs", Type.STRING, Cardinality.ZERO_OR_MORE, "The IDREF sequence")
                },
				new FunctionReturnSequenceType(Type.ELEMENT, Cardinality.ZERO_OR_MORE, "the elements with IDs  matching IDREFs from $idref-sequence")),
            new FunctionSignature(
                    new QName("id", Function.BUILTIN_FUNCTION_NS),
                    "Returns the sequence of element nodes that have an ID value " +
                    "matching the value of one or more of the IDREF values supplied in $idrefs and is in the same document as $node-in-document. " +
                    "If none is matching or $idrefs is the empty sequence, returns the empty sequence.",
                    new SequenceType[] {
                        new FunctionParameterSequenceType("idrefs", Type.STRING, Cardinality.ZERO_OR_MORE, "The IDREF sequence"),
                        new FunctionParameterSequenceType("node-in-document", Type.NODE, Cardinality.EXACTLY_ONE, "The node in document")
                    },
                    new FunctionReturnSequenceType(Type.ELEMENT, Cardinality.ZERO_OR_MORE, "the elements with IDs matching IDREFs from $idrefs in the same document as $node-in-document"))
    };

	/**
	 * Constructor for FunId.
	 */
	public FunId(XQueryContext context, FunctionSignature signature) {
		super(context, signature);
	}

	/**
	 * @see org.exist.xquery.Expression#eval(Sequence, Item)
	 */
	public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);
            context.getProfiler().message(this, Profiler.DEPENDENCIES, "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT SEQUENCE", contextSequence);
            if (contextItem != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT ITEM", contextItem.toSequence());
        }

        if (getArgumentCount() < 1) {
            logger.error("function id requires one argument");
			throw new XPathException(this, "function id requires one argument");
        }
        if(contextItem != null)
			contextSequence = contextItem.toSequence();

        Sequence result;
        boolean processInMem = false;
        Expression arg = getArgument(0);
		Sequence idval = arg.eval(contextSequence);
        if(idval.isEmpty() || (getArgumentCount() == 1 && contextSequence != null && contextSequence.isEmpty()))
            result = Sequence.EMPTY_SEQUENCE;
        else {
    		String nextId;
    		DocumentSet docs = null;
            if (getArgumentCount() == 2) {
                // second argument should be a node, whose owner document will be
                // searched for the id
                Sequence nodes = getArgument(1).eval(contextSequence);
                if (nodes.isEmpty()) {
                    logger.error("XPDY0002: no node or context item for fn:id");
                    throw new XPathException(this,
                            "XPDY0002: no node or context item for fn:id");
                }
                if (!Type.subTypeOf(nodes.itemAt(0).getType(), Type.NODE)) {
                    logger.error("XPTY0004: fn:id() argument is not a node");
                	throw new XPathException(this,
                    "XPTY0004: fn:id() argument is not a node");
                }
                NodeValue node = (NodeValue)nodes.itemAt(0);
                if (node.getImplementationType() == NodeValue.IN_MEMORY_NODE)
                	//TODO : how to enforce this ?
                	//If $node, or the context item if the second argument is omitted, 
                	//is a node in a tree whose root is not a document node [err:FODC0001] is raised                    processInMem = true;
                    processInMem = true;
                else {
                    MutableDocumentSet ndocs = new DefaultDocumentSet();
                    ndocs.add(((NodeProxy)node).getDocument());
                    docs = ndocs;
                }
                contextSequence = node;
            } else if (contextSequence == null) {
                logger.error("XPDY0002: no context item specified");
                throw new XPathException(this, "XPDY0002: no context item specified");
           } else if(!Type.subTypeOf(contextSequence.getItemType(), Type.NODE)) {
                logger.error("XPTY0004: context item is not a node");
    			throw new XPathException(this, "XPTY0004: context item is not a node");
    		} else {
    			if (contextSequence.isPersistentSet())
                    docs = contextSequence.toNodeSet().getDocumentSet();
                else
                    processInMem = true;
            }

            if (processInMem)
                result = new ValueSequence();
            else
                result = new ExtArrayNodeSet();

            for(SequenceIterator i = idval.iterate(); i.hasNext(); ) {
    			nextId = i.nextItem().getStringValue();
                if (nextId.length() == 0)
                    continue;
    			if(nextId.indexOf(" ") != Constants.STRING_NOT_FOUND) {
    				// parse idrefs
    				StringTokenizer tok = new StringTokenizer(nextId, " ");
    				while(tok.hasMoreTokens()) {
    					nextId = tok.nextToken();
    					if(XMLChar.isValidNCName(nextId)) {
                            if (processInMem)
                                getId(result, contextSequence, nextId);
                            else
                                getId((NodeSet)result, docs, nextId);
                        }
    				}
    			} else {
    				if(XMLChar.isValidNCName(nextId)) {
                        if (processInMem)
                            getId(result, contextSequence, nextId);
                        else
                            getId((NodeSet)result, docs, nextId);
                    }
    			}
    		}
        }

        if (context.getProfiler().isEnabled())
            context.getProfiler().end(this, "", result);

        return result;

	}

	private void getId(NodeSet result, DocumentSet docs, String id) throws XPathException {
		NodeSet attribs = context.getBroker().getValueIndex().find(Constants.EQ, docs, null, -1, null, new StringValue(id, Type.ID));
		NodeProxy n, p;
		for (Iterator i = attribs.iterator(); i.hasNext();) {
			n = (NodeProxy) i.next();
			p = new NodeProxy(n.getDocument(), n.getNodeId().getParentId(), Node.ELEMENT_NODE);
			result.add(p);
		}
	}

    private void getId(Sequence result, Sequence seq, String id) throws XPathException {
        Set visitedDocs = new TreeSet();
        for (SequenceIterator i = seq.iterate(); i.hasNext();) {
            NodeImpl v = (NodeImpl) i.nextItem();
            DocumentImpl doc = v.getDocument();
            if (!visitedDocs.contains(doc)) {
                NodeImpl elem = doc.selectById(id);
                if (elem != null)
                    result.add(elem);
                visitedDocs.add(doc);
            }
        }
    }
}
