package com.malinskiy.marathon.android.executor.listeners.screenshot

//
//  GifSequenceWriter.java
//
//  Created by Elliot Kroo on 2009-04-25.
//
// This work is licensed under the Creative Commons Attribution 3.0 Unported
// License. To view a copy of this license, visit
// http://creativecommons.org/licenses/by/3.0/ or send a letter to Creative
// Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.


import java.awt.image.RenderedImage
import java.io.File
import java.io.IOException
import javax.imageio.IIOException
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageTypeSpecifier
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.FileImageOutputStream
import javax.imageio.stream.ImageOutputStream

class GifSequenceWriter
/**
 * Creates a new GifSequenceWriter
 *
 * @param outputStream the ImageOutputStream to be written to
 * @param imageType one of the imageTypes specified in BufferedImage
 * @param timeBetweenFramesMS the time between frames in miliseconds
 * @param loopContinuously wether the gif should loop repeatedly
 * @throws IIOException if no gif ImageWriters are found
 *
 * @author Elliot Kroo (elliot[at]kroo[dot]net)
 */
@Throws(IIOException::class, IOException::class)
constructor(
    outputStream: ImageOutputStream,
    imageType: Int,
    timeBetweenFramesMS: Int,
    loopContinuously: Boolean
) {
    protected var gifWriter: ImageWriter
    protected var imageWriteParam: ImageWriteParam
    protected var imageMetaData: IIOMetadata

    init {
        // my method to create a writer
        gifWriter = writer
        imageWriteParam = gifWriter.defaultWriteParam
        val imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType)

        imageMetaData = gifWriter.getDefaultImageMetadata(
            imageTypeSpecifier,
            imageWriteParam
        )

        val metaFormatName = imageMetaData.nativeMetadataFormatName

        val root = imageMetaData.getAsTree(metaFormatName) as IIOMetadataNode

        val graphicsControlExtensionNode = getNode(
            root,
            "GraphicControlExtension"
        )

        graphicsControlExtensionNode.setAttribute("disposalMethod", "none")
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE")
        graphicsControlExtensionNode.setAttribute(
            "transparentColorFlag",
            "FALSE"
        )
        graphicsControlExtensionNode.setAttribute(
            "delayTime",
            Integer.toString(timeBetweenFramesMS / 10)
        )
        graphicsControlExtensionNode.setAttribute(
            "transparentColorIndex",
            "0"
        )

        val commentsNode = getNode(root, "CommentExtensions")
        commentsNode.setAttribute("CommentExtension", "Created by MAH")

        val appEntensionsNode = getNode(
            root,
            "ApplicationExtensions"
        )

        val child = IIOMetadataNode("ApplicationExtension")

        child.setAttribute("applicationID", "NETSCAPE")
        child.setAttribute("authenticationCode", "2.0")

        val loop = if (loopContinuously) 0 else 1

        child.userObject = byteArrayOf(0x1, (loop and 0xFF).toByte(), (loop shr 8 and 0xFF).toByte())
        appEntensionsNode.appendChild(child)

        imageMetaData.setFromTree(metaFormatName, root)

        gifWriter.output = outputStream

        gifWriter.prepareWriteSequence(
            null
        )
    }

    @Throws(IOException::class)
    fun writeToSequence(img: RenderedImage) {
        gifWriter.writeToSequence(
            IIOImage(
                img, null,
                imageMetaData
            ),
            imageWriteParam
        )
    }

    /**
     * Close this GifSequenceWriter object. This does not close the underlying
     * stream, just finishes off the GIF.
     */
    @Throws(IOException::class)
    fun close() {
        gifWriter.endWriteSequence()
    }

    companion object {

        /**
         * Returns the first available GIF ImageWriter using
         * ImageIO.getImageWritersBySuffix("gif").
         *
         * @return a GIF ImageWriter object
         * @throws IIOException if no GIF image writers are returned
         */
        private val writer: ImageWriter
            @Throws(IIOException::class)
            get() {
                val iter = ImageIO.getImageWritersBySuffix("gif")
                return if (!iter.hasNext()) {
                    throw IIOException("No GIF Image Writers Exist")
                } else {
                    iter.next()
                }
            }

        /**
         * Returns an existing child node, or creates and returns a new child node (if
         * the requested node does not exist).
         *
         * @param rootNode the <tt>IIOMetadataNode</tt> to search for the child node.
         * @param nodeName the name of the child node.
         *
         * @return the child node, if found or a new node created with the given name.
         */
        private fun getNode(
            rootNode: IIOMetadataNode,
            nodeName: String
        ): IIOMetadataNode {
            val nNodes = rootNode.length
            for (i in 0 until nNodes) {
                if (rootNode.item(i).nodeName.compareTo(nodeName, ignoreCase = true) == 0) {
                    return rootNode.item(i) as IIOMetadataNode
                }
            }
            val node = IIOMetadataNode(nodeName)
            rootNode.appendChild(node)
            return node
        }

        /**
         * public GifSequenceWriter(
         * BufferedOutputStream outputStream,
         * int imageType,
         * int timeBetweenFramesMS,
         * boolean loopContinuously) {
         *
         */

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size > 1) {
                // grab the output image type from the first image in the sequence
                val firstImage = ImageIO.read(File(args[0]))

                // create a new BufferedOutputStream with the last argument
                val output = FileImageOutputStream(File(args[args.size - 1]))

                // create a gif sequence with the type of the first image, 1 second
                // between frames, which loops continuously
                val writer = GifSequenceWriter(output, firstImage.type, 1, false)

                // write out the first image to our sequence...
                writer.writeToSequence(firstImage)
                for (i in 1 until args.size - 1) {
                    val nextImage = ImageIO.read(File(args[i]))
                    writer.writeToSequence(nextImage)
                }

                writer.close()
                output.close()
            } else {
                println(
                    "Usage: java GifSequenceWriter [list of gif files] [output file]"
                )
            }
        }
    }
}