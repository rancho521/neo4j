/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.builtinprocs;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import org.neo4j.collection.RawIterator;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.api.exceptions.ProcedureException;
import org.neo4j.kernel.api.security.AnonymousContext;
import org.neo4j.kernel.impl.api.integrationtest.KernelIntegrationTest;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.neo4j.helpers.collection.Iterators.asList;
import static org.neo4j.kernel.api.proc.ProcedureSignature.procedureName;

public class BuiltInDbmsProceduresIT extends KernelIntegrationTest
{
    @Test
    public void listConfig() throws Exception
    {
        // When
        RawIterator<Object[],ProcedureException> stream =
                dbmsOperations().procedureCallDbms( procedureName( "dbms", "listConfig" ),
                        Collections.singletonList( false ).toArray(),
                        AnonymousContext.none() );

        // Then
        List<Object[]> config = asList( stream );
        List<String> names = config.stream()
                .map( o -> o[0].toString() )
                .collect( Collectors.toList() );

        // The size of the config is not fixed so just make sure it's the right magnitude
        assertTrue( names.size() > 10 );

        assertThat( names, hasItem( GraphDatabaseSettings.record_format.name() ) );

        // Should not contain "unsupported.*" configs
        assertEquals( names.stream()
                .filter( n -> n.startsWith( "unsupported" ) )
                .count(), 0 );
    }

    @Test
    public void listConfigWithUnsupported() throws Exception
    {
        // When
        RawIterator<Object[],ProcedureException> stream =
                dbmsOperations().procedureCallDbms( procedureName( "dbms", "listConfig" ),
                        Collections.singletonList( true ).toArray(),
                        AnonymousContext.none() );

        // Then
        List<Object[]> config = asList( stream );
        List<String> names = config.stream()
                .map( o -> o[0].toString() )
                .collect( Collectors.toList() );

        // The size of the config is not fixed so just make sure it's the right magnitude
        assertTrue( names.size() > 10 );

        assertThat( names, hasItem( GraphDatabaseSettings.record_format.name() ) );

        // Should contain "unsupported.*" configs, again the number is not fixed
        assertTrue( names.stream()
                .filter( n -> n.startsWith( "unsupported" ) )
                .count() > 10 );

        // Check a specific unsupported one
        assertTrue( GraphDatabaseSettings.cypher_runtime.name().startsWith( "unsupported" ) );
        assertThat( names, hasItem( GraphDatabaseSettings.cypher_runtime.name() ) );
    }
}
