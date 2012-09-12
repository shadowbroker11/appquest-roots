package org.siriusnet.metaldetector;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class GraphView extends View {
	private static final int VALUE_BUFFER_SIZE = 750;
	private static final long UPDATE_PERIOD = 200;
	private static final float GRAPH_STROKE_WIDTH = 2;
	private static final float GRID_STROKE_WIDTH = 1;
	private static final float GRID_TEXT_SIZE = 8;
	private static final int GRAPH_PADDING = 15;

	private int minY;
	private int maxY;
	private int y;
	private int[] valueBuffer;
	private int valueBufferStartPosition;
	private int defaultXWidth;
	private int maxXWidth;
	private int minXWidth;
	private int xWidth;
	private Color graphForeground;
	private Color gridColor;
	private Timer timer;
	private Paint graphLinePaint;
	private Paint graphSurfacePaint;
	private Paint gridPaint;
	private Paint gridAxisLabels;
	private int halfOfStrokeWidthInPx;
	private int graphPaddingInPx;

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		minY = 0;
		maxY = 100;
		y = 0;
		valueBuffer = new int[VALUE_BUFFER_SIZE];
		valueBufferStartPosition = 0;
		defaultXWidth = 5;
		maxXWidth = 10;
		minXWidth = 1;
		xWidth = defaultXWidth;
		timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 0, UPDATE_PERIOD);
		graphLinePaint = new Paint();
		graphLinePaint.setColor(context.getResources().getColor(
				android.R.color.holo_blue_bright));
		graphLinePaint.setStrokeWidth(convertDpToPx(GRAPH_STROKE_WIDTH));
		graphLinePaint.setAntiAlias(true);
		graphLinePaint.setStyle(Style.STROKE);

		graphSurfacePaint = new Paint();
		graphSurfacePaint.setColor(context.getResources().getColor(
				android.R.color.holo_blue_light)
				^ (0xAF << 24));
		graphSurfacePaint.setAntiAlias(true);
		graphSurfacePaint.setStyle(Style.FILL_AND_STROKE);
		halfOfStrokeWidthInPx = convertDpToPx(GRAPH_STROKE_WIDTH / 2);
		graphPaddingInPx = convertDpToPx(GRAPH_PADDING);

		gridPaint = new Paint();
		gridPaint.setColor(android.graphics.Color.LTGRAY);
		gridPaint.setStrokeWidth(convertDpToPx(GRID_STROKE_WIDTH));
		gridPaint.setStyle(Style.STROKE);

		gridAxisLabels = new Paint();
		gridAxisLabels.setColor(Color.BLACK);
		gridAxisLabels.setTextSize(convertDpToPx(GRID_TEXT_SIZE));
		gridAxisLabels.setTextAlign(Align.RIGHT);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		int graphWidth = canvas.getWidth() - 2 * graphPaddingInPx;
		int xMarginLeft = graphPaddingInPx * 2;
		int graphHeight = canvas.getHeight() - 2 * graphPaddingInPx;
		int yMarginTop = convertDpToPx(GRID_TEXT_SIZE) / 2;

		int rows = 10;
		int rowHeight = graphHeight / rows;
		int colWidth = xWidth * 4;
		int cols = graphWidth / colWidth;
		float[] gridPoints = new float[(rows + cols + 2) * 4];
		int y;
		for (y = 0; y <= rows + 1; y++) {
			gridPoints[y * 4] = xMarginLeft;
			gridPoints[y * 4 + 1] = yMarginTop + y * rowHeight;
			gridPoints[y * 4 + 2] = xMarginLeft + graphWidth;
			gridPoints[y * 4 + 3] = yMarginTop + y * rowHeight;
			if (y <= rows) {
				canvas.drawText(100 - y * 10 + "%", xMarginLeft
						- convertDpToPx(2), convertDpToPx(GRID_TEXT_SIZE) / 2
						+ yMarginTop + y * rowHeight, gridAxisLabels);
			}
		}
		int aOffset = (y - 1) * 4;
		for (int x = 0; x <= cols; x++) {
			gridPoints[aOffset + x * 4] = xMarginLeft + x * colWidth;
			gridPoints[aOffset + x * 4 + 1] = yMarginTop;
			gridPoints[aOffset + x * 4 + 2] = xMarginLeft + x * colWidth;
			gridPoints[aOffset + x * 4 + 3] = yMarginTop + graphHeight;
		}

		canvas.drawLines(gridPoints, gridPaint);

		drawGraph(canvas, graphWidth, xMarginLeft, graphHeight, yMarginTop);
	}

	private void drawGraph(Canvas canvas, int graphWidth, int xMarginLeft,
			int graphHeight, int yMarginTop) {
		Path path = new Path();
		Path surface = new Path();
		path.moveTo(xMarginLeft, graphHeight + yMarginTop); //FIXME
		surface.moveTo(xMarginLeft, graphHeight + yMarginTop
				+ halfOfStrokeWidthInPx);
		for (int x = 0; graphWidth / xWidth + 1 > x; x++) {
			float xPos = x * xWidth + xMarginLeft;
			float yPos = graphHeight
					+ yMarginTop
					- valueBuffer[(valueBufferStartPosition + x
							+ valueBuffer.length - (graphWidth / xWidth + 1))
							% valueBuffer.length]
					* ((graphHeight + yMarginTop) / 100);
			if (x + 1 < graphWidth / xWidth + 1) {
				path.lineTo(xPos, yPos);
				surface.lineTo(xPos, yPos);
			} else {
				path.setLastPoint(xPos, yPos);
				surface.lineTo(xPos, yPos);
			}
		}
		surface.lineTo(graphWidth + xMarginLeft, graphHeight + yMarginTop
				+ halfOfStrokeWidthInPx);
		canvas.drawPath(surface, graphSurfacePaint);
		canvas.drawPath(path, graphLinePaint);
	}

	public Color getGraphForeground() {
		return graphForeground;
	}

	public void setGraphForeground(Color graphForeground) {
		this.graphForeground = graphForeground;
	}

	public int getGraphLine() {
		return graphLinePaint.getColor();
	}

	public void setGraphLine(int graphLine) {
		graphLinePaint.setColor(graphLine);
	}

	public Color getGridColor() {
		return gridColor;
	}

	public void setGridColor(Color gridColor) {
		this.gridColor = gridColor;
	}

	public void setYValue(int y) {
		this.y = y;
	}

	public int getMinY() {
		return minY;
	}

	public void setMinY(int minY) {
		this.minY = minY;
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

	public int getDefaultXWidth() {
		return defaultXWidth;
	}

	public void setDefaultXWidth(int defaultXWidth) {
		this.defaultXWidth = defaultXWidth;
	}

	public int getMaxXWidth() {
		return maxXWidth;
	}

	public void setMaxXWidth(int maxXWidth) {
		this.maxXWidth = maxXWidth;
	}

	public int getMinXWidth() {
		return minXWidth;
	}

	public void setMinXWidth(int minXWidth) {
		this.minXWidth = minXWidth;
	}

	public int getxWidth() {
		return xWidth;
	}

	public void setxWidth(int xWidth) {
		this.xWidth = xWidth;
	}

	private TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			if (valueBufferStartPosition == valueBuffer.length) {
				valueBufferStartPosition = 0;
			}
			valueBuffer[valueBufferStartPosition++] = y;
			post(new Runnable() {

				@Override
				public void run() {
					invalidate();
				}
			});
		}
	};

	private int convertDpToPx(float dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getContext().getResources().getDisplayMetrics());
	}
}
