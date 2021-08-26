package com.malinskiy.marathon.vendor.junit4.booter.isolation

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.util.Enumeration

class ChildFirstURLClassLoader(classpath: Array<URL>, parent: ClassLoader) : URLClassLoader(classpath, parent) {
    private val system: ClassLoader? = getSystemClassLoader()

    @Synchronized
    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String, resolve: Boolean): Class<*>? {
        var existingClass = findLoadedClass(name)
        if (existingClass == null) {
            if (system != null) {
                try {
                    existingClass = system.loadClass(name)
                } catch (ignored: ClassNotFoundException) {
                }
            }
            if (existingClass == null) {
                existingClass = try {
                    findClass(name)
                } catch (e: ClassNotFoundException) {
                    super.loadClass(name, resolve)
                }
            }
        }
        if (resolve) {
            resolveClass(existingClass)
        }
        return existingClass
    }

    override fun getResource(name: String): URL {
        var url: URL? = null
        if (system != null) {
            url = system.getResource(name)
        }
        if (url == null) {
            url = findResource(name)
            if (url == null) {
                url = super.getResource(name)
            }
        }
        return url!!
    }

    @Throws(IOException::class)
    override fun getResources(name: String): Enumeration<URL> {
        var systemUrls: Enumeration<URL>? = null
        if (system != null) {
            systemUrls = system.getResources(name)
        }
        val localUrls = findResources(name)
        var parentUrls: Enumeration<URL>? = null
        if (parent != null) {
            parentUrls = parent.getResources(name)
        }
        val urls: MutableList<URL> = ArrayList()
        if (systemUrls != null) {
            while (systemUrls.hasMoreElements()) {
                urls.add(systemUrls.nextElement())
            }
        }
        if (localUrls != null) {
            while (localUrls.hasMoreElements()) {
                urls.add(localUrls.nextElement())
            }
        }
        if (parentUrls != null) {
            while (parentUrls.hasMoreElements()) {
                urls.add(parentUrls.nextElement())
            }
        }
        return object : Enumeration<URL> {
            var iter: Iterator<URL> = urls.iterator()
            override fun hasMoreElements(): Boolean {
                return iter.hasNext()
            }

            override fun nextElement(): URL {
                return iter.next()
            }
        }
    }

    override fun getResourceAsStream(name: String): InputStream? {
        val url = getResource(name)
        try {
            return if (url != null) url.openStream() else null
        } catch (e: IOException) {
        }
        return null
    }
}
