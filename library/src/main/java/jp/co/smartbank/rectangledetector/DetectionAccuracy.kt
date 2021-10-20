package jp.co.smartbank.rectangledetector

import jp.co.smartbank.rectangledetector.strategy.AdaptiveThresholdStrategy
import jp.co.smartbank.rectangledetector.strategy.CannyAlgorithmStrategy
import jp.co.smartbank.rectangledetector.strategy.CompositeContourDetectionStrategy
import jp.co.smartbank.rectangledetector.strategy.ContourDetectionStrategy

/**
 * Mode of accuracy of rectangle detection in [RectangleDetector].
 */
enum class DetectionAccuracy {
    /**
     * [DetectionAccuracy] that prioritizes the number of detections over the accuracy of the results.
     */
    Aggressive,

    /**
     * [DetectionAccuracy] that prioritizes the accuracy of the results.
     */
    Passive;

    internal fun buildContourStrategy(): ContourDetectionStrategy = when (this) {
        Aggressive -> CompositeContourDetectionStrategy(
            listOf(
                AdaptiveThresholdStrategy(),
                CannyAlgorithmStrategy()
            )
        )
        Passive -> CannyAlgorithmStrategy()
    }
}
