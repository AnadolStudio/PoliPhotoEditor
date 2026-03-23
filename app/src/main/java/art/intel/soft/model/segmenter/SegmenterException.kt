package art.intel.soft.model.segmenter

sealed class SegmenterException(message: String) : Exception(message)

class EmptyMaskException : SegmenterException("Empty mask")

class SmallMaskException : SegmenterException("Small mask")
