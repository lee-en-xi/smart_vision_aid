package com.example.smart_vision_aid.cropUtils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import com.example.smart_vision_aid.cropUtils.AspectRatio
import kotlin.math.*

public fun IntRect.toRect() = Rect(
    left = left.toFloat(), top = top.toFloat(),
    right = right.toFloat(), bottom = bottom.toFloat()
)

public fun Size.coerceAtMost(maxSize: Size?): Size =
    if (maxSize == null) this else coerceAtMost(maxSize)

public fun Size.coerceAtMost(maxSize: Size): Size {
    val scaleF = min(maxSize.width / width, maxSize.height / height)
    if (scaleF >= 1f) return this
    return Size(width = width * scaleF, height = height * scaleF)
}

public fun Rect.atOrigin(): Rect = Rect(offset = Offset.Zero, size = size)

public val Rect.area get() = width * height

public fun Rect.lerp(target: Rect, p: Float): Rect {
    val tl0 = topLeft
    val br0 = bottomRight
    val dtl = target.topLeft - tl0
    val dbr = target.bottomRight - br0
    return Rect(tl0 + dtl * p, br0 + dbr * p)
}

public fun Rect.centerIn(outer: Rect): Rect =
    translate(outer.center.x - center.x, outer.center.y - center.y)

public fun Rect.fitIn(outer: Rect): Rect {
    val scaleF = min(outer.width / width, outer.height / height)
    return scale(scaleF, scaleF)
}

public fun Rect.scale(sx: Float, sy: Float) = setSizeTL(width = width * sx, height = height * sy)

public fun Rect.setSizeTL(width: Float, height: Float) =
    Rect(offset = topLeft, size = Size(width, height))

public fun Rect.setSizeBR(width: Float, height: Float) =
    Rect(bottom = bottom, right = right, left = right - width, top = bottom - height)

public fun Rect.setSizeCenter(width: Float, height: Float) =
    Rect(offset = Offset(center.x - width / 2, center.y - height / 2), size = Size(width, height))

public fun Rect.constrainResize(bounds: Rect): Rect = Rect(
    left = left.coerceAtLeast(bounds.left),
    top = top.coerceAtLeast(bounds.top),
    right = right.coerceAtMost(bounds.right),
    bottom = bottom.coerceAtMost(bounds.bottom),
)

public fun Rect.constrainOffset(bounds: Rect): Rect {
    var (x, y) = topLeft
    if (right > bounds.right) x += bounds.right - right
    if (bottom > bounds.bottom) y += bounds.bottom - bottom
    if (x < bounds.left) x += bounds.left - x
    if (y < bounds.top) y += bounds.top - y
    return Rect(Offset(x, y), size)
}

public fun IntRect.constrainOffset(bounds: IntRect): IntRect {
    var (x, y) = topLeft
    if (right > bounds.right) x += bounds.right - right
    if (bottom > bounds.bottom) y += bounds.bottom - bottom
    if (x < bounds.left) x += bounds.left - x
    if (y < bounds.top) y += bounds.top - y
    return IntRect(IntOffset(x, y), size)
}

public fun Rect.resize(
    handle: Offset,
    delta: Offset,
): Rect {
    var (l, t, r, b) = this
    val (dx, dy) = delta
    if (handle.y == 1f) b += dy
    else if (handle.y == 0f) t += dy
    if (handle.x == 1f) r += dx
    else if (handle.x == 0f) l += dx
    if (l > r) l = r.also { r = l }
    if (t > b) t = b.also { b = t }
    return Rect(l, t, r, b)
}

public fun Rect.roundOut(): IntRect = IntRect(
    left = floor(left).toInt(), top = floor(top).toInt(),
    right = ceil(right).toInt(), bottom = ceil(bottom).toInt()
)

public fun Size.roundUp(): IntSize = IntSize(ceil(width).toInt(), ceil(height).toInt())

public fun Rect.abs(rel: Offset): Offset {
    return Offset(left + rel.x * width, top + rel.y * height)
}

public fun Rect.setAspect(aspect: AspectRatio): Rect = setAspect(aspect.x.toFloat() / aspect.y)

public fun Rect.setAspect(aspect: Float): Rect {
    val dim = max(width, height)
    return Rect(Offset.Zero, Size(dim * aspect, height = dim))
        .fitIn(this)
        .centerIn(this)
}

public fun Size.keepAspect(old: Size): Size {
    val a = width * height
    return Size(
        width = sqrt((a * old.width) / old.height),
        height = sqrt((a * old.height) / old.width)
    )
}

public fun Rect.keepAspect(old: Rect): Rect {
    return setSize(old, size.keepAspect(old.size))
}

public fun Rect.setSize(old: Rect, size: Size): Rect {
    var (l, t, r, b) = this
    if ((old.left - l).absoluteValue < (old.right - r).absoluteValue) {
        r = l + size.width
    } else {
        l = r - size.width
    }
    if ((old.top - t).absoluteValue < (old.bottom - b).absoluteValue) {
        b = t + size.height
    } else {
        t = b - size.height
    }
    return Rect(l, t, r, b)
}

public fun Rect.scaleToFit(bounds: Rect, old: Rect): Rect {
    val (l, t, r, b) = this
    val scale = minOf(
        (bounds.right - l) / (r - l),
        (bounds.bottom - t) / (b - t),
        (r - bounds.left) / (r - l),
        (bottom - bounds.top) / (b - t),
    )
    if (scale >= 1f) return this
    return setSize(old, size * scale)
}

public fun IntRect.containsInclusive(other: IntRect): Boolean {
    return other.left >= left && other.top >= top &&
            other.right <= right && other.bottom <= bottom
}

public fun Rect.align(alignment: Int): Rect = Rect(
    left.alignDown(alignment), top.alignDown(alignment),
    right.alignUp(alignment), bottom.alignUp(alignment)
)