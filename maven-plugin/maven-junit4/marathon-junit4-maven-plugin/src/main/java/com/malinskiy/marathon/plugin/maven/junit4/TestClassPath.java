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

import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.addAll;
import static org.apache.commons.lang3.StringUtils.split;

final class TestClassPath
{
    private final Iterable<Artifact> artifacts;
    private final File classesDirectory;
    private final File testClassesDirectory;
    private final String[] additionalClasspathElements;

    TestClassPath(Iterable<Artifact> artifacts,
                  File classesDirectory,
                  File testClassesDirectory,
                  String[] additionalClasspathElements )
    {
        this.artifacts = artifacts;
        this.classesDirectory = classesDirectory;
        this.testClassesDirectory = testClassesDirectory;
        this.additionalClasspathElements = additionalClasspathElements;
    }

    Map<String, Artifact> getTestDependencies()
    {
        Map<String, Artifact> artifactMapping = new LinkedHashMap<>();
        for ( Artifact artifact : artifacts )
        {
            artifactMapping.put( artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact );
        }
        return artifactMapping;
    }

    Classpath toClasspath()
    {
        List<String> classpath = new ArrayList<>();
        classpath.add( testClassesDirectory.getAbsolutePath() );
        classpath.add( classesDirectory.getAbsolutePath() );
        for ( Artifact artifact : artifacts )
        {
            if ( artifact.getArtifactHandler().isAddedToClasspath() )
            {
                File file = artifact.getFile();
                if ( file != null )
                {
                    classpath.add( file.getAbsolutePath() );
                }
            }
        }
        if ( additionalClasspathElements != null )
        {
            for ( String additionalClasspathElement : additionalClasspathElements )
            {
                if ( additionalClasspathElement != null )
                {
                    addAll( classpath, split( additionalClasspathElement, "," ) );
                }
            }
        }

        return new Classpath( classpath );
    }
}
