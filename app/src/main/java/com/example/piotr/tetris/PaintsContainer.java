package com.example.piotr.tetris;

import android.graphics.Paint;

public interface PaintsContainer {
    Paint[] getBlockPaints();
    Paint getBackgroundPaint();
    Paint getBorderPaint();
}
