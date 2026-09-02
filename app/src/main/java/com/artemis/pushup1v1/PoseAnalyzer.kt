package com.artemis.pushup1v1

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlin.math.abs
import kotlin.math.atan2

/** Detects a push-up using elbow flexion with confidence and hysteresis. */
class PoseAnalyzer(
    private val onRepDetected: () -> Unit,
    private val onDownStateChanged: (Boolean) -> Unit
) {
    private val detector: PoseDetector = PoseDetection.getClient(
        AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
    )

    private var isDown = false
    private var hasStarted = false
    private var smoothedAngle: Double? = null
    private var lastAcceptedAtMs = 0L

    companion object {
        private const val DOWN_ANGLE = 100.0
        private const val UP_ANGLE = 155.0
        private const val MIN_CONFIDENCE = 0.55f
        private const val EMA_ALPHA = 0.35
        private const val MIN_REP_INTERVAL_MS = 450L
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        detector.process(image)
            .addOnSuccessListener(::handlePose)
            .addOnFailureListener { /* Ignore individual bad frames; the stream continues. */ }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun handlePose(pose: Pose) {
        val measurement = computeBestElbow(pose) ?: return
        smoothedAngle = smoothedAngle?.let { it + EMA_ALPHA * (measurement.angle - it) } ?: measurement.angle
        val angle = smoothedAngle ?: return

        when {
            angle <= DOWN_ANGLE && !isDown -> {
                isDown = true
                hasStarted = true
                onDownStateChanged(true)
            }
            angle >= UP_ANGLE && isDown -> {
                isDown = false
                onDownStateChanged(false)
                val now = android.os.SystemClock.elapsedRealtime()
                if (hasStarted && now - lastAcceptedAtMs >= MIN_REP_INTERVAL_MS) {
                    lastAcceptedAtMs = now
                    onRepDetected()
                }
            }
            angle >= UP_ANGLE -> hasStarted = true
        }
    }

    private data class ElbowMeasurement(val angle: Double, val confidence: Float)

    private fun computeBestElbow(pose: Pose): ElbowMeasurement? {
        val left = elbowMeasurement(
            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
            pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW),
            pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        )
        val right = elbowMeasurement(
            pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
            pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW),
            pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        )
        return listOfNotNull(left, right)
            .filter { it.confidence >= MIN_CONFIDENCE }
            .maxByOrNull { it.confidence }
    }

    private fun elbowMeasurement(a: PoseLandmark?, b: PoseLandmark?, c: PoseLandmark?): ElbowMeasurement? {
        if (a == null || b == null || c == null) return null
        val confidence = (a.inFrameLikelihood + b.inFrameLikelihood + c.inFrameLikelihood) / 3f
        return ElbowMeasurement(angleBetween(a, b, c), confidence)
    }

    private fun angleBetween(a: PoseLandmark, b: PoseLandmark, c: PoseLandmark): Double {
        val a1 = atan2((a.position.y - b.position.y).toDouble(), (a.position.x - b.position.x).toDouble())
        val a2 = atan2((c.position.y - b.position.y).toDouble(), (c.position.x - b.position.x).toDouble())
        var degrees = abs(Math.toDegrees(a1 - a2))
        if (degrees > 180.0) degrees = 360.0 - degrees
        return degrees
    }

    fun reset() {
        isDown = false
        hasStarted = false
        smoothedAngle = null
        lastAcceptedAtMs = 0L
        onDownStateChanged(false)
    }

    fun close() = detector.close()
}
