/**
 * Copyright 2012-2015 Niall Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.cqengine.query.parser.cqn.support;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.AttributeOrder;
import com.googlecode.cqengine.query.option.OrderByOption;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.QueryParser;
import com.googlecode.cqengine.query.parser.cqn.grammar.CQNGrammarBaseListener;
import com.googlecode.cqengine.query.parser.cqn.grammar.CQNGrammarParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;

import static com.googlecode.cqengine.query.QueryFactory.noQueryOptions;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;
import static com.googlecode.cqengine.query.parser.common.ParserUtils.*;

/**
 * @author Niall Gallagher
 */
public class CQNAntlrListener<O> extends CQNGrammarBaseListener {

    /*
        NOTE: this class depends on classes auto-generated by the antlr4-maven-plugin.
        Run "mvn clean compile" to generate those classes.
    */

    protected final QueryParser<O> queryParser;
    // A map of parent context, to parsed child queries belonging to that context...
    protected final Map<ParserRuleContext, Collection<Query<O>>> childQueries = new HashMap<ParserRuleContext, Collection<Query<O>>>();
    protected int numQueriesEncountered = 0;
    protected int numQueriesParsed = 0;

    protected final List<AttributeOrder<O>> attributeOrders = new LinkedList<AttributeOrder<O>>();

    public CQNAntlrListener(QueryParser<O> queryParser) {
        this.queryParser = queryParser;
    }


    // ======== Handler methods for each type of query defined in the antlr grammar... ========

    @Override
    public void exitAndQuery(CQNGrammarParser.AndQueryContext ctx) {
        addParsedQuery(ctx, QueryFactory.and(childQueries.get(ctx)));
    }

    @Override
    public void exitOrQuery(CQNGrammarParser.OrQueryContext ctx) {
        addParsedQuery(ctx, QueryFactory.or(childQueries.get(ctx)));
    }

    @Override
    public void exitNotQuery(CQNGrammarParser.NotQueryContext ctx) {
        addParsedQuery(ctx, QueryFactory.not(childQueries.get(ctx).iterator().next()));
    }

    @Override
    public void exitEqualQuery(CQNGrammarParser.EqualQueryContext ctx) {
        Attribute<O, Object> attribute = queryParser.getAttribute(ctx.attributeName(), Object.class);
        Object value = queryParser.parseValue(attribute, ctx.queryParameter());
        addParsedQuery(ctx, QueryFactory.equal(attribute, value));
    }

    @Override
    public void exitLessThanOrEqualToQuery(CQNGrammarParser.LessThanOrEqualToQueryContext ctx) {
        Attribute<O, Comparable> attribute = queryParser.getAttribute(ctx.attributeName(), Comparable.class);
        Comparable value = queryParser.parseValue(attribute, ctx.queryParameter());
        addParsedQuery(ctx, QueryFactory.lessThanOrEqualTo(attribute, value));
    }

    @Override
    public void exitLessThanQuery(CQNGrammarParser.LessThanQueryContext ctx) {
        Attribute<O, Comparable> attribute = queryParser.getAttribute(ctx.attributeName(), Comparable.class);
        Comparable value = queryParser.parseValue(attribute, ctx.queryParameter());
        addParsedQuery(ctx, QueryFactory.lessThan(attribute, value));
    }

    @Override
    public void exitGreaterThanOrEqualToQuery(CQNGrammarParser.GreaterThanOrEqualToQueryContext ctx) {
        Attribute<O, Comparable> attribute = queryParser.getAttribute(ctx.attributeName(), Comparable.class);
        Comparable value = queryParser.parseValue(attribute, ctx.queryParameter());
        addParsedQuery(ctx, QueryFactory.greaterThanOrEqualTo(attribute, value));
    }

    @Override
    public void exitGreaterThanQuery(CQNGrammarParser.GreaterThanQueryContext ctx) {
        Attribute<O, Comparable> attribute = queryParser.getAttribute(ctx.attributeName(), Comparable.class);
        Comparable value = queryParser.parseValue(attribute, ctx.queryParameter());
        addParsedQuery(ctx, QueryFactory.greaterThan(attribute, value));
    }

    @Override
    public void exitVerboseBetweenQuery(CQNGrammarParser.VerboseBetweenQueryContext ctx) {
        Attribute<O, Comparable> attribute = queryParser.getAttribute(ctx.attributeName(), Comparable.class);
        List<? extends ParseTree> queryParameters = ctx.queryParameter(), booleanParameters = ctx.BooleanLiteral();
        Comparable lowerValue = queryParser.parseValue(attribute, queryParameters.get(0));
        boolean lowerInclusive = queryParser.parseValue(Boolean.class, booleanParameters.get(0));
        Comparable upperValue = queryParser.parseValue(attribute, queryParameters.get(1));
        boolean upperInclusive = queryParser.parseValue(Boolean.class, booleanParameters.get(1));
        addParsedQuery(ctx, QueryFactory.between(attribute, lowerValue, lowerInclusive, upperValue, upperInclusive));
    }

    @Override
    public void exitBetweenQuery(CQNGrammarParser.BetweenQueryContext ctx) {
        Attribute<O, Comparable> attribute = queryParser.getAttribute(ctx.attributeName(), Comparable.class);
        List<? extends ParseTree> queryParameters = ctx.queryParameter();
        Comparable lowerValue = queryParser.parseValue(attribute, queryParameters.get(0));
        Comparable upperValue = queryParser.parseValue(attribute, queryParameters.get(1));
        addParsedQuery(ctx, QueryFactory.between(attribute, lowerValue, upperValue));
    }

    @Override
    public void exitInQuery(CQNGrammarParser.InQueryContext ctx) {
        Attribute<O, Object> attribute = queryParser.getAttribute(ctx.attributeName(), Object.class);
        List<? extends ParseTree> queryParameters = ctx.queryParameter();
        Collection<Object> values = new ArrayList<Object>(queryParameters.size());
        for (ParseTree queryParameter : queryParameters) {
            Object value = queryParser.parseValue(attribute, queryParameter);
            values.add(value);
        }
        addParsedQuery(ctx, QueryFactory.in(attribute, values));
    }

    @Override
    public void exitStartsWithQuery(CQNGrammarParser.StartsWithQueryContext ctx) {
        Attribute<O, String> attribute = queryParser.getAttribute(ctx.attributeName(), String.class);
        String value = queryParser.parseValue(attribute, ctx.stringQueryParameter());
        addParsedQuery(ctx, QueryFactory.startsWith(attribute, value));
    }

    @Override
    public void exitEndsWithQuery(CQNGrammarParser.EndsWithQueryContext ctx) {
        Attribute<O, String> attribute = queryParser.getAttribute(ctx.attributeName(), String.class);
        String value = queryParser.parseValue(attribute, ctx.stringQueryParameter());
        addParsedQuery(ctx, QueryFactory.endsWith(attribute, value));
    }

    @Override
    public void exitContainsQuery(CQNGrammarParser.ContainsQueryContext ctx) {
        Attribute<O, String> attribute = queryParser.getAttribute(ctx.attributeName(), String.class);
        String value = queryParser.parseValue(attribute, ctx.stringQueryParameter());
        addParsedQuery(ctx, QueryFactory.contains(attribute, value));
    }

    @Override
    public void exitIsContainedInQuery(CQNGrammarParser.IsContainedInQueryContext ctx) {
        Attribute<O, String> attribute = queryParser.getAttribute(ctx.attributeName(), String.class);
        String value = queryParser.parseValue(attribute, ctx.stringQueryParameter());
        addParsedQuery(ctx, QueryFactory.isContainedIn(attribute, value));
    }

    @Override
    public void exitMatchesRegexQuery(CQNGrammarParser.MatchesRegexQueryContext ctx) {
        Attribute<O, String> attribute = queryParser.getAttribute(ctx.attributeName(), String.class);
        String value = queryParser.parseValue(attribute, ctx.stringQueryParameter());
        addParsedQuery(ctx, QueryFactory.matchesRegex(attribute, value));
    }

    @Override
    public void exitHasQuery(CQNGrammarParser.HasQueryContext ctx) {
        Attribute<O, Object> attribute = queryParser.getAttribute(ctx.attributeName(), Object.class);
        addParsedQuery(ctx, QueryFactory.has(attribute));
    }

    @Override
    public void exitAllQuery(CQNGrammarParser.AllQueryContext ctx) {
        validateObjectTypeParameter(queryParser.getObjectType(), ctx.objectType().getText());
        addParsedQuery(ctx, QueryFactory.all(queryParser.getObjectType()));
    }

    @Override
    public void exitNoneQuery(CQNGrammarParser.NoneQueryContext ctx) {
        validateObjectTypeParameter(queryParser.getObjectType(), ctx.objectType().getText());
        addParsedQuery(ctx, QueryFactory.none(queryParser.getObjectType()));
    }

    /** This handler is called for all queries, allows us to validate that no handlers are missing. */
    @Override
    public void exitQuery(CQNGrammarParser.QueryContext ctx) {
        numQueriesEncountered++;
        validateAllQueriesParsed(numQueriesEncountered, numQueriesParsed);
    }

    @Override
    public void exitOrderByOption(CQNGrammarParser.OrderByOptionContext ctx) {
        for (CQNGrammarParser.AttributeOrderContext attributeOrderContext : ctx.attributeOrder()) {
            Attribute<O, Comparable> attribute = queryParser.getAttribute(attributeOrderContext.attributeName(), Comparable.class);
            boolean descending = "descending".equals(attributeOrderContext.direction().getText());
            attributeOrders.add(new AttributeOrder<O>(attribute, descending));
        }
    }

    // ======== Utility methods... ========

    /**
     * Adds the given query to a list of child queries which have not yet been wrapped in a parent query.
     */
    void addParsedQuery(ParserRuleContext currentContext, Query<O> parsedQuery) {
        // Retrieve the possibly null parent query...
        ParserRuleContext parentContext = getParentContextOfType(currentContext, getAndOrNotContextClasses());
        Collection<Query<O>> childrenOfParent = this.childQueries.get(parentContext);
        if (childrenOfParent == null) {
            childrenOfParent = new ArrayList<Query<O>>();
            this.childQueries.put(parentContext, childrenOfParent); // parentContext will be null if this is root query
        }
        childrenOfParent.add(parsedQuery);
        numQueriesParsed++;
    }

    /**
     * Can be called when parsing has finished, to retrieve the parsed query.
     */
    public Query<O> getParsedQuery() {
        Collection<Query<O>> rootQuery = childQueries.get(null);
        validateExpectedNumberOfChildQueries(1, rootQuery.size());
        return rootQuery.iterator().next();
    }

    /**
     * Can be called when parsing has finished, to retrieve the {@link QueryOptions}, which may include an
     * {@link OrderByOption} if found in the string query.
     *
     * @return The parsed {@link QueryOptions}
     */
    public QueryOptions getQueryOptions() {
        return attributeOrders.isEmpty() ? noQueryOptions() : queryOptions(orderBy(attributeOrders));
    }

    protected Class[] getAndOrNotContextClasses() {
        return new Class[] {CQNGrammarParser.AndQueryContext.class, CQNGrammarParser.OrQueryContext.class, CQNGrammarParser.NotQueryContext.class};
    }

}
