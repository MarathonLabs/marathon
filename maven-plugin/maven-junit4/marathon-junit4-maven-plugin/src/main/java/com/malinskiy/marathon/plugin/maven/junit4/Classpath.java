package com.malinskiy.marathon.plugin.maven.junit4;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

import static java.io.File.pathSeparatorChar;

/**
 * An ordered list of classpath elements with set behaviour
 *
 * A Classpath is immutable and thread safe.
 *
 * Immutable and thread safe
 *
 * @author Kristian Rosenvold
 */
public final class Classpath implements Iterable<String>, Cloneable
{
    private final List<String> unmodifiableElements;

    public static Classpath join( Classpath firstClasspath, Classpath secondClasspath )
    {
        LinkedHashSet<String> accumulated =  new LinkedHashSet<>();
        if ( firstClasspath != null )
        {
            firstClasspath.addTo( accumulated );
        }
        if ( secondClasspath != null )
        {
            secondClasspath.addTo( accumulated );
        }
        return new Classpath( accumulated );
    }

    private void addTo( @Nonnull Collection<String> c )
    {
        c.addAll( unmodifiableElements );
    }

    private Classpath()
    {
        unmodifiableElements = Collections.emptyList();
    }

    public Classpath(@Nonnull Classpath other, @Nonnull String additionalElement )
    {
        ArrayList<String> elems = new ArrayList<>( other.unmodifiableElements );
        elems.add( additionalElement );
        unmodifiableElements = Collections.unmodifiableList( elems );
    }

    public Classpath(@Nonnull Collection<String> elements )
    {
        List<String> newCp = new ArrayList<>( elements.size() );
        for ( String element : elements )
        {
            element = element.trim();
            if ( !element.isEmpty() )
            {
                newCp.add( element );
            }
        }
        unmodifiableElements = Collections.unmodifiableList( newCp );
    }

    public static Classpath emptyClasspath()
    {
        return new Classpath();
    }

    public Classpath addClassPathElementUrl( String path )
    {
        if ( path == null )
        {
            throw new IllegalArgumentException( "Null is not a valid class path element url." );
        }
        return unmodifiableElements.contains( path ) ? this : new Classpath( this, path );
    }

    @Nonnull
    public List<String> getClassPath()
    {
        return unmodifiableElements;
    }

    public void writeToSystemProperty( @Nonnull String propertyName )
    {
        StringBuilder sb = new StringBuilder();
        for ( String element : unmodifiableElements )
        {
            sb.append( element )
              .append( pathSeparatorChar );
        }
        System.setProperty( propertyName, sb.toString() );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Classpath classpath = (Classpath) o;

        return unmodifiableElements.equals( classpath.unmodifiableElements );
    }

    @Override
    public int hashCode()
    {
        return unmodifiableElements.hashCode();
    }

    public String getLogMessage( @Nonnull String descriptor )
    {
        StringBuilder result = new StringBuilder( descriptor );
        for ( String element : unmodifiableElements )
        {
            result.append( "  " )
                    .append( element );
        }
        return result.toString();
    }

    public String getCompactLogMessage( @Nonnull String descriptor )
    {
        StringBuilder result = new StringBuilder( descriptor );
        for ( String element : unmodifiableElements )
        {
            result.append( "  " );
            int pos = element.lastIndexOf( File.separatorChar );
            result.append( pos == -1 ? element : element.substring( pos + 1 ) );
        }
        return result.toString();
    }

    @Override
    public Iterator<String> iterator()
    {
        return unmodifiableElements.iterator();
    }

    @Override
    public Classpath clone()
    {
        return new Classpath( unmodifiableElements );
    }
}
