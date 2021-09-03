package com.malinskiy.marathon.plugin.maven.junit4;

import com.sun.org.apache.bcel.internal.classfile.Unknown;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;

@SuppressWarnings("UnusedDeclaration") // Used reflectively by Maven.
@Mojo(name = "marathon", defaultPhase = VERIFY, threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST)
public final class MarathonMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The directory containing generated classes of the project being tested. This will be included after the test
     * classes in the test classpath.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File classesDirectory;

    /**
     * The directory containing generated test classes of the project being tested. This will be included at the
     * beginning of the test classpath. *
     */
    @Parameter(defaultValue = "${project.build.testOutputDirectory}")
    protected File testClassesDirectory;

    /**
     * Additional elements to be appended to the classpath.
     */
    @Parameter(property = "maven.test.additionalClasspath")
    private String[] additionalClasspathElements;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        
        MavenProject parent = findParent(project);
        log.info("Parent: " + parent.getBasedir().getAbsolutePath());
        
        Set<Artifact> classpathArtifacts = project.getArtifacts();
        HashSet<Artifact> runtime = new HashSet<>();
        HashSet<Artifact> test = new HashSet<>();
        classpathArtifacts
            .stream()
            .forEach(a -> {
                String scope = a.getScope();
                if (scope.equals("runtime") || scope.equals("compile") || scope.equals("provided")) {
                    runtime.add(a);
                } else if (scope.equals("test")) {
                    test.add(a);
                } else {
                    log.info("[" + a.getScope() + "] Unknown artifact " + a.getFile().getAbsolutePath() + "");
                }
            });

        TestClassPath testClassPath = new TestClassPath(classpathArtifacts, classesDirectory,
            testClassesDirectory, additionalClasspathElements);

        File marathonDir = new File(parent.getBasedir(), ".marathon");
        marathonDir.mkdirs();
        File config = new File(marathonDir, project.getGroupId() + "." + project.getArtifactId());

        try {
            FileWriter writer = new FileWriter(config);
            
            writer.append("workdir: ");
            writer.append(project.getBasedir().getAbsolutePath());
            writer.append("\n");
            writer.append("applicationClasspath:\n");
            writer.append("- " + classesDirectory.getAbsolutePath() + "\n");
            for(Artifact a : runtime) {
                writer.append("- " + a.getFile().getAbsolutePath() + "\n");
            }
            writer.append("testClasspath:\n");
            writer.append("- " + testClassesDirectory.getAbsolutePath() + "\n");
            for(Artifact a : test) {
                writer.append("- " + a.getFile().getAbsolutePath() + "\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        
//        log.info("Application classpath");
//        runtime.forEach(a -> log.info("- " + a.getFile().getAbsolutePath()));
//        log.info("Test classpath");
//        test.forEach(a -> log.info("- " + a.getFile().getAbsolutePath()));
//        log.info("Workdir: " + project.getBasedir().getAbsolutePath());
//
//        ProcessBuilder builder = new ProcessBuilder("marathon", "--application-classpath", classesDirectory.getAbsolutePath() + ":" + joinToString(runtime),
//        "--test-classpath", testClassesDirectory.getAbsolutePath() + ":" + joinToString(test));
//
//        Process process = null;
//        try {
//            process = builder.directory(project.getBasedir()).start();
//            inheritIO(process.getInputStream(), System.out);
//            inheritIO(process.getErrorStream(), System.err);
//            process.waitFor();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private MavenProject findParent(MavenProject project) {
        MavenProject parent = project;
        while(parent.getParent() != null && parent.getParent().getBasedir() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    private String joinToString(HashSet<Artifact> runtime) {
        StringBuilder builder = new StringBuilder();
        runtime.forEach(c -> {
            builder.append(c.getFile().getAbsolutePath());
            builder.append(':');
        });
        if(builder.length() > 1) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    private void inheritIO(InputStream src, PrintStream dst) {
        new Thread(() -> {
            Scanner sc = new Scanner(src);
            while (sc.hasNextLine()) {
                dst.println(sc.nextLine());
            }
        }).start();
    }
}
