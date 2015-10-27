/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: GeoSymAttributeExpressionProvider.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class GeoSymAttributeExpressionProvider
{
    protected Map<Integer, GeoSymAttributeExpression> expressionMap;

    public GeoSymAttributeExpressionProvider(GeoSymTable table)
    {
        this.expressionMap = new HashMap<Integer, GeoSymAttributeExpression>();
        this.loadExpressions(table);
    }

    public GeoSymAttributeExpression getAttributeExpression(int symbolId)
    {
        return this.expressionMap.get(symbolId);
    }

    protected void loadExpressions(GeoSymTable table)
    {
        this.expressionMap.clear();

        // Group attribute expression rows with the same symbol ID. There is either zero or one set of attribute
        // expressions associated with each symbol ID. Sort expression rows within each group according to the
        // expression sequence number for each row.

        HashMap<Integer, Set<AVList>> map = new HashMap<Integer, Set<AVList>>();

        for (AVList row : table.getRecords())
        {
            Integer symbolId = AVListImpl.getIntegerValue(row, "cond_index");
            Integer sequenceNumber = AVListImpl.getIntegerValue(row, "seq");
            if (symbolId == null || sequenceNumber == null)
            {
                String message = Logging.getMessage("VPF.GeoSymInvalidAttributeExpression", row);
                Logging.logger().warning(message);
                continue;
            }

            Set<AVList> list = map.get(symbolId);
            if (list == null)
            {
                list = new TreeSet<AVList>(new Comparator<AVList>()
                {
                    public int compare(AVList a, AVList b)
                    {
                        Integer ia = AVListImpl.getIntegerValue(a, "seq");
                        Integer ib = AVListImpl.getIntegerValue(b, "seq");

                        return (ia < ib) ? -1 : (ia > ib) ? 1 : 0;
                    }
                });
                map.put(symbolId, list);
            }

            list.add(row);
        }

        // Parse each set of attribute expressions into an abstract logical expression, which may be evaluated at a
        // later time (given a set of feature attributes). Place each expression in a map, keying by the symbol ID
        // associated with that expression.

        for (Map.Entry<Integer, Set<AVList>> entry : map.entrySet())
        {
            LinkedList<Object> queue = new LinkedList<Object>();

            for (AVList row : entry.getValue())
            {
                // If no connector is specified, then assume the terminal connector '0'.
                int connector = AVListImpl.getIntegerValue(row, "connector", 0);
                queue.add(new Comparison(row));
                queue.add(LogicalOperator.values()[connector]);
            }

            Expression expression = this.parseExpression(queue);
            if (expression != null)
            {
                this.expressionMap.put(entry.getKey(), expression);
            }
        }
    }

    //**************************************************************//
    //********************  Expression Parsing  ********************//
    //**************************************************************//

    /**
     * See MIL-HDBK-857A, section 6.4.
     * <p/>
     * Parses the specified queue of logical expression components according to the precendence rules specified in
     * MIL-HDBK-857A, returning a reference to a a live Expression which may be evaluated against any set of attribute
     * values.
     *
     * @param queue the queue of logical expression
     *
     * @return a live Expression which may be evaluated against any set of attribute values.
     */
    protected Expression parseExpression(Queue<?> queue)
    {
        if (queue.isEmpty())
        {
            return null;
        }

        // Perform a recursive descent parsing of the attribute expression components in the queue.

        ExpressionParser comparisonParser = new ComparisonParser(queue);
        ExpressionParser logicalLevel1Parser = new LogicalExpressionParser(queue, comparisonParser,
            EnumSet.of(LogicalOperator.AND_LEVEL1, LogicalOperator.OR_LEVEL1));
        ExpressionParser logicalLevel2Parser = new LogicalExpressionParser(queue, logicalLevel1Parser,
            EnumSet.of(LogicalOperator.AND_LEVEL2, LogicalOperator.OR_LEVEL2));

        return logicalLevel2Parser.parse();
    }

    protected interface ExpressionParser
    {
        Expression parse();
    }

    protected static class ComparisonParser implements ExpressionParser
    {
        protected Queue<?> queue;

        public ComparisonParser(Queue<?> queue)
        {
            this.queue = queue;
        }

        public Expression parse()
        {
            return (this.queue.peek() instanceof Comparison) ? (Comparison) this.queue.poll() : null;
        }
    }

    protected static class LogicalExpressionParser implements ExpressionParser
    {
        protected Queue<?> queue;
        protected ExpressionParser delegateParser;
        protected EnumSet<LogicalOperator> operatorSet;

        public LogicalExpressionParser(Queue<?> queue, ExpressionParser delegateParser,
            EnumSet<LogicalOperator> operatorSet)
        {
            this.queue = queue;
            this.delegateParser = delegateParser;
            this.operatorSet = operatorSet;
        }

        public Expression parse()
        {
            Expression exp = this.delegateParser.parse();
            if (exp == null)
            {
                return null;
            }

            LogicalExpression bool = null;
            LogicalOperator cur;

            while ((cur = this.peekOperator()) != null && this.operatorSet.contains(cur))
            {
                if (bool == null || !bool.logicalOperator.equals(cur))
                {
                    bool = new LogicalExpression(cur);
                    bool.add(exp);
                    exp = bool;
                }

                this.queue.poll();
                bool.add(this.delegateParser.parse());
            }

            return exp;
        }

        protected LogicalOperator peekOperator()
        {
            return (this.queue.peek() instanceof LogicalOperator) ? (LogicalOperator) this.queue.peek() : null;
        }
    }

    //**************************************************************//
    //********************  Expression Syntax  *********************//
    //**************************************************************//

    protected interface Expression extends GeoSymAttributeExpression
    {
        boolean evaluate(AVList params);
    }

    protected static class Comparison implements Expression
    {
        protected String attributeName;
        protected ComparisonOperator operator;
        protected String value;

        public Comparison(String attributeName, ComparisonOperator op, String value)
        {
            this.attributeName = attributeName;
            this.operator = op;
            this.value = value;
        }

        public Comparison(AVList params)
        {
            this.attributeName = params.getStringValue("att");
            this.value = params.getStringValue("value");

            Integer i = AVListImpl.getIntegerValue(params, "oper");
            if (i != null)
                this.operator = ComparisonOperator.values()[i];
        }

        public boolean evaluate(AVList featureAttributes)
        {
            return this.operator.evaluate(featureAttributes, this.attributeName, this.value);
        }
    }

    protected static class LogicalExpression extends ArrayList<Expression> implements Expression
    {
        protected LogicalOperator logicalOperator;

        public LogicalExpression(LogicalOperator op)
        {
            this.logicalOperator = op;
        }

        public boolean evaluate(AVList featureAttributes)
        {
            return this.logicalOperator.evaluate(featureAttributes, this);
        }
    }

    protected static enum ComparisonOperator
    {
        NONE
            {
                public boolean evaluate(AVList params, String paramName, String value)
                {
                    return false;
                }
            },
        EQUAL
            {
                public boolean evaluate(AVList params, String paramName, String value)
                {
                    return compare(params, paramName, value) == 0;
                }
            },
        NOT_EQUAL
            {
                public boolean evaluate(AVList params, String paramName, String value)
                {
                    return !EQUAL.evaluate(params, paramName, value);
                }
            },
        LESS_THAN
            {
                public boolean evaluate(AVList params, String paramName, String value)
                {
                    return compare(params, paramName, value) < 0;
                }
            },
        GREATER_THAN
            {
                public boolean evaluate(AVList params, String paramName, String value)
                {
                    return compare(params, paramName, value) > 0;
                }
            },
        LESS_THAN_OR_EQUAL_TO
            {
                public boolean evaluate(AVList params, String paramName, String value)
                {
                    return compare(params, paramName, value) <= 0;
                }
            },
        GREATER_THAN_OR_EQUAL_TO
            {
                public boolean evaluate(AVList params, String paramName, String value)
                {
                    return compare(params, paramName, value) >= 0;
                }
            };

        public abstract boolean evaluate(AVList params, String paramName, String value);

        private static int compare(AVList params, String paramName, String value)
        {
            Object o = params.getValue(paramName);
            boolean valueIsNull = value.equalsIgnoreCase("NULL");

            // NULL feature attributes are stored as actual Java null references, while NULL values are the literal
            // string "NULL" (without the quotes). If both the attribute and value are null, then the value and
            // attribute are "equal". Otherwise, we say that the non-null quantity is greater than the null quantity.
            if (valueIsNull || o == null)
            {
                return valueIsNull ? (o != null ? -1 : 0) : 1;
            }
            else
            {
                // When value contains a text string, it has a leading and trailing double-quotation character.
                if (value.startsWith("\"") && value.endsWith("\""))
                    value = value.substring(1, value.length() - 1);

                return String.CASE_INSENSITIVE_ORDER.compare(o.toString(), value);
            }
        }
    }

    protected static enum LogicalOperator
    {
        NONE
            {
                public boolean evaluate(AVList params, Iterable<? extends Expression> iterable)
                {
                    return false;
                }
            },
        OR_LEVEL1
            {
                public boolean evaluate(AVList params, Iterable<? extends Expression> iterable)
                {
                    for (Expression term : iterable)
                    {
                        if (term.evaluate(params))
                        {
                            return true;
                        }
                    }

                    return false;
                }
            },
        AND_LEVEL2
            {
                public boolean evaluate(AVList params, Iterable<? extends Expression> iterable)
                {
                    return AND_LEVEL1.evaluate(params, iterable);
                }
            },
        AND_LEVEL1
            {
                public boolean evaluate(AVList params, Iterable<? extends Expression> iterable)
                {
                    for (Expression term : iterable)
                    {
                        if (!term.evaluate(params))
                        {
                            return false;
                        }
                    }

                    return true;
                }
            },
        OR_LEVEL2
            {
                public boolean evaluate(AVList params, Iterable<? extends Expression> iterable)
                {
                    return OR_LEVEL1.evaluate(params, iterable);
                }
            };

        public abstract boolean evaluate(AVList params, Iterable<? extends Expression> iterable);
    }
}
