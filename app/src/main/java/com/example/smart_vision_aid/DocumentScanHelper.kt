package com.example.smart_vision_aid

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.MSER
import org.opencv.imgproc.Imgproc
import kotlin.math.*

object DocumentScanHelper {

    data class ScanResult(val corners: List<Point>?, val size: Size)

    // 📐 Detect corners from the input Mat (live preview or captured image)
    // In DocumentScanHelper.kt
    fun detectDocumentEdges(input: Mat): DocumentScanHelper.ScanResult? {
        // Step 1: Prepare intermediate Mats
        val gray = Mat()
        val blurred = Mat()
        val edge = Mat()

        // Step 2: Convert to Grayscale
        Imgproc.cvtColor(input, gray, Imgproc.COLOR_BGR2GRAY)

        // Step 3: Apply Gaussian Blur
        Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)

        // Step 4: Adaptive Thresholding
        val adaptiveThresholdBlockSize = 41  // must be odd and >= 3
        Imgproc.adaptiveThreshold(
            blurred, blurred, 255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            adaptiveThresholdBlockSize,
            2.0
        )

        // Step 5: Canny Edge Detection
        val cannyThreshold1 = 50.0
        val cannyThreshold2 = 150.0
        Imgproc.Canny(blurred, edge, cannyThreshold1, cannyThreshold2)

        // Optionally call findContours and getDocumentCorners here
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(edge, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        contours.sortByDescending { Imgproc.contourArea(it) }

        return DocumentScanHelper.ScanResult(
            corners = DocumentScanHelper.getDocumentCorners(contours, input.size())?.corners,
            size = input.size()
        )
    }


    // ✂️ Crop a document using 4 corner points
    fun cropDocument(source: Mat, corners: List<Point>): Mat {
        val tl = corners[0]
        val tr = corners[1]
        val br = corners[2]
        val bl = corners[3]

        val widthTop = hypot(tr.x - tl.x, tr.y - tl.y)
        val widthBottom = hypot(br.x - bl.x, br.y - bl.y)
        val maxWidth = max(widthTop, widthBottom).toInt()

        val heightLeft = hypot(bl.x - tl.x, bl.y - tl.y)
        val heightRight = hypot(br.x - tr.x, br.y - tr.y)
        val maxHeight = max(heightLeft, heightRight).toInt()

        val dstMat = Mat(maxHeight, maxWidth, CvType.CV_8UC4)

        val srcMat = MatOfPoint2f(tl, tr, br, bl)
        val dstPoints = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(maxWidth.toDouble(), 0.0),
            Point(maxWidth.toDouble(), maxHeight.toDouble()),
            Point(0.0, maxHeight.toDouble())
        )

        val transform = Imgproc.getPerspectiveTransform(srcMat, dstPoints)
        Imgproc.warpPerspective(source, dstMat, transform, dstMat.size())

        return dstMat
    }

    // 🌟 Optional enhancement (grayscale + adaptive threshold)
    fun enhanceBitmap(bitmap: Bitmap): Bitmap {
        val srcMat = Mat()
        Utils.bitmapToMat(bitmap, srcMat)
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.adaptiveThreshold(
            srcMat, srcMat, 255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY, 15, 15.0
        )
        val result = Bitmap.createBitmap(srcMat.cols(), srcMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(srcMat, result)
        return result
    }

    // 🔍 Internal: Find contours from image
    private fun findContours(src: Mat): List<MatOfPoint> {
        val gray = Mat()
        val blurred = Mat()
        val edge = Mat()
        val dilated = Mat()
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(9.0, 9.0))

        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(gray, blurred, Size(5.0, 5.0), 0.0)
        Imgproc.threshold(blurred, blurred, 20.0, 255.0, Imgproc.THRESH_TRIANGLE)
        Imgproc.Canny(blurred, edge, 75.0, 200.0)
        Imgproc.dilate(edge, dilated, kernel)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(dilated, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        contours.sortByDescending { Imgproc.contourArea(it) }

        return contours
    }

    // 🎯 Internal: Filter contours to get 4 corners of the largest document-like polygon
    private fun getDocumentCorners(contours: List<MatOfPoint>, size: Size): ScanResult? {
        val maxCheck = min(4, contours.size - 1)
        for (i in 0..maxCheck) {
            val contour2f = MatOfPoint2f(*contours[i].toArray())
            val peri = Imgproc.arcLength(contour2f, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(contour2f, approx, 0.03 * peri, true)
            val points = approx.toList()
            if (points.size == 4 && Imgproc.isContourConvex(MatOfPoint(*approx.toArray()))) {
                val sorted = sortCorners(points)
                return ScanResult(sorted, size)
            }
        }
        return null
    }

    // 🔁 Sort corners in the order: top-left, top-right, bottom-right, bottom-left
    private fun sortCorners(pts: List<Point>): List<Point> {
        val tl = pts.minByOrNull { it.x + it.y } ?: Point()
        val br = pts.maxByOrNull { it.x + it.y } ?: Point()
        val tr = pts.minByOrNull { it.y - it.x } ?: Point()
        val bl = pts.maxByOrNull { it.y - it.x } ?: Point()
        return listOf(tl, tr, br, bl)
    }
}

fun detectAndMergeTextBoxes(input: Mat): List<Rect> {
    // Convert to grayscale
    val gray = Mat()
    Imgproc.cvtColor(input, gray, Imgproc.COLOR_BGR2GRAY)

    // Detect MSER regions
    val mser = MSER.create(21, 200, 8000)
    val msers = mutableListOf<MatOfPoint>()
    mser.detectRegions(gray, msers, MatOfRect())

    // Filter regions by aspect ratio and area
    val rects = msers.mapNotNull { region ->
        val rect = Imgproc.boundingRect(region)
        val aspectRatio = rect.width.toDouble() / rect.height.toDouble()
        val area = Imgproc.contourArea(region)
        if (aspectRatio > 3.0 || area < 200 || area > 8000) {
            null
        } else {
            rect
        }
    }

    // Merge overlapping or close bounding boxes
    val mergedRects = mergeRects(rects, 10) // 10 px padding for merging

    return mergedRects
}

// Merge rects that overlap or are within 'padding' pixels of each other
fun mergeRects(rects: List<Rect>, padding: Int): List<Rect> {
    if (rects.isEmpty()) return emptyList()

    val merged = mutableListOf<Rect>()
    val used = BooleanArray(rects.size)

    for (i in rects.indices) {
        if (used[i]) continue
        var current = rects[i]
        used[i] = true

        var mergedAny: Boolean
        do {
            mergedAny = false
            for (j in rects.indices) {
                if (used[j]) continue
                if (rectsCloseOrOverlap(current, rects[j], padding)) {
                    current = unionRect(current, rects[j])
                    used[j] = true
                    mergedAny = true
                }
            }
        } while (mergedAny)

        merged.add(current)
    }

    return merged
}

// Check if two rectangles overlap or are within padding pixels of each other
fun rectsCloseOrOverlap(a: Rect, b: Rect, padding: Int): Boolean {
    val ax1 = a.x - padding
    val ay1 = a.y - padding
    val ax2 = a.x + a.width + padding
    val ay2 = a.y + a.height + padding

    val bx1 = b.x
    val by1 = b.y
    val bx2 = b.x + b.width
    val by2 = b.y + b.height

    return !(ax2 < bx1 || ax1 > bx2 || ay2 < by1 || ay1 > by2)
}

// Compute union of two rectangles
fun unionRect(a: Rect, b: Rect): Rect {
    val x1 = minOf(a.x, b.x)
    val y1 = minOf(a.y, b.y)
    val x2 = maxOf(a.x + a.width, b.x + b.width)
    val y2 = maxOf(a.y + a.height, b.y + b.height)
    return Rect(x1, y1, x2 - x1, y2 - y1)
}
